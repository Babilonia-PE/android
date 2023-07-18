package com.babilonia.presentation.flow.main.publish.description

import androidx.activity.addCallback
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.ListingDescriptionFragmentBinding
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel

class ListingDescriptionFragment :
    BaseCreateListingFragment<ListingDescriptionFragmentBinding, CreateListingContainerViewModel>() {
    private var previousText: String? = EmptyConstants.EMPTY_STRING
    override fun viewCreated() {
        binding.model = sharedViewModel
        previousText = sharedViewModel.description.value
        initToolbar()
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener {
            binding.etDescription.setText(previousText)
            viewModel.navigateBack()
        }
        binding.btDone.setOnClickListener {
            binding.etDescription.setText(binding.etDescription.text?.trim())
            viewModel.navigateBack()
        }
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback {
            binding.etDescription.setText(previousText)
            viewModel.navigateBack()
        }
    }
}
