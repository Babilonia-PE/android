package com.babilonia.data.di

import com.babilonia.data.repository.*
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.repository.PaymentsRepository
import com.babilonia.domain.repository.PlacesRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for repositories
 */
@Module(includes = [DataSourceModule::class])
class RepositoryModule {
    @Provides
    @Reusable
    fun provideAuthRepository(authRepository: AuthRepositoryImpl): AuthRepository = authRepository

    @Provides
    @Reusable
    fun provideListingRepository(listingRepository: ListingRepositoryImpl): ListingRepository = listingRepository

    @Provides
    @Reusable
    fun providePlacesRepository(placesRepository: PlacesRepositoryImpl): PlacesRepository = placesRepository

    @Provides
    @Reusable
    fun provideArDataRepository(arRepositoryImpl: ArRepositoryImpl): ArRepository = arRepositoryImpl

    @Provides
    @Reusable
    fun providePaymentsRepository(paymentsRepositoryImpl: PaymentsRepositoryImpl): PaymentsRepository = paymentsRepositoryImpl
}