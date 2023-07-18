package com.babilonia.presentation.di.activity

import com.babilonia.presentation.flow.ar.ArSceneActivity
import com.babilonia.presentation.flow.auth.AuthActivity
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.flow.main.payment.PaymentActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

// Created by Anton Yatsenko on 26.02.2019.
/**
 * Module for our activities, put each new activity here
 */
@Module
abstract class ActivityBindingsModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityFragmentsModule::class])
    abstract fun contributesMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AuthActivityFragmentsModule::class])
    abstract fun contributesAuthActivity(): AuthActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ArSceneActivityFragmentsModule::class])
    abstract fun contributesArSceneActivity(): ArSceneActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PaymentActivityFragmentsModule::class])
    abstract fun contributesPaymentActivity(): PaymentActivity
}