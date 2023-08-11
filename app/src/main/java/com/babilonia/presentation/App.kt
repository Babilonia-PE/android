package com.babilonia.presentation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.babilonia.BuildConfig
import com.babilonia.android.di.AndroidModule
import com.babilonia.data.di.DataBaseModule
import com.babilonia.data.di.NetworkModule
import com.babilonia.data.di.RepositoryModule
import com.babilonia.data.storage.auth.AuthStorageLocal
import com.babilonia.presentation.di.app.AppModule
import com.babilonia.presentation.di.app.DaggerAppComponent
import com.babilonia.presentation.di.navigation.NavigationModule
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.google.android.libraries.places.api.Places
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.realm.Realm
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber
import javax.inject.Inject


// Created by Anton Yatsenko on 26.02.2019.
/**
 * Base app class with dagger initialization
 */

//FIXME: Move to dagger 2.23 (FIXED AndroidX compatibility)
class App : Application(), HasActivityInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Activity>
//    @Inject
//    lateinit var fragmentInjector : DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var authStorageLocal: AuthStorageLocal

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Realm.init(this)

        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .realmModule(DataBaseModule(this))
            .androidModule(AndroidModule())
            .networkModule(NetworkModule())
            .navigationModule(NavigationModule())
            .repositoryModule(RepositoryModule())
            .create(this)
            .inject(this)

        JodaTimeAndroid.init(this)

        authStorageLocal.setValidateDefaultLocation(false)

        if(BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        }

        //val prefs = getSharedPreferences(BABILONIA_PREFS, Context.MODE_PRIVATE)
        //prefs.edit().putBoolean(VALIDATE_DEFAULT_LOCATION, false).apply()
    }

    override fun activityInjector() = androidInjector
//    override fun supportFragmentInjector()= fragmentInjector
}