package com.babilonia.presentation.flow.main.publish.mylistings

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.usecase.DeleteDraftUseCase
import com.babilonia.domain.usecase.GetMyListingsUseCase
import com.babilonia.domain.usecase.UpdateListingUseCase
import com.babilonia.presentation.base.ActionLiveData
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.mylistings.common.ListingNavigationListener
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import javax.inject.Inject

private const val ID = "id"
private const val MODE = "mode"

class MyListingsViewModel @Inject constructor(
    private val getMyListingsUseCase: GetMyListingsUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase,
    private val updateListingUseCase: UpdateListingUseCase
) :
    BaseViewModel(), ListingNavigationListener {


    val myListings = MutableLiveData<List<Listing>>()
    val onMoreClickedEvent = ActionLiveData<Listing>()
    val onListingUpdatedLiveData = SingleLiveEvent<Unit>()
    val onListingDeletedLiveData = SingleLiveEvent<Unit>()

    override fun onMenuClicked(listing: Listing) {
        onMoreClickedEvent.postValue(listing)
    }

    override fun onDraftClicked(id: Long?) {
        id?.let { navigateToCreateListing(it, NewListingOpenMode.DRAFT) }
    }

    override fun onMyListingClicked(id: Long?, status: String) {
        id?.let {
            if (status == Constants.HIDDEN) {
                navigate(MyListingsFragmentDirections.actionGlobalListingFragment(id, ListingDisplayMode.UNPUBLISHED))
            } else {
                navigate(MyListingsFragmentDirections.actionGlobalListingFragment(id, ListingDisplayMode.PUBLISHED))
            }
        }

    }

    fun deleteDraft(id: Long) {
        deleteDraftUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                onListingDeletedLiveData.call()
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, id)
    }

    fun getMyListings() {
        getMyListingsUseCase.execute(object : DisposableSubscriber<List<Listing>>() {
            override fun onComplete() {
            }

            override fun onNext(t: List<Listing>) {
                myListings.postValue(t)
            }

            override fun onError(t: Throwable?) {
                dataError.postValue(t)
            }

        }, Unit)
    }

    fun updateListing(params: Listing) {
        updateListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(updatedListing: Listing) {
                myListings.value?.let {  listings ->
                    listings.firstOrNull { it.id == updatedListing.id }?.setFrom(updatedListing)
                }
                onListingUpdatedLiveData.call()
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, params)
    }

    fun navigateToCreateListing(
        id: Long = EmptyConstants.EMPTY_LONG,
        mode: NewListingOpenMode = NewListingOpenMode.NEW
    ) {

        val bundle =
            bundleOf(ID to if (mode == NewListingOpenMode.NEW) System.currentTimeMillis() else id, MODE to mode)
        navigate(R.id.action_myListingsFragment_to_createListingContainerFragment2, bundle)
    }

    fun navigateToEdit(id: Long) {
        navigateToCreateListing(id, NewListingOpenMode.EDIT)
    }
}
