package com.babilonia.presentation.di.app

import com.babilonia.android.di.AndroidModule
import com.babilonia.data.di.DataBaseModule
import com.babilonia.data.di.NetworkModule
import com.babilonia.data.di.RepositoryModule
import com.babilonia.presentation.App
import com.babilonia.presentation.di.activity.ActivityBindingsModule
import com.babilonia.presentation.di.navigation.NavigationModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Base dagger app component
 */
@Singleton
@Component(
    modules = [AppModule::class,
        AndroidModule::class,
        DataBaseModule::class,
        RepositoryModule::class,
        NavigationModule::class,
        NetworkModule::class,
        ActivityBindingsModule::class,
        AndroidSupportInjectionModule::class]
)
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>() {
        abstract fun appModule(appModule: AppModule): Builder
        abstract fun androidModule(androidModule: AndroidModule): Builder
        abstract fun realmModule(realmModule: DataBaseModule): Builder
        abstract fun repositoryModule(repositoryModule: RepositoryModule): Builder
        abstract fun networkModule(networkModule: NetworkModule): Builder
        abstract fun navigationModule(navigationModule: NavigationModule): Builder
    }
}