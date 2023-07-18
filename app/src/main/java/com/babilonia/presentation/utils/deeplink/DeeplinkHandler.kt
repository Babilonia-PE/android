package com.babilonia.presentation.utils.deeplink

import android.net.Uri
import com.babilonia.EmptyConstants
import javax.inject.Inject

class DeeplinkHandler @Inject constructor() {

    private var currentDeeplink: Deeplink? = null

    fun onNewLink(link: Uri) {
        currentDeeplink = processLink(link)
    }

    fun hasDeeplink(): Boolean {
        return currentDeeplink?.destination != null
    }

    fun getLink(): Deeplink? {
        return currentDeeplink
    }

    fun clearLink() {
        currentDeeplink = null

    }

    private fun processLink(link: Uri): Deeplink? {
        val lastPathSegment = link.lastPathSegment
        return when (lastPathSegment) {
            DeeplinkNavigationConstants.TO_PRIVACY_POLICY,
            DeeplinkNavigationConstants.TO_USER_PROFILE -> {
                Deeplink(lastPathSegment)
            }
            DeeplinkNavigationConstants.TO_LISTINGS_LIST,
            DeeplinkNavigationConstants.TO_HOME,
            null -> {
                Deeplink(DeeplinkNavigationConstants.TO_LISTINGS_LIST)
            }
            else -> {
                if (link.toString().contains(DeeplinkNavigationConstants.TO_LISTING)) {
                    // this part is for links like https://web-qa.babilonia.io/listings/223
                    // if link contains '/listings/' then we are navigating to particular listing,
                    // in this case lastPathSegment must contain id of this listing
                    try {
                        // I decided to wrap it with try/catch block in case if something goes wrong
                        // and id is not parsed correctly
                        val listingId = lastPathSegment.toLong()
                        Deeplink(DeeplinkNavigationConstants.TO_LISTING, listingId)
                    } catch (e: Exception) {
                        Deeplink(DeeplinkNavigationConstants.TO_LISTINGS_LIST)
                    }
                } else {
                    Deeplink(DeeplinkNavigationConstants.TO_LISTINGS_LIST)
                }
            }
        }
    }

    class Deeplink(
        val destination: String? = null,
        val listingId: Long = EmptyConstants.EMPTY_LONG
    )
}