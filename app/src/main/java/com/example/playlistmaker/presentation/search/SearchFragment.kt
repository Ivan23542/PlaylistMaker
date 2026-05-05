package com.example.playlistmaker.presentation.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.activity_poisk) {

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var searchFieldContainer: View

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var historyContainer: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var placeholderSubtext: TextView
    private lateinit var retryButton: Button

    private lateinit var searchProgressBar: ProgressBar

    private val clickHandler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top)
            insets
        }

        setupViews(view)
        setupRecyclerViews()
        observeViewModel()
        setupSearchLogic()
        setupRetryButton()
        setupClearHistoryButton()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHistory()
    }

    override fun onDestroyView() {
        clickHandler.removeCallbacksAndMessages(null)
        tracksRecyclerView.adapter = null
        historyRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun setupViews(root: View) {
        searchEditText = root.findViewById(R.id.search_edit_text)
        clearButton = root.findViewById(R.id.crest)
        searchIcon = root.findViewById(R.id.search_icon)
        searchFieldContainer = root.findViewById(R.id.search_field_container)
        tracksRecyclerView = root.findViewById(R.id.tracksRecyclerView)
        historyContainer = root.findViewById(R.id.historyContainer)
        historyRecyclerView = root.findViewById(R.id.historyRecyclerView)
        clearHistoryButton = root.findViewById(R.id.clearHistoryButton)
        placeholderContainer = root.findViewById(R.id.placeholderContainer)
        placeholderImage = root.findViewById(R.id.placeholderImage)
        placeholderText = root.findViewById(R.id.placeholderText)
        placeholderSubtext = root.findViewById(R.id.placeholderSubtext)
        retryButton = root.findViewById(R.id.retryButton)
        searchProgressBar = root.findViewById(R.id.searchProgressBar)
    }

    private fun setupRecyclerViews() {
        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                openPlayer(track)
            }
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                openPlayer(track)
            }
        }

        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = trackAdapter
        tracksRecyclerView.visibility = View.GONE

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter
        historyContainer.visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (searchEditText.text.toString() != state.searchText) {
                searchEditText.setText(state.searchText)
                searchEditText.setSelection(state.searchText.length)
            }

            clearButton.visibility = if (state.isClearButtonVisible) View.VISIBLE else View.GONE

            when (val contentState = state.contentState) {
                SearchContentState.Idle -> hideAllStates()
                SearchContentState.Loading -> showLoading()
                SearchContentState.NothingFound -> showNothingFound()
                SearchContentState.ConnectionError -> showConnectionError()
                is SearchContentState.SearchResults -> showTracks(contentState.tracks)
                is SearchContentState.History -> showHistory(contentState.tracks)
            }
        }
    }

    private fun setupSearchLogic() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchTextChanged(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isDoneAction || isEnterKey) {
                viewModel.onSearchSubmitted()
                true
            } else {
                false
            }
        }

        val focusAndShow = View.OnClickListener { focusAndShowKeyboard() }
        searchFieldContainer.setOnClickListener(focusAndShow)
        searchIcon.setOnClickListener(focusAndShow)
        searchEditText.setOnClickListener(focusAndShow)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
            viewModel.onSearchFocusChanged(hasFocus)
        }

        clearButton.setOnClickListener {
            viewModel.onClearClicked()
            hideKeyboard()
            searchEditText.clearFocus()
        }
    }

    private fun setupRetryButton() {
        retryButton.setOnClickListener { viewModel.onRetryClicked() }
    }

    private fun setupClearHistoryButton() {
        clearHistoryButton.setOnClickListener { viewModel.onClearHistoryClicked() }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            clickHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_searchFragment_to_playerFragment,
            bundleOf(PlayerFragment.ARG_TRACK to track)
        )
    }

    private fun showLoading() {
        historyContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        searchProgressBar.visibility = View.VISIBLE
    }

    private fun showTracks(tracks: List<Track>) {
        searchProgressBar.visibility = View.GONE
        historyContainer.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.VISIBLE
        trackAdapter.updateTracks(tracks)
    }

    private fun showHistory(tracks: List<Track>) {
        searchProgressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        historyContainer.visibility = View.VISIBLE
        historyAdapter.updateTracks(tracks)
    }

    private fun showNothingFound() {
        searchProgressBar.visibility = View.GONE
        historyContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.VISIBLE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
        placeholderSubtext.visibility = View.GONE
        retryButton.visibility = View.GONE
        placeholderImage.setImageResource(R.drawable.nothing_found)
        placeholderText.setText(R.string.nothing_found)
    }

    private fun showConnectionError() {
        searchProgressBar.visibility = View.GONE
        historyContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.VISIBLE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
        placeholderSubtext.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        placeholderImage.setImageResource(R.drawable.connection_error)
        placeholderText.setText(R.string.connection_error)
        placeholderSubtext.setText(R.string.connection_error_message)
    }

    private fun hideAllStates() {
        searchProgressBar.visibility = View.GONE
        tracksRecyclerView.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        historyContainer.visibility = View.GONE
        placeholderSubtext.visibility = View.GONE
        retryButton.visibility = View.GONE
        trackAdapter.updateTracks(emptyList())
    }

    private fun focusAndShowKeyboard() {
        searchEditText.post {
            if (!searchEditText.isFocused) {
                searchEditText.requestFocus()
            }
            showKeyboard()
        }
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
