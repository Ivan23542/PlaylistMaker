package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var themeSwitcher: Switch

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top)
            insets
        }

        setupThemeSwitcher(view)
        observeViewModel()
        setupShareButton(view)
        setupSupportButton(view)
        setupAgreementButton(view)
    }

    private fun setupThemeSwitcher(root: View) {
        themeSwitcher = root.findViewById(R.id.themeSwitcher)
        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.onThemeCheckedChanged(checked)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (themeSwitcher.isChecked != state.isDarkTheme) {
                themeSwitcher.isChecked = state.isDarkTheme
            }

            AppCompatDelegate.setDefaultNightMode(
                if (state.isDarkTheme) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    private fun setupShareButton(root: View) {
        val shareIconButton = root.findViewById<View>(R.id.button_settings_2)
        val shareTextView = root.findViewById<TextView>(R.id.button_settings_1)

        shareIconButton.setOnClickListener { shareApp() }
        shareTextView.setOnClickListener { shareApp() }
    }

    private fun shareApp() {
        val shareText = resources.getString(R.string.shareText)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val chooserIntent = Intent.createChooser(shareIntent, resources.getString(R.string.shareApp))
        if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(chooserIntent)
        }
    }

    private fun setupSupportButton(root: View) {
        val supportIconButton = root.findViewById<View>(R.id.support_2)
        val supportTextView = root.findViewById<TextView>(R.id.support_1)

        supportIconButton.setOnClickListener { sendSupportEmail() }
        supportTextView.setOnClickListener { sendSupportEmail() }
    }

    private fun sendSupportEmail() {
        val recipientEmail = resources.getString(R.string.email)
        val subject = resources.getString(R.string.subject)
        val body = resources.getString(R.string.body)

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(emailIntent)
    }

    private fun setupAgreementButton(root: View) {
        val agreementIconButton = root.findViewById<View>(R.id.agreement_icon)
        val agreementTextView = root.findViewById<TextView>(R.id.agreement_text)

        agreementIconButton.setOnClickListener { openUserAgreement() }
        agreementTextView.setOnClickListener { openUserAgreement() }
    }

    private fun openUserAgreement() {
        val agreementUrl = resources.getString(R.string.agreementUrl)
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(agreementUrl)
        }
        startActivity(browserIntent)
    }
}
