package com.babilonia.presentation.di.activity

import com.babilonia.presentation.flow.main.favorites.FavoritesFragment
import com.babilonia.presentation.flow.main.listing.ListingFragment
import com.babilonia.presentation.flow.main.listing.common.about.AboutListingFragment
import com.babilonia.presentation.flow.main.listing.gallery.GalleryFragment
import com.babilonia.presentation.flow.main.listing.gallery.photo.GalleryPhotoFragment
import com.babilonia.presentation.flow.main.listing.map.ListingFullscreenMapFragment
import com.babilonia.presentation.flow.main.notifications.NotificationsFragment
import com.babilonia.presentation.flow.main.privacy.PrivacyFragment
import com.babilonia.presentation.flow.main.profile.ProfileFragment
import com.babilonia.presentation.flow.main.profile.account.AccountFragment
import com.babilonia.presentation.flow.main.profile.email.ProfileEmailFragment
import com.babilonia.presentation.flow.main.profile.username.UserNameProfileFragment
import com.babilonia.presentation.flow.main.publish.advanced.AdvancedDetailsFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerFragment
import com.babilonia.presentation.flow.main.publish.description.ListingDescriptionFragment
import com.babilonia.presentation.flow.main.publish.details.CreateListingDetailsFragment
import com.babilonia.presentation.flow.main.publish.facilities.FacilitiesFragment
import com.babilonia.presentation.flow.main.publish.listingtype.ListingTypeFragment
import com.babilonia.presentation.flow.main.publish.mylistings.MyListingsFragment
import com.babilonia.presentation.flow.main.publish.photos.ListingPhotosFragment
import com.babilonia.presentation.flow.main.publish.placepicker.PlacePickerFragment
import com.babilonia.presentation.flow.main.root.RootFragment
import com.babilonia.presentation.flow.main.search.SearchRootFragment
import com.babilonia.presentation.flow.main.search.filters.ListingFiltersFragment
import com.babilonia.presentation.flow.main.search.list.SearchFragment
import com.babilonia.presentation.flow.main.search.map.ListingsMapFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

// Created by Anton Yatsenko on 28.05.2019.
@Module
abstract class MainActivityFragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeRootFragment(): RootFragment

    @ContributesAndroidInjector
    abstract fun contributeMyListingsFragment(): MyListingsFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateListingContainerFragment(): CreateListingContainerFragment

    @ContributesAndroidInjector
    abstract fun contributeListingTypeFragment(): ListingTypeFragment

    @ContributesAndroidInjector
    abstract fun contributeListringDescriptionFragment(): ListingDescriptionFragment

    @ContributesAndroidInjector
    abstract fun contributePlacePickerFragment(): PlacePickerFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateListingDetailsFragment(): CreateListingDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeFacilitiesFragment(): FacilitiesFragment

    @ContributesAndroidInjector
    abstract fun contributeAdvancedDetailsFragment(): AdvancedDetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeListingPhotosFragment(): ListingPhotosFragment

    @ContributesAndroidInjector
    abstract fun contributeListingFragment(): ListingFragment

    @ContributesAndroidInjector
    abstract fun contributeAboutListingFragment(): AboutListingFragment

    @ContributesAndroidInjector
    abstract fun contributeGalleryFragment(): GalleryFragment

    @ContributesAndroidInjector
    abstract fun contributeGalleryPhotoFragment(): GalleryPhotoFragment

    @ContributesAndroidInjector
    abstract fun contributeListingFullscreenMapFragment(): ListingFullscreenMapFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributePrivacyFragment(): PrivacyFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileEmailFragment(): ProfileEmailFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileUserNameFragment(): UserNameProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector
    abstract fun contributeFavoritesFragment(): FavoritesFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationsFragment(): NotificationsFragment

    @ContributesAndroidInjector
    abstract fun contributeListingsMapFragment(): ListingsMapFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment


    @ContributesAndroidInjector
    abstract fun contributeSearchRootFragment(): SearchRootFragment

    @ContributesAndroidInjector
    abstract fun contributeFiltersFragment(): ListingFiltersFragment
}