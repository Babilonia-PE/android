package com.babilonia.presentation.flow.main.listing.common.about

import android.text.method.ScrollingMovementMethod
import androidx.navigation.fragment.navArgs
import com.babilonia.R
import com.babilonia.databinding.AboutListingFragmentBinding
import com.babilonia.presentation.base.BaseFragment

class AboutListingFragment : BaseFragment<AboutListingFragmentBinding, AboutListingViewModel>() {
    private val args: AboutListingFragmentArgs by navArgs()
    override fun viewCreated() {
        setToolbar()
        binding.etDescription.text = args.description
        binding.etDescription.movementMethod = ScrollingMovementMethod()
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}
