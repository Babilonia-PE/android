package com.babilonia.presentation.flow.auth.login

import android.content.Context
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.domain.model.LogIn
import com.babilonia.domain.model.User
import com.babilonia.domain.usecase.LogInUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class LogInViewModel @Inject constructor(
    private val logInUseCase: LogInUseCase
) : BaseViewModel() {
    val logInLiveData = MutableLiveData<LogIn>()
    val navigateToRootLiveData = SingleLiveEvent<Unit>()
    val waitingLiveData = MutableLiveData<Boolean>(false)
    var logInValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return logInLiveData.value?.password?.trim().isNullOrEmpty().not()
                    && isEmailValid()
        }
    }

    init {
        this.logInLiveData.value = LogIn("","", "", "android", "email")
    }

    fun onLogIn(context: Context) {
        logInLiveData.value?.let { logIn ->
            logIn.email = logIn.email?.trim()
            logIn.password = logIn.password?.trim()
            waitingLiveData.value = true

            logInUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(t: User) {
                    waitingLiveData.value = false
                    navigateToMain()
                }

                override fun onError(e: Throwable) {
                    waitingLiveData.value = false
                    dataError.postValue(e)
                }
            }, logIn)
        }
    }

    fun navigateToMain() {
        navigateToRootLiveData.call()
    }

    fun isEmailValid(): Boolean {
        return logInLiveData.value?.email?.trim().isNullOrEmpty().not()
                && Patterns.EMAIL_ADDRESS.matcher(logInLiveData.value?.email).matches()
    }
}