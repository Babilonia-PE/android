package com.babilonia.presentation.flow.main.listing.gallery.photo

import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.babilonia.databinding.FragmentGalleryPhotoBinding
import com.babilonia.domain.model.Listing
import com.babilonia.presentation.base.BaseFragment

class GalleryPhotoFragment : BaseFragment<FragmentGalleryPhotoBinding, GalleryPhotoViewModel>() {

    private val args: GalleryPhotoFragmentArgs by navArgs()

    override fun viewCreated() {
        observeViewModel()
        setClicks()
        viewModel.getListing(args.listingId)
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.authFailedData.removeObservers(this)
    }

    private fun observeViewModel() {
        viewModel.getListingLiveData().observe(this, Observer { listing ->
            initImagesAdapter(listing)
            setPhotoCounterText(args.imageIndex, listing.images?.size ?: 0)
        })
    }

    private fun setClicks() {
        binding.btnBack.setOnClickListener { viewModel.navigateBack() }
    }

    private fun initImagesAdapter(listing: Listing) {
        binding.vpListingImages.adapter = GalleryPhotoPagerAdapter(listing.images)
        binding.vpListingImages.setCurrentItem(args.imageIndex, false)
        binding.vpListingImages.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                setPhotoCounterText(position, listing.images?.size ?: 0)
            }
        })
    }

    private fun setPhotoCounterText(position: Int, totalCount: Int) {
        binding.tvPhotoCounter.text = "${position + 1}/$totalCount"
    }
}