package com.example.playlistmaker.presentation.playlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.data.storage.PlaylistStorage
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator

class NewPlaylistActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var createButton: Button
    private lateinit var playlistStorage: PlaylistStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_playlist)


        playlistStorage = Creator.providePlaylistStorage(this)

        nameEditText = findViewById(R.id.playlistNameEditText)
        descriptionEditText = findViewById(R.id.playlistDescriptionEditText)
        createButton = findViewById(R.id.createButton)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        createButton.isEnabled = false

        nameEditText.addTextChangedListener {
            createButton.isEnabled = !it.isNullOrBlank()
        }

        createButton.setOnClickListener {
            val playlistName = nameEditText.text.toString().trim()
            val playlistDescription = descriptionEditText.text.toString().trim()

            val playlist = Playlist(
                name = playlistName,
                description = playlistDescription
            )

            playlistStorage.savePlaylist(playlist)

            val resultIntent = Intent().apply {
                putExtra(PLAYLIST_NAME_EXTRA, playlistName)
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    companion object {
        const val PLAYLIST_NAME_EXTRA = "playlist_name_extra"
    }
}