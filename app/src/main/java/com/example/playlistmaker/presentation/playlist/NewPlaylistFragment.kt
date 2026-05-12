package com.example.playlistmaker.presentation.playlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment(R.layout.activity_new_playlist) {

    private val viewModel: NewPlaylistViewModel by viewModel()

    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var imageContainer: View
    private lateinit var coverImageView: ImageView
    private lateinit var titleTextView: TextView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onCoverSelected(it.toString()) }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleCloseRequest()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupViews(view)
        loadEditPlaylistIfNeeded()
        restoreSavedState(savedInstanceState)
        observeViewModel()
        setupListeners(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value ?: return
        outState.putString(STATE_NAME, currentState.name)
        outState.putString(STATE_DESCRIPTION, currentState.description)
        outState.putString(STATE_COVER_URI, currentState.coverUri)
    }

    private fun setupViews(root: View) {
        titleTextView = root.findViewById(R.id.titleTextView)
        imageContainer = root.findViewById(R.id.imageContainer)
        coverImageView = root.findViewById(R.id.coverImageView)
        nameEditText = root.findViewById(R.id.playlistNameEditText)
        descriptionEditText = root.findViewById(R.id.playlistDescriptionEditText)
        createButton = root.findViewById(R.id.createButton)
    }

    private fun setupListeners(root: View) {
        root.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            handleCloseRequest()
        }

        imageContainer.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        nameEditText.addTextChangedListener {
            viewModel.onNameChanged(it?.toString().orEmpty())
        }

        descriptionEditText.addTextChangedListener {
            viewModel.onDescriptionChanged(it?.toString().orEmpty())
        }

        createButton.setOnClickListener {
            viewModel.onCreateButtonClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (nameEditText.text?.toString() != state.name) {
                nameEditText.setText(state.name)
                nameEditText.setSelection(state.name.length)
            }

            if (descriptionEditText.text?.toString() != state.description) {
                descriptionEditText.setText(state.description)
                descriptionEditText.setSelection(state.description.length)
            }

            createButton.isEnabled = state.isCreateButtonEnabled
            titleTextView.setText(
                if (state.isEditMode) R.string.edit_playlist_title else R.string.new_playlist
            )
            createButton.setText(
                if (state.isEditMode) R.string.save_playlist_button else R.string.create
            )
            renderCover(state.coverUri)

            state.createdPlaylistName?.let { playlistName ->
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(RESULT_PLAYLIST_CREATED, playlistName)
                viewModel.onPlaylistCreatedHandled()
                findNavController().navigateUp()
            }

            state.savedPlaylistName?.let {
                viewModel.onPlaylistCreatedHandled()
                findNavController().navigateUp()
            }
        }
    }

    private fun renderCover(coverUri: String?) {
        if (coverUri.isNullOrBlank()) {
            imageContainer.setBackgroundResource(R.drawable.playlist_image_placeholder_bg)
            coverImageView.visibility = View.GONE
            coverImageView.setImageDrawable(null)
            return
        }

        imageContainer.background = null
        coverImageView.visibility = View.VISIBLE

        Glide.with(this)
            .load(coverUri)
            .centerCrop()
            .transform(
                RoundedCorners(
                    resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)
                )
            )
            .into(coverImageView)
    }

    private fun handleCloseRequest() {
        val currentState = viewModel.uiState.value ?: NewPlaylistUiState()
        if (currentState.isEditMode) {
            findNavController().navigateUp()
            return
        }

        if (!currentState.hasUnsavedChanges()) {
            findNavController().navigateUp()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.finish_playlist_creation_title)
            .setMessage(R.string.finish_playlist_creation_message)
            .setNegativeButton(R.string.cancel_dialog, null)
            .setPositiveButton(R.string.finish_dialog) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        val currentState = viewModel.uiState.value ?: return
        if (currentState.hasUnsavedChanges()) return

        val restoredName = savedInstanceState?.getString(STATE_NAME).orEmpty()
        val restoredDescription = savedInstanceState?.getString(STATE_DESCRIPTION).orEmpty()
        val restoredCoverUri = savedInstanceState?.getString(STATE_COVER_URI)

        if (restoredName.isBlank() && restoredDescription.isBlank() && restoredCoverUri.isNullOrBlank()) {
            return
        }

        viewModel.restoreState(restoredName, restoredDescription, restoredCoverUri)
    }

    private fun loadEditPlaylistIfNeeded() {
        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID, NO_PLAYLIST_ID) ?: NO_PLAYLIST_ID
        val isEditMode = arguments?.getBoolean(ARG_EDIT_MODE, false) ?: false
        if (isEditMode && playlistId != NO_PLAYLIST_ID) {
            viewModel.loadPlaylistForEdit(playlistId)
        }
    }

    companion object {
        const val RESULT_PLAYLIST_CREATED = "result_playlist_created"
        const val ARG_PLAYLIST_ID = "playlistId"
        const val ARG_EDIT_MODE = "editMode"

        private const val STATE_NAME = "state_name"
        private const val STATE_DESCRIPTION = "state_description"
        private const val STATE_COVER_URI = "state_cover_uri"
        private const val NO_PLAYLIST_ID = -1L
    }
}
