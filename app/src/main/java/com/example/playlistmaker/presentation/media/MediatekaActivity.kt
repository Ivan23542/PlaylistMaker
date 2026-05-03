package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediatekaActivity : AppCompatActivity() {

    private val viewModel: MediatekaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mediateka)
        viewModel

        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupBackButton()
        setupViewPager()
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.strelka_Mediateka)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        val tabLayout = findViewById<TabLayout>(R.id.mediaTabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.mediaViewPager)
        val tabTextColor = ContextCompat.getColor(this, R.color.media_tab_text_color)
        val titles = listOf(
            getString(R.string.favorite_tracks_tab),
            getString(R.string.playlists_tab)
        )

        tabLayout.setSelectedTabIndicatorColor(
            ContextCompat.getColor(this, R.color.media_tab_indicator_color)
        )
        tabLayout.setTabTextColors(tabTextColor, tabTextColor)
        viewPager.adapter = MediatekaPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}
