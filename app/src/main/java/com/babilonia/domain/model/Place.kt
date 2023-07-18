package com.babilonia.domain.model

// Created by Anton Yatsenko on 17.07.2019.
data class Place(var id: String, var title: String) {
    // Clicking on item in AutoCompleteTextView sets clicked item 'toString()' result in
    // SearchView's input field. So we want SearchView to set the right text.
    override fun toString(): String {
        return title
    }
}