package com.babilonia.presentation.di.activity

import com.babilonia.presentation.flow.auth.createprofile.CreateProfileFragment
import com.babilonia.presentation.flow.auth.login.LogInFragment
import com.babilonia.presentation.flow.auth.signup.SignUpFragment
import com.babilonia.presentation.flow.auth.splash.SplashFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

// Created by Anton Yatsenko on 03.07.2019.
@Module
abstract class AuthActivityFragmentsModule {
    @ContributesAndroidInjector
    abstract fun contributeSplashFragment(): SplashFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateProfileFragment(): CreateProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeSignUpFragment(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun contributeLogInFragment(): LogInFragment
}