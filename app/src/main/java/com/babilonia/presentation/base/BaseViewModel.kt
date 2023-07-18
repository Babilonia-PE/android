package com.babilonia.presentation.base

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.usecase.SignOutUseCase
import com.babilonia.presentation.flow.main.profile.account.AccountFragmentDirections
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import javax.inject.Inject

// Created by Anton Yatsenko on 26.02.2019.
abstract class BaseViewModel : ViewModel() {
    val dataError = SingleLiveEvent<Throwable>()
    val loadingEvent = MutableLiveData<Boolean>()
    val navigationCommands = SingleLiveEvent<NavigationCommand>()
    val messageEvent = SingleLiveEvent<SuccessMessageType>()
    protected val disposables = CompositeDisposable()
    @Inject
    lateinit var signOutUseCase: SignOutUseCase

    protected fun navigate(directions: NavDirections, closePrevious: Boolean = false) {
        navigationCommands.postValue(NavigationCommand.ToDestination(directions, closePrevious = closePrevious))
    }

    protected fun navigateGlobal(directions: NavDirections, closePrevious: Boolean = false) {
        navigationCommands.postValue(NavigationCommand.ToDestinationGlobal(directions, closePrevious = closePrevious))
    }

    protected fun navigate(directions: Int, bundle: Bundle? = null, closePrevious: Boolean = false) {
        navigationCommands.postValue(NavigationCommand.ToScreen(directions, bundle, closePrevious))
    }

    protected fun navigateGlobal(directions: Int, bundle: Bundle? = null, closePrevious: Boolean = false) {
        navigationCommands.postValue(NavigationCommand.ToScreenGlobal(directions, bundle, closePrevious))
    }

    fun navigateBack() {
        navigationCommands.postValue(NavigationCommand.Back)
    }

    fun forceSignOut() {
        navigationCommands.postValue(NavigationCommand.SignOut)
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun signOut(forceSignOut: Boolean) {
        signOutUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                if (forceSignOut) {
                    forceSignOut()
                } else {
                    navigate(AccountFragmentDirections.actionAccountFragmentToAuthActivity(), closePrevious = true)
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }
}
