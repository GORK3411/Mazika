package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.MainActivity
import com.example.mazika.R
import com.example.mazika.model.PlaylistSummary
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistPickerBottomSheet(
    private val onPlaylistSelected: (playlistId: Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottomsheet_playlist_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]

        recyclerView = view.findViewById(R.id.playlistRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PlaylistAdapter(
            onClick = { pl ->
                onPlaylistSelected(pl.id)
                dismiss()
            },
            onMoreClick = { _, _ ->
                // no "more" menu in picker
            }
        )
        recyclerView.adapter = adapter

        playlistViewModel.playlistSummaries.observe(viewLifecycleOwner) { summaries ->
            /*
            val summaries: List<PlaylistSummary> = playlists.map {
                PlaylistSummary(
                    id = it.id,
                    name = it.name,
                    songCount = 0 // keep it simple for picker (fast)
                )
            }
             */
            adapter.submitList(summaries)
        }
    }
}
