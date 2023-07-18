package com.babilonia.presentation.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.babilonia.presentation.flow.ar.ArSceneViewModel
import com.babilonia.presentation.flow.auth.AuthActivityViewModel
import com.babilonia.presentation.flow.auth.createprofile.CreateProfileViewModel
import com.babilonia.presentation.flow.auth.splash.SplashViewModel
import com.babilonia.presentation.flow.main.MainActivityViewModel
import com.babilonia.presentation.flow.main.favorites.FavoritesViewModel
import com.babilonia.presentation.flow.main.listing.ListingViewModel
import com.babilonia.presentation.flow.main.listing.common.about.AboutListingViewModel
import com.babilonia.presentation.flow.main.listing.gallery.GalleryViewModel
import com.babilonia.presentation.flow.main.listing.gallery.photo.GalleryPhotoViewModel
import com.babilonia.presentation.flow.main.listing.map.ListingFullscreenMapViewModel
import com.babilonia.presentation.flow.main.map.FullscreenMapViewModel
import com.babilonia.presentation.flow.main.notifications.NotificationsViewModel
import com.babilonia.presentation.flow.main.payment.PaymentActivitySharedViewModel
import com.babilonia.presentation.flow.main.privacy.PrivacyViewModel
import com.babilonia.presentation.flow.main.profile.ProfileViewModel
import com.babilonia.presentation.flow.main.profile.account.AccountViewModel
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.publish.mylistings.MyListingsViewModel
import com.babilonia.presentation.flow.main.root.RootViewModel
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

// Created by Anton Yatsenko on 19.07.2019.
@Module
abstract class ViewModelFactoryModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthActivityViewModel::class)
    abstract fun bindAuthActivityViewModel(myViewModel: AuthActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateProfileViewModel::class)
    abstract fun bindCreateProfileViewModel(myViewModel: CreateProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(myViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(myViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListingSearchViewModel::class)
    abstract fun bindListingSearchViewModel(myViewModel: ListingSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RootViewModel::class)
    abstract fun bindRootViewModel(myViewModel: RootViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateListingContainerViewModel::class)
    abstract fun bindCreateListingContainerViewModel(myViewModel: CreateListingContainerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(myViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PrivacyViewModel::class)
    abstract fun bindPrivacyViewModel(myViewModel: PrivacyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(myViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListingViewModel::class)
    abstract fun bindListingViewModel(myViewModel: ListingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListingFullscreenMapViewModel::class)
    abstract fun bindListingFullscreenMapViewModel(myViewModel: ListingFullscreenMapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyListingsViewModel::class)
    abstract fun bindMyListingsViewModel(myViewModel: MyListingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoritesViewModel::class)
    abstract fun bindFavoritesViewModel(myViewModel: FavoritesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationsViewModel::class)
    abstract fun bindNotificationsViewModel(myViewModel: NotificationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AboutListingViewModel::class)
    abstract fun bindAboutListingViewModel(myViewModel: AboutListingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GalleryViewModel::class)
    abstract fun bindGalleryViewModel(myViewModel: GalleryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GalleryPhotoViewModel::class)
    abstract fun bindGalleryPhotoViewModel(myViewModel: GalleryPhotoViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArSceneViewModel::class)
    abstract fun bindArSceneViewModel(myViewModel: ArSceneViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FullscreenMapViewModel::class)
    abstract fun bindFullscreenMapViewModel(myViewModel: FullscreenMapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PaymentActivitySharedViewModel::class)
    abstract fun bindPaymentActivitySharedViewModel(myViewModel: PaymentActivitySharedViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

}