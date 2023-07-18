package com.babilonia.presentation.di.navigation


import com.babilonia.presentation.utils.deeplink.DeeplinkHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Navigation dagger module for Cicerone navigation
 */
@Module
class NavigationModule {
    @Provides
    @Singleton
    fun provideDeeplinkHandler(): DeeplinkHandler = DeeplinkHandler()
}