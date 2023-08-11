package com.babilonia.domain.model

import com.babilonia.presentation.utils.SvgUtil.concatString

class RecentSearch(
    val queryText: String,
    val placeId: String,
    val location: Location
) {
    // Clicking on item in AutoCompleteTextView sets clicked item 'toString()' result in
    // SearchView's input field. So we want SearchView to set the right text.
    override fun toString(): String {
        return concatString(location.address, location.district, location.province, location.department)
    }
}