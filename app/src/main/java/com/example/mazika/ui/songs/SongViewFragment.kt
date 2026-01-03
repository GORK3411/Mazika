package com.example.mazika.ui.songs

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mazika.R
import com.example.mazika.databinding.SongViewBinding

class SongViewFragment : Fragment(R.layout.song_view) {

    private var _binding: SongViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var songViewModel: SongViewModel
    private var userIsSeeking = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SongViewBinding.bind(view)

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        songViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song == null) {
                binding.tvNowTitle.text = "No song playing"
                binding.tvNowArtist.text = ""
            } else {
                binding.tvNowTitle.text = song.title
                binding.tvNowArtist.text = song.artist
            }
        }

        songViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            binding.btnPlay.setImageResource(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        binding.btnPlay.setOnClickListener { songViewModel.togglePlayback() }
        binding.btnNext.setOnClickListener { songViewModel.next() }
        binding.btnPrev.setOnClickListener { songViewModel.previous() }

        songViewModel.duration.observe(viewLifecycleOwner) { dur ->
            val safeDur = dur.coerceAtLeast(1)
            binding.seekBar.max = safeDur
            binding.tvTotalTime.text = formatTime(safeDur)
        }

        songViewModel.position.observe(viewLifecycleOwner) { pos ->
            val safePos = pos.coerceAtLeast(0)

            if (!userIsSeeking) {
                binding.seekBar.progress = safePos
                binding.tvCurrentTime.text = formatTime(safePos)
            } else {
                // while dragging, current time is updated in onProgressChanged
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = false
                songViewModel.seekTo(seekBar.progress)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = formatTime(progress)
            }
        })
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
