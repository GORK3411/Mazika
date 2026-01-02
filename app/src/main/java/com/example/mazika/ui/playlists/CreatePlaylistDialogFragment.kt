package com.example.mazika.ui.playlists

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.mazika.R

class CreatePlaylistDialogFragment(
    private val onCreate: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_playlist, null)

        val editText = view.findViewById<EditText>(R.id.etPlaylistName)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    onCreate(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
