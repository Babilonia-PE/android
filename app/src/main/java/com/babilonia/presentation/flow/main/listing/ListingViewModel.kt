package com.babilonia.presentation.flow.main.listing

import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.usecase.*
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

const val DESCRIPTION = "description"
const val ID = "id"

class ListingViewModel @Inject constructor(
    private val getListingUseCase: GetListingUseCase,
    private val createListingUseCase: CreateListingUseCase,
    private val listingActionUseCase: ListingActionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val updateListingUseCase: UpdateListingUseCase
) : BaseViewModel(), CompoundButton.OnCheckedChangeListener {

    val listingLiveData = MutableLiveData<Listing>()
    val contactOwnerLiveData = SingleLiveEvent<String>()
    val userIdLiveData = MutableLiveData<Long>()
    val onBackPressedLiveData = SingleLiveEvent<Unit>()
    val listingCreatedLiveData = SingleLiveEvent<Unit>()
    var listingId = EmptyConstants.EMPTY_LONG
    var displayMode = ListingDisplayMode.IMPROPER_LISTING

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

    }

    fun resetListingId() {
        listingId = EmptyConstants.EMPTY_LONG
    }

    fun updateListing(params: Listing) {
        updateListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(updatedListing: Listing) {
                listingLiveData.value = updatedListing
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
                e.printStackTrace()
            }

        }, params)
    }

    fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
                userIdLiveData.value = userId
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun navigateToDescription(description: String) {
        val bundle = bundleOf(DESCRIPTION to description)
        navigate(R.id.action_listingFragment_to_aboutListingFragment, bundle)
    }

    fun navigateToGallery() {
        val bundle = bundleOf(ID to listingId)
        navigate(R.id.action_listingFragment_to_galleryFragment, bundle)
    }

    fun navigateToFullscreenMap() {
        val bundle = bundleOf(ID to listingId)
        navigate(R.id.action_listingFragment_to_listingFullscreenMapFragment, bundle)
    }

    fun navigateToEdit() {
        listingLiveData.value?.id?.let {
            navigate(
                R.id.action_listingFragment_to_createListingContainerFragment2,
                bundleOf("id" to it, "mode" to NewListingOpenMode.EDIT)
            )
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        val mode = if (isFavorite) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }
        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(listingLiveData.value?.id, ListingAction.FAVOURITE, mode))
    }

    fun contactOwner() {
        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                listingLiveData.value?.user?.phoneNumber?.let {
                    contactOwnerLiveData.value = it
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(
            listingLiveData.value?.id,
            ListingAction.CONTACT_VIEW,
            ListingActionMode.SET
        ))
    }

    fun getListing(mode: ListingDisplayMode) {
        getListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(listing: Listing) {
                listingLiveData.value = listing
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, GetListingUseCase.Params(listingId, mode))
    }

    fun navigateToListOfListings(currentDisplayMode: ListingDisplayMode) {
        resetListingId()
        navigate(ListingFragmentDirections.actionListingFragmentToMyListingsFragment(currentDisplayMode))
    }

    fun onExitClicked() {
        createListing()
    }

    fun onBackPressed() {
        onBackPressedLiveData.call()
    }

    fun createListing(exitAfterSaving: Boolean = true) {
        listingLiveData.value?.let {
            createListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
                override fun onSuccess(listing: Listing) {
                    if (exitAfterSaving) {
                        navigateToListOfListings(ListingDisplayMode.UNPUBLISHED)
                    } else {
                        listing.id?.let { listingId = it }
                        listingLiveData.value = listing
                        listingCreatedLiveData.call()
                    }
                }

                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                }
            }, it)
        }

    }
}
