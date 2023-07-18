package com.babilonia.presentation.flow.main.listing.gallery.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.domain.model.Listing
import com.babilonia.domain.usecase.GetListingUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class GalleryPhotoViewModel @Inject constructor(
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

    fun getListingLiveData(): LiveData<Listing> = listingLiveData
}