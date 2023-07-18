package com.babilonia.domain.model

class RecentSearch(
    val queryText: String,
    val placeId: String
) {
    // Clicking on item in AutoCompleteTextView sets clicked item 'toString()' result in
    // SearchView's input field. So we want SearchView to set the right text.
    override fun toString(): String {
        return queryText
    }
}