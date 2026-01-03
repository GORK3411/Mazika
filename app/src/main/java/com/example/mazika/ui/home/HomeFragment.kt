package com.example.mazika.ui.home

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mazika.R
import com.example.mazika.databinding.FragmentHomeBinding
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.ui.playlists.MessageDialogFragment
import com.example.mazika.ui.playlists.PlaylistDetailsLookup
import com.example.mazika.ui.playlists.PlaylistPickerBottomSheet
import com.example.mazika.ui.playlists.PlaylistViewModel
import com.example.mazika.ui.songs.SongAdapter
import com.example.mazika.ui.songs.SongDetailsLookup
import com.example.mazika.ui.songs.SongViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var songViewModel: SongViewModel
    private lateinit var songAdapter: SongAdapter


    private var fullList: List<Song> = emptyList()
    private var query: String = ""
    private var sortMode: SortMode = SortMode.NAME

    private enum class SortMode { NAME, ARTIST, DATE }

    private lateinit var tracker:SelectionTracker<Long>;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        // Adapter: clicking a song should start playing immediately (Spotify behavior)
        songAdapter = SongAdapter(
            onSongClick = { clickedSong ->
                val visible = songAdapter.currentList
                val ids = visible.map { it.id }.toMutableList()
                ids.remove(clickedSong.id)
                ids.add(0, clickedSong.id)
                songViewModel.playSongs(ids)
            }
        )

        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSongs.adapter = songAdapter

        // Loading on start
        binding.pbLoading.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        // Observe songs from repository
        songViewModel.songs.observe(viewLifecycleOwner) { list ->
            fullList = list ?: emptyList()
            binding.pbLoading.visibility = View.GONE
            applyFilterSort()
        }

        // Search
        binding.etSearch.addTextChangedListener {
            query = it?.toString().orEmpty()
            applyFilterSort()
        }

        // Sort chips
        binding.chipSortGroup.setOnCheckedChangeListener { _, checkedId ->
            sortMode = when (checkedId) {
                R.id.chipSortArtist -> SortMode.ARTIST
                R.id.chipSortDate -> SortMode.DATE
                else -> SortMode.NAME
            }
            applyFilterSort()
        }

        // Highlight currently playing song in the list (Spotify touch)
        songViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            songAdapter.setNowPlaying(song?.id, songViewModel.isPlaying.value == true)
        }
        songViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            songAdapter.setNowPlaying(songViewModel.currentSong.value?.id, playing == true)
        }


        val recyclerView = binding.rvSongs

        tracker = SelectionTracker.Builder<Long>(
            "songSelection",
            recyclerView,
            StableIdKeyProvider(recyclerView),
            SongDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        songAdapter.tracker = tracker
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val count = tracker.selection.size()

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
    }

    private fun applyFilterSort() {
        val q = query.trim().lowercase()

        val filtered = if (q.isEmpty()) {
            fullList
        } else {
            fullList.filter { s ->
                s.title.lowercase().contains(q) || s.artist.lowercase().contains(q)
            }
        }

        val sorted = when (sortMode) {
            SortMode.NAME -> filtered.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortMode.DATE -> filtered.sortedByDescending { it.createDate }
        }

        songAdapter.submitList(sorted)

        val showEmpty = sorted.isEmpty()
        binding.tvEmpty.visibility = if (showEmpty) View.VISIBLE else View.GONE

        binding.tvEmpty.text =
            if (q.isNotEmpty()) "No results for \"$query\""
            else "No songs found (download songs to your phone first)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //This is used with the tracker
    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            mode?.menuInflater?.inflate(R.menu.selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedIds = tracker.selection.toList()

            when(item?.itemId) {
                R.id.menu_play->{
                    PlayBackRepository.play(selectedIds)
                    mode?.finish()
                    return true
                }
                R.id.menu_add_to_play_queue->
                {

                }
                R.id.menu_add_to_playlist-> {
                    val sheet = PlaylistPickerBottomSheet() { playlistId ->
                        lifecycleScope.launch {
                            try {
                                songViewModel.addSongsToPlaylist(playlistId,selectedIds)
                            }
                            catch (e: Exception)
                            {
                                MessageDialogFragment(e.message.toString())
                                    .show(parentFragmentManager, "Fail")
                                //Toast.makeText( activity, e.message, Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                    sheet.show(parentFragmentManager, "PlaylistPicker")
                    mode?.finish()
                    return true
                }

            }
            return false
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when ActionMode ends
            tracker?.clearSelection()
            actionMode = null
        }

    }
}
