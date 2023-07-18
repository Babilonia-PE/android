package com.babilonia.presentation.flow.main.publish.common

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingSharedViewModel
import com.dhabensky.scopedvm.ScopedViewModelProviders

// Created by Anton Yatsenko on 18.06.2019.
private const val CREATE_LISTING_SCOPE = "create_listing_scope"

abstract class BaseCreateListingFragment<B : ViewDataBinding, VM : BaseViewModel> : BaseFragment<B, VM>() {
    lateinit var sharedViewModel: CreateListingSharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ScopedViewModelProviders.forScope(this, CREATE_LISTING_SCOPE)
            .of(requireActivity()).get(CreateListingSharedViewModel::class.java)
    }
}