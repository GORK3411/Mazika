package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.MainActivity
import com.example.mazika.R
import com.example.mazika.ui.songs.SongViewModel
import com.example.mazika.model.PlaylistSummary
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class PlaylistFragment : Fragment(R.layout.fragment_playlist) {

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter
    private lateinit var tracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null
    private var allItems: List<PlaylistSummary> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //playlistViewModel = (activity as MainActivity).playlistViewModel
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyPlaylists)
        val etSearch = view.findViewById<EditText>(R.id.etSearchPlaylists)

        recyclerView = view.findViewById(R.id.rvPlaylists)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PlaylistAdapter(
            onClick = { playlist ->
                // if selection mode active, do NOT navigate
                if (tracker.selection.size() != 0) return@PlaylistAdapter

                val bundle = Bundle().apply {
                    putInt("playlistId", playlist.id)
                    putString("playlistName", playlist.name)
                }
                findNavController().navigate(R.id.action_navigation_playlists_to_playlistDetailsFragment, bundle)
            },
            onMoreClick = { anchor, playlist ->
                // popup menu per row
                val menu = PopupMenu(anchor.context, anchor)
                menu.menuInflater.inflate(R.menu.menu_playlist_row, menu.menu)
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_rename -> {
                            SimpleDialogFragment("Rename") { newName ->
                                playlistViewModel.renamePlaylist(playlist.id, newName)
                            }.show(parentFragmentManager, "RenamePlaylistDialog")
                            true
                        }
                        R.id.menu_delete -> {
                            playlistViewModel.deletePlaylists(listOf(playlist.id))
                            true
                        }
                        else -> false
                    }
                }
                menu.show()
            }
        )
        recyclerView.adapter = adapter

        // ✅ SelectionTracker (same pattern you had)
        tracker = SelectionTracker.Builder<Long>(
            "playlistSelection",
            recyclerView,
            StableIdKeyProvider(recyclerView),
            PlaylistDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        adapter.tracker = tracker

        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val count = tracker.selection.size()

                // refresh strokes
                //adapter.notifyDataSetChanged()

                if (count > 0) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        })

        // Observe playlists list (summaries)
        playlistViewModel.playlistSummaries.observe(viewLifecycleOwner) { items ->
            allItems = items
            applyFilter(etSearch.text?.toString().orEmpty(), tvEmpty)
        }

        // Search filter
        etSearch.doAfterTextChanged { txt ->
            applyFilter(txt?.toString().orEmpty(), tvEmpty)
        }

        // Create playlist button
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnCreatePlaylist)
        btnCreate.setOnClickListener {
            SimpleDialogFragment("Create") { playlistName ->
                playlistViewModel.addPlaylist(playlistName)
            }.show(parentFragmentManager, "CreatePlaylistDialog")
        }
    }

    private fun applyFilter(query: String, tvEmpty: TextView) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) allItems else allItems.filter { it.name.lowercase().contains(q) }
        adapter.submitList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            mode?.menuInflater?.inflate(R.menu.playlist_selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedIdsInt: List<Int> = tracker.selection.map { it.toInt() }

            return when (item?.itemId) {
                R.id.menu_delete -> {
                    playlistViewModel.deletePlaylists(selectedIdsInt)
                    mode?.finish()
                    tracker.clearSelection()
                    true
                }

                R.id.menu_add_to_playlist -> {
                    val sheet = PlaylistPickerBottomSheet() { parentPlaylistId ->
                        lifecycleScope.launch {
                            try {
                                playlistViewModel.addChildrenToPlaylist(parentPlaylistId, selectedIdsInt)
                            } catch (e: Exception) {
                                MessageDialogFragment(e.message.toString())
                                    .show(parentFragmentManager, "Fail")
                            }
                        }
                    }
                    sheet.show(parentFragmentManager, "PlaylistPicker")
                    mode?.finish()
                    tracker.clearSelection()
                    true
                }

                else ->
                {
                    tracker.clearSelection()
                    false
                }
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            tracker.clearSelection()
            actionMode = null
        }
    }
}