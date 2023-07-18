package com.babilonia.data.model.ar.tag

import com.babilonia.EmptyConstants
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.geo.ILocation

open class ArObject(val listing: Listing) {

    var location: ILocation = listing.locationAttributes
    var id = listing.id ?: EmptyConstants.EMPTY_LONG

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArObject) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}