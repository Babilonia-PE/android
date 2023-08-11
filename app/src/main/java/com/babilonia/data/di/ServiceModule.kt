package com.babilonia.data.di

import com.babilonia.BuildConfig
import com.babilonia.data.network.TokenServiceHolder
import com.babilonia.data.network.service.*
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for retrofit services
 */
@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideAuthService(@Named(BuildConfig.BASE_URL) retrofit: Retrofit, tokenServiceHolder: TokenServiceHolder): AuthService {
        val authService = retrofit.create(AuthService::class.java)
        tokenServiceHolder.authService = authService
        return authService
    }

    @Provides
    @Singleton
    fun provideNewAuthService(@Named(BuildConfig.BASE_URL_SERVICES) retrofit: Retrofit, tokenServiceHolder: TokenServiceHolder): NewAuthService {
        val authService = retrofit.create(NewAuthService::class.java)
        //Activar cuando el servicio se migre completamente
        //tokenServiceHolder.authService = authService
        return authService
    }

    @Provides
    @Singleton
    fun provideListingsService(
        @Named(BuildConfig.BASE_URL) retrofit: Retrofit
    ): ListingsService = retrofit.create(ListingsService::class.java)

    @Provides
    @Singleton
    fun provideNewListingsService(
        @Named(BuildConfig.BASE_URL_SERVICES) retrofit: Retrofit
    ): NewListingsService = retrofit.create(NewListingsService::class.java)

    @Provides
    @Singleton
    fun provideMapService(@Named(BuildConfig.BASE_URL) retrofit: Retrofit): MapService = retrofit.create(MapService::class.java)

    @Provides
    @Singleton
    fun providePaymentService(@Named(BuildConfig.BASE_URL_SERVICES) retrofit: Retrofit): PaymentService = retrofit.create(PaymentService::class.java)
}