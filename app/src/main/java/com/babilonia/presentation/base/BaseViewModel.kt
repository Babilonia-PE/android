package com.babilonia.presentation.base

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.babilonia.R
import com.babilonia.data.storage.auth.AuthStorageLocal
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.usecase.SignOutUseCase
import com.babilonia.presentation.flow.main.MainActivity
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
    val isLoading = SingleLiveEvent<Boolean>().apply{value = false}
    protected val disposables = CompositeDisposable()
    @Inject
    lateinit var signOutUseCase: SignOutUseCase

    @Inject
    lateinit var authStorageLocal: AuthStorageLocal

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

    fun signOut(forceSignOut: Boolean, context: Context) {
        authStorageLocal.setValidateDefaultLocation(false)
        signOutUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                removeShortCuts(context)

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

    fun signOut(complete: () -> Unit) {
        authStorageLocal.setValidateDefaultLocation(false)
        signOutUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                complete()
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun startLoading(){
        isLoading.postValue(true)
    }

    fun stopLoading(){
        isLoading.postValue(false)
    }

    @TargetApi(25)
    fun createShorCut(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW;
        intent.putExtra(Intent.EXTRA_SHORTCUT_ID, "open_create_listing")
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val shortcut = ShortcutInfoCompat.Builder(context, "create_listing")
            .setShortLabel(context.getString(R.string.add_new_listing))
            .setLongLabel(context.getString(R.string.add_new_listing))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_plus_black_24))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    @TargetApi(25)
    fun removeShortCuts(context: Context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }
}
