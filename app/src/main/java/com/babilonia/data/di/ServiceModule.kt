package com.babilonia.data.di


import com.babilonia.data.network.TokenServiceHolder
import com.babilonia.data.network.service.AuthService
import com.babilonia.data.network.service.ListingsService
import com.babilonia.data.network.service.MapService
import com.babilonia.data.network.service.PaymentService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for retrofit services
 */
@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit, tokenServiceHolder: TokenServiceHolder): AuthService {
        val authService = retrofit.create(AuthService::class.java)
        tokenServiceHolder.authService = authService
        return authService
    }

    @Provides
    @Singleton
    fun provideListingsService(retrofit: Retrofit): ListingsService = retrofit.create(ListingsService::class.java)

    @Provides
    @Singleton
    fun provideMapService(retrofit: Retrofit): MapService = retrofit.create(MapService::class.java)

    @Provides
    @Singleton
    fun providePaymentService(retrofit: Retrofit): PaymentService = retrofit.create(PaymentService::class.java)
}