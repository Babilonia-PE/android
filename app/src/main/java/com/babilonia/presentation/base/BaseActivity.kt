package com.babilonia.presentation.base

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerAppCompatActivity
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

// Created by Anton Yatsenko on 26.02.2019.
abstract class BaseActivity<VM : BaseViewModel> : DaggerAppCompatActivity() {

    val viewModel: VM by lazy { ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass()) }
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    abstract fun startListenToEvents()
    abstract fun stopListenToEvents()

    override fun onResume() {
        super.onResume()
        startListenToEvents()
    }


    override fun onPause() {
        super.onPause()
        stopListenToEvents()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getViewModelClass(): Class<VM> {
        val type =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        return type as Class<VM>
    }
}