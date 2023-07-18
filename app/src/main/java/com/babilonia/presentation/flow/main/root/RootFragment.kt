package com.babilonia.presentation.flow.main.root

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.babilonia.R
import com.babilonia.databinding.RootFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.setupWithNavController
import com.babilonia.presentation.flow.main.listing.ListingFragment
import com.babilonia.presentation.flow.main.listing.common.about.AboutListingFragment
import com.babilonia.presentation.flow.main.listing.gallery.GalleryFragment
import com.babilonia.presentation.flow.main.listing.gallery.photo.GalleryPhotoFragment
import com.babilonia.presentation.flow.main.listing.map.ListingFullscreenMapFragment
import com.babilonia.presentation.flow.main.map.FullscreenMapFragment
import com.babilonia.presentation.flow.main.profile.email.ProfileEmailFragment
import com.babilonia.presentation.flow.main.profile.username.UserNameProfileFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerFragment
import com.babilonia.presentation.flow.main.publish.description.ListingDescriptionFragment
import com.babilonia.presentation.flow.main.publish.placepicker.PlacePickerFragment
import com.babilonia.presentation.flow.main.search.filters.ListingFiltersFragment
import com.babilonia.presentation.view.bottombar.MeowBottomNavigation

class RootFragment : BaseFragment<RootFragmentBinding, RootViewModel>() {
    private var currentNavController: LiveData<NavController>? = null

    override fun viewCreated() {
        setupBottomNavigationBar()
        observeViewModel()
        viewModel.checkDeeplinks()
    }

    private fun observeViewModel() {
        viewModel.getNavigateToProfileLiveData().observe(this, Observer {
            binding.bottomNav.show(R.id.profile_nav_graph)
        })
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.add(
            MeowBottomNavigation.Model(
                R.id.search_nav_graph,
                R.drawable.ic_search_grey,
                getString(R.string.search)
            )
        )
        bottomNavigationView.add(
            MeowBottomNavigation.Model(
                R.id.favorites_nav_graph,
                R.drawable.ic_favorites_white_24,
                getString(R.string.favorites)
            )
        )

        bottomNavigationView.add(
            MeowBottomNavigation.Model(
                R.id.my_listings_nav_graph,
                R.drawable.ic_listings_24,
                getString(R.string.my_listings)
            )
        )
        bottomNavigationView.add(
            MeowBottomNavigation.Model(
                R.id.notifications_nav_graph,
                R.drawable.ic_notifications_white_24,
                getString(R.string.inbox)
            )
        )
        bottomNavigationView.add(
            MeowBottomNavigation.Model(
                R.id.profile_nav_graph,
                R.drawable.ic_profile,
                getString(R.string.profile)
            )
        )
        val navGraphIds = mutableListOf(
            R.navigation.search_nav_graph,
            R.navigation.favorites_nav_graph,
            R.navigation.my_listings_nav_graph,
            R.navigation.notifications_nav_graph,
            R.navigation.profile_nav_graph
        )

        bottomNavigationView.show(R.id.search_nav_graph)
        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = childFragmentManager,
            containerId = R.id.nav_host_container,
            intent = activity?.intent
        )

        // Whenever the selected controller changes, setup the action bar.

        controller.observe(this, Observer { navController ->
            subscribeToControllerChange(navController, bottomNavigationView)
        })
        currentNavController = controller
    }

    //TODO: Find better solution
    private fun subscribeToControllerChange(
        navController: NavController,
        bottomNavigationView: MeowBottomNavigation
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when {
                destination.label == CreateListingContainerFragment::class.java.simpleName
                        || destination.label == ListingDescriptionFragment::class.java.simpleName
                        || destination.label == PlacePickerFragment::class.java.simpleName
                        || destination.label == ListingFragment::class.java.simpleName
                        || destination.label == AboutListingFragment::class.java.simpleName
                        || destination.label == GalleryFragment::class.java.simpleName
                        || destination.label == GalleryPhotoFragment::class.java.simpleName
                        || destination.label == FullscreenMapFragment::class.java.simpleName
                        || destination.label == ListingFullscreenMapFragment::class.java.simpleName
                        || destination.label == ProfileEmailFragment::class.java.simpleName
                        || destination.label == ListingFiltersFragment::class.java.simpleName
                        || destination.label == UserNameProfileFragment::class.java.simpleName -> bottomNavigationView.visibility =
                    View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

}
