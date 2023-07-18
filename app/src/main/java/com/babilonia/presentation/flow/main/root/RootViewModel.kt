package com.babilonia.presentation.flow.main.root

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import com.babilonia.R
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.listing.common.ListingDisplayMode
import com.babilonia.presentation.flow.main.publish.createlisting.ID
import com.babilonia.presentation.flow.main.publish.createlisting.MODE
import com.babilonia.presentation.utils.deeplink.DeeplinkHandler
import com.babilonia.presentation.utils.deeplink.DeeplinkNavigationConstants
import javax.inject.Inject

class RootViewModel @Inject constructor(
    private val deeplinkHandler: DeeplinkHandler
) : BaseViewModel() {

    private val navigateToProfileLiveData = SingleLiveEvent<Unit>()

    fun checkDeeplinks() {
        if (deeplinkHandler.hasDeeplink()) {
            val deeplink = deeplinkHandler.getLink()
            deeplink?.let { link ->
                when (link.destination) {
                    DeeplinkNavigationConstants.TO_LISTING -> {
                        deeplinkHandler.clearLink()
                        navigateToListing(link.listingId)
                    }
                    DeeplinkNavigationConstants.TO_USER_PROFILE -> {
                        deeplinkHandler.clearLink()
                        navigateToProfileLiveData.call()
                    }
                    DeeplinkNavigationConstants.TO_PRIVACY_POLICY -> {
                        navigateToProfileLiveData.call()
                    }
                    else -> {
                        deeplinkHandler.clearLink()
                    }
                }
            }
        }
    }

    private fun navigateToListing(listingId: Long) {
        navigateGlobal(
            R.id.action_global_listingFragment,
            bundleOf(
                ID to listingId,
                MODE to ListingDisplayMode.IMPROPER_LISTING
            )
        )
    }

    fun getNavigateToProfileLiveData(): LiveData<Unit> = navigateToProfileLiveData
}
