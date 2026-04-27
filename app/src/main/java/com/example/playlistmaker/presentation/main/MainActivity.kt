package com.example.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.presentation.media.MediatekaActivity
import com.example.playlistmaker.presentation.search.PoiskActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val contentContainer = findViewById<View>(R.id.contentContainer)

        val startPadding = contentContainer.paddingStart
        val topPadding = contentContainer.paddingTop
        val endPadding = contentContainer.paddingEnd
        val bottomPadding = contentContainer.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(contentContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                startPadding,
                topPadding + systemBars.top,
                endPadding,
                bottomPadding + systemBars.bottom
            )
            insets
        }

        val button = findViewById<Button>(R.id.poisk)
        button.setOnClickListener {
            startActivity(Intent(this, PoiskActivity::class.java))
        }

        val button2 = findViewById<Button>(R.id.mediateka)
        button2.setOnClickListener {
            startActivity(Intent(this, MediatekaActivity::class.java))
        }

        val button3 = findViewById<Button>(R.id.nastroiki)
        button3.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}