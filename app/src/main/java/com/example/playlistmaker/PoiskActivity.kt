package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PoiskActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var searchIcon: ImageView
    private lateinit var searchFieldContainer: View
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private val tracks = arrayListOf(
        Track(
            "Smells Like Teen Spirit",
            "Nirvana",
            "5:01",
            "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Billie Jean",
            "Michael Jackson",
            "4:35",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
        ),
        Track(
            "Stayin' Alive",
            "Bee Gees",
            "4:10",
            "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Whole Lotta Love",
            "Led Zeppelin",
            "5:33",
            "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
        ),
        Track(
            "Sweet Child O'Mine",
            "Guns N' Roses",
            "5:03",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
        )
    )

    private var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_poisk)

        setupViews()
        setupBackButton()
        setupRecyclerView()
        setupSearchLogic()

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
            searchEditText.setText(searchText)
            updateClearButtonVisibility(searchText)
            filterTracks(searchText)
        } else {
            tracksRecyclerView.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        searchEditText.setText(searchText)
        updateClearButtonVisibility(searchText)
        filterTracks(searchText)
    }

    private fun setupViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.crest)
        searchIcon = findViewById(R.id.search_icon)
        searchFieldContainer = findViewById(R.id.search_field_container)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(emptyList())
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter
        tracksRecyclerView.visibility = View.GONE
    }

    private fun setupSearchLogic() {
        updateClearButtonVisibility(searchEditText.text.toString())

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s?.toString() ?: ""
                searchText = currentText
                updateClearButtonVisibility(currentText)
                filterTracks(currentText)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val focusAndShow = View.OnClickListener {
            focusAndShowKeyboard()
        }

        searchFieldContainer.setOnClickListener(focusAndShow)
        searchIcon.setOnClickListener(focusAndShow)
        searchEditText.setOnClickListener(focusAndShow)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            searchText = ""
            hideKeyboard()
            searchEditText.clearFocus()
            updateClearButtonVisibility("")
            filterTracks("")
        }
    }

    private fun filterTracks(text: String) {
        if (text.isBlank()) {
            tracksRecyclerView.visibility = View.GONE
            trackAdapter.updateTracks(emptyList())
            return
        }

        val filteredTracks = tracks.filter {
            it.trackName.contains(text, ignoreCase = true) ||
                    it.artistName.contains(text, ignoreCase = true)
        }

        if (filteredTracks.isEmpty()) {
            tracksRecyclerView.visibility = View.GONE
        } else {
            tracksRecyclerView.visibility = View.VISIBLE
        }

        trackAdapter.updateTracks(filteredTracks)
    }

    private fun updateClearButtonVisibility(text: String) {
        clearButton.visibility = if (text.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
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