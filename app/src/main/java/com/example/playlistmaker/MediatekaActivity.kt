package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MediatekaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mediateka)
        setup_BackButton()
    }

    private fun setup_BackButton(){
        val backButton = findViewById<ImageButton>(R.id.strelka_Mediateka)
        backButton.setOnClickListener {
            finish()
        }
    }


}