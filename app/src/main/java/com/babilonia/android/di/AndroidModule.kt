package com.babilonia.android.di

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.hardware.SensorManager
import android.view.WindowManager
import com.babilonia.android.gravity.AppGravityProvider
import com.babilonia.android.geo.AppOrientationProvider
import com.babilonia.android.location.AppLocationProvider
import com.babilonia.android.permission.AppPermissionsProvider
import com.babilonia.android.rotation.AppWindowRotationProvider
import com.babilonia.android.rotation.LowPassFilter
import com.babilonia.android.system.AppSystemProvider
import com.babilonia.data.datasource.system.*
import com.babilonia.data.utils.ArTagScreenPositionProviderImpl
import com.babilonia.data.utils.DistanceBasedArTagTypeProvider
import com.babilonia.domain.utils.ArTagScreenPositionProvider
import com.babilonia.domain.utils.ArTagTypeProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule {

    @Provides
    @Singleton
    fun provideSystemProvider(application: Context): SystemProvider = AppSystemProvider(application)

    @Provides
    fun provideSensorManager(application: Context): SensorManager {
        return application.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    fun provideWindowManager(application: Context): WindowManager {
        return application.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
    }

    @Provides
    fun provideActivityManager(application: Context): ActivityManager {
        return application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    @Provides
    fun provideLocationProvider(locationProvider: AppLocationProvider): LocationProvider = locationProvider

    @Provides
    fun providePermissionsProvider(permissionsProvider: AppPermissionsProvider): PermissionsProvider =
        permissionsProvider

    @Provides
    fun provideOrientationProvider(orientationProvider: AppOrientationProvider): OrientationProvider =
        orientationProvider

    @Provides
    fun provideRotationProvider(rotationProvider: AppWindowRotationProvider): RotationProvider = rotationProvider

    @Provides
    fun provideAccelerometerProvider(provider: AppGravityProvider): GravityProvider = provider

    @Provides
    fun provideArTagScreenPositionProvider(screenPositionProvider: ArTagScreenPositionProviderImpl)
            : ArTagScreenPositionProvider = screenPositionProvider

    @Provides
    fun provideArTagSizeProvider(sizeProvider: DistanceBasedArTagTypeProvider): ArTagTypeProvider =
        sizeProvider

    @Provides
    fun provideLowPassFilter() = LowPassFilter(0.15F)
}