package com.babilonia.presentation.di.activity

import com.babilonia.presentation.flow.main.payment.checkout.CheckoutFragment
import com.babilonia.presentation.flow.main.payment.period.SelectPaymentPeriodFragment
import com.babilonia.presentation.flow.main.payment.plan.SelectPaymentPlanFragment
import com.babilonia.presentation.flow.main.payment.profile.SelectPaymentProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PaymentActivityFragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectPaymentProfileFragment(): SelectPaymentProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeSelectPaymentPlanFragment(): SelectPaymentPlanFragment

    @ContributesAndroidInjector
    abstract fun contributeSelectPaymentPeriodFragment(): SelectPaymentPeriodFragment

    @ContributesAndroidInjector
    abstract fun contributeCheckoutFragment(): CheckoutFragment
}