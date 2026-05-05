package com.example.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediatekaFragment : Fragment(R.layout.activity_mediateka) {

    private val viewModel: MediatekaViewModel by viewModel()

    private var viewPager: ViewPager2? = null
    private var tabMediator: TabLayoutMediator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top)
            insets
        }

        setupViewPager(view)
    }

    override fun onDestroyView() {
        tabMediator?.detach()
        viewPager?.adapter = null
        viewPager = null
        tabMediator = null
        super.onDestroyView()
    }

    private fun setupViewPager(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.mediaTabLayout)
        val localViewPager = view.findViewById<ViewPager2>(R.id.mediaViewPager)
        val tabTextColor = ContextCompat.getColor(requireContext(), R.color.media_tab_text_color)
        val titles = listOf(
            getString(R.string.favorite_tracks_tab),
            getString(R.string.playlists_tab)
        )

        tabLayout.setSelectedTabIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.media_tab_indicator_color)
        )
        tabLayout.setTabTextColors(tabTextColor, tabTextColor)
        localViewPager.adapter = MediatekaPagerAdapter(this)
        viewPager = localViewPager

        tabMediator = TabLayoutMediator(tabLayout, localViewPager) { tab, position ->
            tab.text = titles[position]
        }.apply {
            attach()
        }
    }
}
