package com.babilonia.data.di

import android.content.Context
import com.babilonia.data.datasource.*
import com.babilonia.data.network.service.MapService
import com.babilonia.data.storage.ar.RealEstateDataSourceImpl
import com.babilonia.data.storage.auth.AuthStorageLocal
import com.babilonia.data.storage.auth.AuthStorageRemote
import com.babilonia.data.storage.listing.ListingStorageLocal
import com.babilonia.data.storage.listing.ListingStorageRemote
import com.babilonia.data.storage.payment.PaymentsStorageRemote
import com.babilonia.data.storage.search.PlacesStorageRemote
import com.google.android.libraries.places.api.Places
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.realm.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for data sources and storages
 */

@Module
class DataSourceModule {
    @Provides
    @Reusable
    fun provideAuthStorageRemote(authStorageRemote: AuthStorageRemote): AuthDataSourceRemote = authStorageRemote

    @Provides
    @Reusable
    fun provideAuthStorageLocal(authStorageLocal: AuthStorageLocal): AuthDataSourceLocal = authStorageLocal

    @Provides
    @Reusable
    fun provideListingStorageRemote(listingStorageRemote: ListingStorageRemote): ListingsDataSourceRemote =
        listingStorageRemote

    @Provides
    @Reusable
    fun providePaymentStorageRemote(paymentsStorageRemote: PaymentsStorageRemote): PaymentsDataSourceRemote =
        paymentsStorageRemote

    @Named(LOCAL)
    @Provides
    @Reusable
    fun provideListingStorageLocal(@Named(LOCAL) local: RealmConfiguration): ListingsDataSourceLocal =
        ListingStorageLocal(local)

    @Named(MEMORY)
    @Provides
    @Reusable
    fun provideListingStorageMemory(@Named(MEMORY) inMemory: RealmConfiguration): ListingsDataSourceLocal =
        ListingStorageLocal(inMemory)

    @Provides
    @Singleton
    fun providePlacesStorageLocal(context: Context, mapService: MapService): PlacesDataSourceRemote {
        return PlacesStorageRemote(Places.createClient(context), mapService, context)
    }

    @Provides
    fun provideArRemoteDataSource(remoteDataSource: RealEstateDataSourceImpl): RealEstateDataSource = remoteDataSource
}