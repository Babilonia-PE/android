package com.babilonia.presentation.flow.main.privacy

import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.babilonia.R
import com.babilonia.databinding.FragmentPrivacyBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.common.PrivacyContentType

class PrivacyFragment : BaseFragment<FragmentPrivacyBinding, PrivacyViewModel>() {

    private val args: PrivacyFragmentArgs by navArgs()

    override fun viewCreated() {
        setToolbar()
        observeViewModel()
        viewModel.getPrivacyUrl(args.contentType)
    }

    private fun observeViewModel() {
        viewModel.getPrivacyUrlLiveData().observe(this, Observer { setWebViewUrl(it) })
    }

    private fun setWebViewUrl(url: String) {
        binding.wvPrivacy.apply {
            isVerticalScrollBarEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            webViewClient = WebViewClient()

            loadUrl(url)
        }
    }

    private fun setToolbar() {
        binding.toolbar.title = if (args.contentType == PrivacyContentType.TERMS_AND_CONDITIONS) {
            getString(R.string.terms_and_conditions)
        } else {
            getString(R.string.privacy_policy)
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}