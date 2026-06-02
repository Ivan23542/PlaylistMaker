package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.uiState.observeAsState(SettingsUiState())

                LaunchedEffect(state.isDarkTheme) {
                    AppCompatDelegate.setDefaultNightMode(
                        if (state.isDarkTheme) {
                            AppCompatDelegate.MODE_NIGHT_YES
                        } else {
                            AppCompatDelegate.MODE_NIGHT_NO
                        }
                    )
                }

                SettingsScreen(
                    state = state,
                    onThemeCheckedChanged = viewModel::onThemeCheckedChanged,
                    onShareClicked = ::shareApp,
                    onSupportClicked = ::sendSupportEmail,
                    onAgreementClicked = ::openUserAgreement
                )
            }
        }
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

    private fun openUserAgreement() {
        val agreementUrl = resources.getString(R.string.agreementUrl)
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(agreementUrl)
        }
        startActivity(browserIntent)
    }
}
