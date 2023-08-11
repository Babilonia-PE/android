package com.babilonia.presentation.flow.main.listing

import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.usecase.*
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.mylistings.MyListingsFragmentDirections
import com.babilonia.presentation.flow.main.publish.mylistings.common.NewListingOpenMode
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

const val DESCRIPTION = "description"
const val ID = "id"
const val ACTIVE_TAB = "activeTab"

class ListingViewModel @Inject constructor(
    private val getListingUseCase: GetListingUseCase,
    private val createListingUseCase: CreateListingUseCase,
    private val listingActionUseCase: ListingActionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val updateListingUseCase: UpdateListingUseCase,
    private val sendActionUseCase: SendActionUseCase
) : BaseViewModel(), CompoundButton.OnCheckedChangeListener {
    val authFailedData = SingleLiveEvent<Unit>()
    val listingLiveData = MutableLiveData<Listing>()
    val userIdLiveData = MutableLiveData<Long>()
    val onBackPressedLiveData = SingleLiveEvent<Unit>()
    val listingCreatedLiveData = SingleLiveEvent<Unit>()
    var listingId = EmptyConstants.EMPTY_LONG
    var displayMode = ListingDisplayMode.IMPROPER_LISTING
    val viewLoadedData = MutableLiveData<Boolean>(false)
    var ipAddress = ""

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
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else {
                    dataError.postValue(e)
                    e.printStackTrace()
                }
            }

        }, params)
    }

    fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
                if (userIdLiveData.value != userId)
                    userIdLiveData.value = userId
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
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
        listingActionUseCase.execute(
            object : DisposableCompletableObserver() {
                override fun onComplete() {
                }

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else
                        dataError.postValue(e)
                }

            }, ListingActionUseCase.Params(
                listingLiveData.value?.id,
                ListingAction.FAVOURITE,
                mode,
                ipAddress,
                "android",
                "email"
            )
        )
    }

    fun contactOwner() {
        listingActionUseCase.execute(
            object : DisposableCompletableObserver() {
                override fun onComplete() {}

                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else
                        dataError.postValue(e)
                }

            }, ListingActionUseCase.Params(
                listingLiveData.value?.id,
                ListingAction.PHONE_VIEW,
                ListingActionMode.SET,
                ipAddress,
                "android",
                "email"
            )
        )
    }

    /*fun triggerView() {
        listingActionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() { }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(
            listingLiveData.value?.id,
            ListingAction.VIEWS_VIEW,
            ListingActionMode.SET
        ))
    }*/

    fun getListing(mode: ListingDisplayMode) {
        getListingUseCase.execute(object : DisposableSingleObserver<Listing>() {
            override fun onSuccess(listing: Listing) {
//                val firstTime = listingLiveData.value == null
                listingLiveData.value = listing
//                if(firstTime)
//                    triggerView()
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(e)
            }

        }, GetListingUseCase.Params(listingId, mode))
    }

    fun navigateToListOfListings(currentDisplayMode: ListingDisplayMode) {
        resetListingId()
//        navigate(ListingFragmentDirections.actionListingFragmentToMyListingsFragment(currentDisplayMode))
        val bundle = bundleOf(ACTIVE_TAB to currentDisplayMode)
        navigate(R.id.action_listingFragment_to_myListingsFragment, bundle)
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
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else
                        dataError.postValue(e)
                }
            }, it)
        }

    }

    fun onWhatsappClicked(id: Long) {
        sendActionUseCase.execute(
            object : DisposableCompletableObserver() {
                override fun onComplete() {}

                override fun onError(e: Throwable) {
                    //dataError.postValue(e)
                }
            }, SendActionUseCase.Params(
                id,
                ListingAction.WHATSAPP_VIEW,
                ipAddress,
                "android",
                "email"
            )
        )
    }
}