package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
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
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var searchHistory: SearchHistory

    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var placeholderSubtext: TextView
    private lateinit var retryButton: Button

    private var searchText = ""
    private var lastSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_poisk)

        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupViews()
        setupBackButton()
        setupRecyclerViews()
        setupSearchLogic()
        setupRetryButton()
        setupClearHistoryButton()

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
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

        searchHistory = SearchHistory(
            getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
        )
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        trackAdapter = TrackAdapter(emptyList()) { track ->
            searchHistory.add(track)
            showHistory()
        }

        historyAdapter = TrackAdapter(emptyList()) { track ->
            searchHistory.add(track)
            showHistory()
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
                updateClearButtonVisibility(currentText)

                if (currentText.isBlank()) {
                    hideAllStates()
                    showHistory()
                } else {
                    historyContainer.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN

            if (isDoneAction || isEnterKey) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        val focusAndShow = View.OnClickListener {
            focusAndShowKeyboard()
        }

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
            searchEditText.setText("")
            searchText = ""
            hideKeyboard()
            searchEditText.clearFocus()
            updateClearButtonVisibility("")
            hideAllStates()
        }
    }

    private fun setupRetryButton() {
        retryButton.setOnClickListener {
            if (lastSearchQuery.isNotBlank()) {
                performSearch(lastSearchQuery)
            }
        }
    }

    private fun setupClearHistoryButton() {
        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
            historyAdapter.updateTracks(emptyList())
            historyContainer.visibility = View.GONE
        }
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        historyContainer.visibility = View.GONE

        NetworkClient.iTunesApi.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (!response.isSuccessful) {
                    showConnectionError()
                    return
                }

                val foundTracks = response.body()?.results
                    ?.map { TrackMapper.map(it) }
                    ?.filter { it.trackName.isNotBlank() || it.artistName.isNotBlank() }
                    ?: emptyList()

                if (foundTracks.isEmpty()) {
                    showNothingFound()
                } else {
                    showTracks(foundTracks)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showConnectionError()
            }
        })
    }

    private fun showTracks(tracks: List<Track>) {
        historyContainer.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.VISIBLE
        trackAdapter.updateTracks(tracks)
    }

    private fun showHistory() {
        val historyTracks = searchHistory.read()

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
            if (!searchEditText.isFocused) {
                searchEditText.requestFocus()
            }
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
    }
}