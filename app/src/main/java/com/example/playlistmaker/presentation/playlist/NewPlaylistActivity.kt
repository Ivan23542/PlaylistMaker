package com.example.playlistmaker.presentation.playlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator

class NewPlaylistActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var createButton: Button
    private lateinit var viewModel: NewPlaylistViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_playlist)

        viewModel = ViewModelProvider(
            this,
            Creator.provideNewPlaylistViewModelFactory(applicationContext)
        )[NewPlaylistViewModel::class.java]

        nameEditText = findViewById(R.id.playlistNameEditText)
        descriptionEditText = findViewById(R.id.playlistDescriptionEditText)
        createButton = findViewById(R.id.createButton)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        observeViewModel()

        nameEditText.addTextChangedListener {
            viewModel.onNameChanged(it?.toString().orEmpty())
        }

        descriptionEditText.addTextChangedListener {
            viewModel.onDescriptionChanged(it?.toString().orEmpty())
        }

        createButton.setOnClickListener { viewModel.onCreateButtonClicked() }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            if (nameEditText.text.toString() != state.name) {
                nameEditText.setText(state.name)
                nameEditText.setSelection(state.name.length)
            }

            if (descriptionEditText.text.toString() != state.description) {
                descriptionEditText.setText(state.description)
                descriptionEditText.setSelection(state.description.length)
            }

            createButton.isEnabled = state.isCreateButtonEnabled

            state.createdPlaylistName?.let { playlistName ->
                viewModel.onPlaylistCreatedHandled()
                val resultIntent = Intent().apply {
                    putExtra(PLAYLIST_NAME_EXTRA, playlistName)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    companion object {
        const val PLAYLIST_NAME_EXTRA = "playlist_name_extra"
    }
}
