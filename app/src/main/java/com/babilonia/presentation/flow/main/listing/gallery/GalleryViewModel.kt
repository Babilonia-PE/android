package com.babilonia.presentation.flow.main.listing.gallery

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.usecase.GetListingUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class GalleryViewModel @Inject constructor(
    private val getListingUseCase: GetListingUseCase
) : BaseViewModel() {

    private val listingLiveData = MutableLiveData<Listing>()

    fun getListing(listingId: Long) {
        getListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(listing: Listing) {
                listingLiveData.value = listing
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, GetListingUseCase.Params(listingId, ListingDisplayMode.IMPROPER_LISTING, true))
    }

    fun onPictureClicked(index: Int) {
        listingLiveData.value?.let {
            val bundle = bundleOf(LISTING_ID to it.id, IMAGE_INDEX to index)
            navigate(R.id.action_galleryFragment_to_galleryPhotoFragment, bundle)
        }
    }

    fun getListingLiveData(): LiveData<Listing> = listingLiveData

    companion object {
        private const val LISTING_ID = "listing_id"
        private const val IMAGE_INDEX = "image_index"
    }
}