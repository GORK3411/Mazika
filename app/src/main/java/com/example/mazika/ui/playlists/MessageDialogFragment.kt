package com.example.mazika.ui.playlists

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mazika.R

class MessageDialogFragment(private val message: String): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_message, null)

        val textView: TextView = view.findViewById<TextView>(R.id.message)
        textView.text = message

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("Ok", null)
            .create()
    }
}