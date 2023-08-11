package com.babilonia.presentation.flow.main.favorites

import androidx.lifecycle.MutableLiveData
import com.babilonia.Constants
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.usecase.GetFavouritesUseCase
import com.babilonia.domain.usecase.GetUserIdUseCase
import com.babilonia.domain.usecase.ListingActionUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.common.ListingActionsListener
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import javax.inject.Inject

class FavoritesViewModel @Inject constructor(
    private val getFavouritesUseCase: GetFavouritesUseCase,
    private val actionUseCase: ListingActionUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : BaseViewModel(), ListingActionsListener {
    val authFailedData = SingleLiveEvent<Unit>()
    val favouritesLiveData = MutableLiveData<List<Listing>>()
    val userIdLiveData = MutableLiveData<Long>()
    var ipAddress = ""

    override fun onFavouriteClicked(isChecked: Boolean, id: Long) {
        val mode = if (isChecked) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }

        actionUseCase.execute(
            object : DisposableCompletableObserver() {
                override fun onComplete() {
                    getFavourites()
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
                id,
                ListingAction.FAVOURITE,
                mode,
                ipAddress,
                "android",
                "email"
            )
        )
    }

    override fun onPreviewClicked(listing: Listing) {
        val mode = if (listing.user?.id == userIdLiveData.value) {
            if (listing.status == Constants.HIDDEN)
                ListingDisplayMode.UNPUBLISHED
            else
                ListingDisplayMode.PUBLISHED
        } else {
            ListingDisplayMode.IMPROPER_LISTING
        }

        listing.id?.let { id ->
            navigate(
                FavoritesFragmentDirections.actionGlobalListingFragment(
                    id,
                    mode
                )
            )
        }
    }

    fun getFavourites() {
        getFavouritesUseCase.execute(object : DisposableSubscriber<List<Listing>>() {
            override fun onComplete() {

            }

            override fun onNext(listings: List<Listing>?) {
                favouritesLiveData.postValue(listings)
            }

            override fun onError(t: Throwable?) {
                if (t is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
                    dataError.postValue(t)
            }

        }, Unit)
    }

    fun getUserId() {
        getUserIdUseCase.execute(object : DisposableSingleObserver<Long>() {
            override fun onSuccess(userId: Long) {
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
}
