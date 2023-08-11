package com.babilonia.presentation.flow.auth.signup

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.data.network.error.EmailAlreadyTakenException
import com.babilonia.domain.model.SignUp
import com.babilonia.domain.model.User
import com.babilonia.domain.usecase.SignUpUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
) : BaseViewModel() {
    val signUpLiveData = MutableLiveData<SignUp>()
    val emailAlreadyTakenLiveData = SingleLiveEvent<Unit>()
    val navigateToRootLiveData = SingleLiveEvent<Unit>()
    val waitingLiveData = MutableLiveData<Boolean>(false)
    var signUpValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
           return signUpLiveData.value?.fullName?.trim().isNullOrEmpty().not()
                   && signUpLiveData.value?.password?.trim().isNullOrEmpty().not()
                   && isValidPhone()
                   && isEmailValid()
        }
    }

    init {
        this.signUpLiveData.value = SignUp("", "", "", "", "", "android", "email")
    }

    fun navigateToMain() {
        navigateToRootLiveData.call()
    }

    fun onSignUp(context: Context) {
        signUpLiveData.value?.let { signUp ->
            signUp.fullName = signUp.fullName?.trim()
            signUp.email = signUp.email?.trim()
            signUp.password = signUp.password?.trim()
            waitingLiveData.value = true

            signUpUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(t: User) {
                    waitingLiveData.value = false
                    navigateToMain()
                }

                override fun onError(e: Throwable) {
                    waitingLiveData.value = false
                    if (e is EmailAlreadyTakenException) {
                        emailAlreadyTakenLiveData.call()
                    } else {
                        dataError.postValue(e)
                    }
                }
            }, signUp)
        }
    }

    fun isEmailValid(): Boolean {
        return signUpLiveData.value?.email?.trim().isNullOrEmpty().not()
                && Patterns.EMAIL_ADDRESS.matcher(signUpLiveData.value?.email).matches()
    }

    fun isValidPhone(): Boolean {
        var validPhone = false
        if (signUpLiveData.value?.phoneNumber?.trim().isNullOrEmpty()) {
            validPhone = true
        } else if (
            PhoneNumberUtils.isGlobalPhoneNumber(signUpLiveData.value?.phoneNumber?.trim())
        ) {
            validPhone = true
        }
        return validPhone
    }
}