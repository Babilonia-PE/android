package com.babilonia.presentation.di.activity

import com.babilonia.presentation.flow.ar.ArSceneFragment
import com.babilonia.presentation.flow.main.listing.ListingFragment
import com.babilonia.presentation.flow.main.listing.common.about.AboutListingFragment
import com.babilonia.presentation.flow.main.listing.gallery.GalleryFragment
import com.babilonia.presentation.flow.main.listing.gallery.photo.GalleryPhotoFragment
import com.babilonia.presentation.flow.main.listing.map.ListingFullscreenMapFragment
import com.babilonia.presentation.flow.main.map.FullscreenMapFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ArSceneActivityFragmentsModule {
    @ContributesAndroidInjector
    abstract fun contributeArSceneFragment(): ArSceneFragment

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
    abstract fun contributeFullscreenMapFragment(): FullscreenMapFragment
}