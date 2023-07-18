package com.babilonia.presentation.flow.main.listing.gallery

import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.babilonia.R
import com.babilonia.databinding.FragmentGalleryBinding
import com.babilonia.presentation.base.BaseFragment

class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val args: GalleryFragmentArgs by navArgs()
    private val galleryAdapter = GalleryRecyclerAdapter {  clickedItemIndex ->
        viewModel.onPictureClicked(clickedItemIndex)
    }

    override fun viewCreated() {
        observeViewModel()
        initToolbar()
        initRecycler()
        viewModel.getListing(args.id)
    }

    private fun observeViewModel() {
        viewModel.getListingLiveData().observe(this, Observer { listing ->
            val photosCount = listing.images?.size ?: 0
            binding.tvPhotosCount.text = resources.getQuantityString(R.plurals.photos_plural, photosCount, photosCount)
            listing.images?.let { galleryAdapter.setItems(it) }
        })
    }

    private fun initRecycler() {
        binding.rvGallery.apply {
            adapter = galleryAdapter
            val gridLayoutManager = GridLayoutManager(context, 2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (galleryAdapter.isLargeItem(position)) {
                        2
                    } else {
                        1
                    }
                }
            }
            layoutManager = gridLayoutManager
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener { viewModel.navigateBack() }
    }
}