package com.example.playlistmaker.presentation.search

import android.content.Intent
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerActivity

class PoiskActivity : AppCompatActivity() {

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


    private lateinit var searchHistoryInteractor: SearchHistoryInteractor
    private lateinit var tracksInteractor: TracksInteractor

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var placeholderSubtext: TextView
    private lateinit var retryButton: Button

    private lateinit var searchProgressBar: ProgressBar

    private val handler = Handler(Looper.getMainLooper())

    private var searchText = ""
    private var lastSearchQuery = ""
    private var latestSearchText = ""
    private var isClickAllowed = true

    private val searchRunnable = Runnable {
        val query = latestSearchText.trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_poisk)

        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }


        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)
        tracksInteractor = Creator.provideTracksInteractor()

        setupViews()
        setupBackButton()
        setupRecyclerViews()
        setupSearchLogic()
        setupRetryButton()
        setupClearHistoryButton()

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
            latestSearchText = searchText
            searchEditText.setText(searchText)
            updateClearButtonVisibility(searchText)

            if (searchText.isNotBlank()) {
                performSearch(searchText)
            } else {
                hideAllStates()
                showHistory()
            }
        } else {
            hideAllStates()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
    }

    private fun setupViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.crest)
        searchIcon = findViewById(R.id.search_icon)
        searchFieldContainer = findViewById(R.id.search_field_container)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        placeholderContainer = findViewById(R.id.placeholderContainer)
        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        placeholderSubtext = findViewById(R.id.placeholderSubtext)
        retryButton = findViewById(R.id.retryButton)
        searchProgressBar = findViewById(R.id.searchProgressBar)
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.add(track)
                openPlayer(track)
            }
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.add(track)
                openPlayer(track)
            }
        }

        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter
        tracksRecyclerView.visibility = View.GONE

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyContainer.visibility = View.GONE
    }

    private fun setupSearchLogic() {
        updateClearButtonVisibility(searchEditText.text.toString())

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s?.toString() ?: ""
                searchText = currentText
                latestSearchText = currentText
                updateClearButtonVisibility(currentText)
                handler.removeCallbacks(searchRunnable)

                if (currentText.isBlank()) {
                    hideAllStates()
                    showHistory()
                } else {
                    historyContainer.visibility = View.GONE
                    tracksRecyclerView.visibility = View.GONE
                    placeholderContainer.visibility = View.GONE
                    searchProgressBar.visibility = View.GONE
                    handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isDoneAction || isEnterKey) {
                val query = searchEditText.text.toString().trim()
                handler.removeCallbacks(searchRunnable)
                if (query.isNotEmpty()) {
                    latestSearchText = query
                    performSearch(query)
                }
                true
            } else false
        }

        val focusAndShow = View.OnClickListener { focusAndShowKeyboard() }
        searchFieldContainer.setOnClickListener(focusAndShow)
        searchIcon.setOnClickListener(focusAndShow)
        searchEditText.setOnClickListener(focusAndShow)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
                showHistory()
            } else {
                historyContainer.visibility = View.GONE
            }
        }

        clearButton.setOnClickListener {
            handler.removeCallbacks(searchRunnable)
            searchEditText.setText("")
            searchText = ""
            latestSearchText = ""
            hideKeyboard()
            searchEditText.clearFocus()
            updateClearButtonVisibility("")
            hideAllStates()
        }
    }

    private fun setupRetryButton() {
        retryButton.setOnClickListener {
            if (lastSearchQuery.isNotBlank()) performSearch(lastSearchQuery)
        }
    }

    private fun setupClearHistoryButton() {
        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clear()
            historyAdapter.updateTracks(emptyList())
            historyContainer.visibility = View.GONE
        }
    }


    private fun performSearch(query: String) {
        lastSearchQuery = query
        showLoading()

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                handler.post {
                    if (query != latestSearchText.trim()) return@post

                    if (errorMessage != null) {
                        showConnectionError()
                    } else if (foundTracks.isNullOrEmpty()) {
                        showNothingFound()
                    } else {
                        showTracks(foundTracks)
                    }
                }
            }
        })
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(TRACK_EXTRA, track)
        startActivity(intent)
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

    private fun showHistory() {
        searchProgressBar.visibility = View.GONE
        val historyTracks = searchHistoryInteractor.read()

        if (searchEditText.text.isEmpty() && searchEditText.hasFocus() && historyTracks.isNotEmpty()) {
            tracksRecyclerView.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
            historyContainer.visibility = View.VISIBLE
            historyAdapter.updateTracks(historyTracks)
        } else {
            historyContainer.visibility = View.GONE
        }
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

    private fun updateClearButtonVisibility(text: String) {
        clearButton.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun focusAndShowKeyboard() {
        searchEditText.post {
            if (!searchEditText.isFocused) searchEditText.requestFocus()
            showKeyboard()
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "search_text"
        const val TRACK_EXTRA = "track_extra"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}