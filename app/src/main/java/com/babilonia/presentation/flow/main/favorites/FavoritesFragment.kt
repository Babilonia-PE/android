package com.babilonia.presentation.flow.main.favorites

import androidx.core.view.isInvisible
import androidx.lifecycle.Observer
import com.babilonia.databinding.FavoritesFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.common.ListingPreviewRecyclerAdapter
import com.babilonia.presentation.utils.NetworkUtil

class FavoritesFragment : BaseFragment<FavoritesFragmentBinding, FavoritesViewModel>() {
    private var adapter: ListingPreviewRecyclerAdapter? = null
    override fun viewCreated() {
        adapter = ListingPreviewRecyclerAdapter(viewModel)
        binding.rcFavorites.adapter = adapter
        viewModel.getUserId()
        viewModel.getFavourites()

        viewModel.ipAddress = NetworkUtil.getIPAddress(requireContext()) ?: ""
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.favouritesLiveData.observe(this, Observer {
            binding.emptyGroup.isInvisible = it.isNotEmpty()
            binding.rcFavorites.isInvisible = it.isEmpty()
            adapter?.clear()
            adapter?.addItems(it)
        })
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
        viewModel.authFailedData.removeObservers(this)
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.favouritesLiveData.removeObservers(this)
    }
}

