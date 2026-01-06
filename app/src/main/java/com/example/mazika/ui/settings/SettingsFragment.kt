package com.example.mazika.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mazika.R
import com.example.mazika.databinding.FragmentSettingsBinding
import com.example.mazika.settings.AccentColor
import com.example.mazika.settings.AppSettings
import com.example.mazika.settings.ThemeApplier
import com.example.mazika.settings.ThemeMode
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var ignoreUiEvents = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        // Reflect saved theme
        viewLifecycleOwner.lifecycleScope.launch {
            AppSettings.themeFlow(requireContext()).collect { mode ->
                ignoreUiEvents = true
                when (mode) {
                    ThemeMode.SYSTEM -> binding.rbThemeSystem.isChecked = true
                    ThemeMode.LIGHT -> binding.rbThemeLight.isChecked = true
                    ThemeMode.DARK -> binding.rbThemeDark.isChecked = true
                }
                ignoreUiEvents = false
            }
        }

        // Reflect saved accent
        viewLifecycleOwner.lifecycleScope.launch {
            AppSettings.accentFlow(requireContext()).collect { accent ->
                ignoreUiEvents = true
                when (accent) {
                    AccentColor.PURPLE -> binding.chipPurple.isChecked = true
                    AccentColor.GREEN -> binding.chipGreen.isChecked = true
                    AccentColor.BLUE -> binding.chipBlue.isChecked = true
                    AccentColor.ORANGE -> binding.chipOrange.isChecked = true
                }
                ignoreUiEvents = false
            }
        }

        // Theme change (NO manual recreate)
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            if (ignoreUiEvents) return@setOnCheckedChangeListener

            val mode = when (checkedId) {
                R.id.rbThemeSystem -> ThemeMode.SYSTEM
                R.id.rbThemeLight -> ThemeMode.LIGHT
                R.id.rbThemeDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }

            viewLifecycleOwner.lifecycleScope.launch {
                AppSettings.setTheme(requireContext(), mode)
                ThemeApplier.applyNightMode(mode)
            }
        }

        // Accent change (YES recreate)
        binding.cgAccent.setOnCheckedStateChangeListener { _, checkedIds ->
            if (ignoreUiEvents) return@setOnCheckedStateChangeListener

            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val accent = when (id) {
                R.id.chipPurple -> AccentColor.PURPLE
                R.id.chipGreen -> AccentColor.GREEN
                R.id.chipBlue -> AccentColor.BLUE
                R.id.chipOrange -> AccentColor.ORANGE
                else -> AccentColor.PURPLE
            }

            viewLifecycleOwner.lifecycleScope.launch {
                AppSettings.setAccent(requireContext(), accent)
                requireActivity().recreate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
