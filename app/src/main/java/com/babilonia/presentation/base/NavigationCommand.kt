package com.babilonia.presentation.base

import android.os.Bundle
import androidx.navigation.NavDirections

// Created by Anton Yatsenko on 24.05.2019.
sealed class NavigationCommand {
    data class ToDestination(
        val directions: NavDirections,
        val bundle: Bundle? = null,
        val closePrevious: Boolean = false
    ) : NavigationCommand()

    data class ToDestinationGlobal(
        val directions: NavDirections,
        val bundle: Bundle? = null,
        val closePrevious: Boolean = false
    ) : NavigationCommand()

    data class ToScreen(val destinationId: Int, val bundle: Bundle? = null, val closePrevious: Boolean = false) :
        NavigationCommand()

    data class ToScreenGlobal(val destinationId: Int, val bundle: Bundle? = null, val closePrevious: Boolean = false) :
        NavigationCommand()

    object Back : NavigationCommand()
    data class BackTo(val destinationId: Int) : NavigationCommand()
    object SignOut : NavigationCommand()
}