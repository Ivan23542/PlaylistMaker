package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.model.ThemeSettings

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }


        settingsInteractor = Creator.provideSettingsInteractor(this)

        setupBackButton()
        setupThemeSwitcher()
        setupShareButton()
        setupSupportButton()
        setupAgreementButton()
    }

    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.back)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupThemeSwitcher() {
        val themeSwitcher = findViewById<Switch>(R.id.themeSwitcher)


        val currentSettings = settingsInteractor.getThemeSettings()
        themeSwitcher.isChecked = currentSettings.isDarkTheme

        themeSwitcher.setOnCheckedChangeListener { _, checked ->

            settingsInteractor.updateThemeSetting(ThemeSettings(checked))

            AppCompatDelegate.setDefaultNightMode(
                if (checked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    private fun setupShareButton() {
        val shareIconButton = findViewById<ImageButton>(R.id.button_settings_2)
        val shareTextView = findViewById<TextView>(R.id.button_settings_1)

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
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(chooserIntent)
        }
    }

    private fun setupSupportButton() {
        val supportIconButton = findViewById<ImageButton>(R.id.support_2)
        val supportTextView = findViewById<TextView>(R.id.support_1)

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

    private fun setupAgreementButton() {
        val agreementIconButton = findViewById<ImageButton>(R.id.agreement_icon)
        val agreementTextView = findViewById<TextView>(R.id.agreement_text)

        agreementIconButton.setOnClickListener { openUserAgreement() }
        agreementTextView.setOnClickListener { openUserAgreement() }
    }

    private fun openUserAgreement() {
        val agreementURL = resources.getString(R.string.agreementUrl)
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(agreementURL)
        }
        startActivity(browserIntent)
    }
}