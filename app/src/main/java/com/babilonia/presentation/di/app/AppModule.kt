package com.babilonia.presentation.di.app

import android.content.Context
import android.content.SharedPreferences
import com.babilonia.presentation.di.viewmodel.ViewModelFactoryModule
import com.facebook.appevents.AppEventsLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Base dagger app module , contains included ViewModuleModel
 */
private const val BABILONIA_PREFS = "babilonia_prefs"

@Module(includes = [ViewModelFactoryModule::class])
class AppModule(var context: Context) {

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideSharedPrefrences(): SharedPreferences =
        context.getSharedPreferences(BABILONIA_PREFS, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideFbLogger() = AppEventsLogger.newLogger(context)
}
