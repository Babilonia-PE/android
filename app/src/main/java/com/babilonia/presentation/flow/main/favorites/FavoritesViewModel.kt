package com.babilonia.presentation.flow.main.favorites

import androidx.lifecycle.MutableLiveData
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.ListingAction
import com.babilonia.domain.model.enums.ListingActionMode
import com.babilonia.domain.usecase.GetFavouritesUseCase
import com.babilonia.domain.usecase.ListingActionUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.flow.main.common.ListingActionsListener
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.subscribers.DisposableSubscriber
import javax.inject.Inject

class FavoritesViewModel @Inject constructor(
    private val getFavouritesUseCase: GetFavouritesUseCase,
    private val actionUseCase: ListingActionUseCase
) : BaseViewModel(), ListingActionsListener {
    val favouritesLiveData = MutableLiveData<List<Listing>>()

    override fun onFavouriteClicked(isChecked: Boolean, id: Long) {
        val mode = if (isChecked) {
            ListingActionMode.SET
        } else {
            ListingActionMode.DELETE
        }

        actionUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                getFavourites()
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, ListingActionUseCase.Params(id, ListingAction.FAVOURITE, mode))
    }

    override fun onPreviewClicked(id: Long) {
        navigate(FavoritesFragmentDirections.actionGlobalListingFragment(id, ListingDisplayMode.IMPROPER_LISTING))
    }

    fun getFavourites() {
        getFavouritesUseCase.execute(object : DisposableSubscriber<List<Listing>>() {
            override fun onComplete() {

            }

            override fun onNext(listings: List<Listing>?) {
                favouritesLiveData.postValue(listings)
            }

            override fun onError(t: Throwable?) {
                dataError.postValue(t)
            }

        }, Unit)
    }


}
