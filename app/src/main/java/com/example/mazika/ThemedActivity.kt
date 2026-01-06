package com.example.mazika

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mazika.settings.ThemeApplier

open class ThemedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeApplier.apply(this) // MUST be before super
        super.onCreate(savedInstanceState)
    }
}
