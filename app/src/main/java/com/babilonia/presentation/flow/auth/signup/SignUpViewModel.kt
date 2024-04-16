package com.babilonia.presentation.flow.auth.signup

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.data.network.error.EmailAlreadyTakenException
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.domain.model.PaisPrefix
import com.babilonia.domain.model.SignUp
import com.babilonia.domain.model.User
import com.babilonia.domain.usecase.GetListPaisPrefixUseCase
import com.babilonia.domain.usecase.SignUpUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val getListPaisPrefixUseCase: GetListPaisPrefixUseCase
) : BaseViewModel() {
    val signUpLiveData = MutableLiveData<SignUp>()
    val emailAlreadyTakenLiveData = SingleLiveEvent<Unit>()
    val navigateToRootLiveData = SingleLiveEvent<Unit>()
    val listPaisPrefix : MutableLiveData<List<PaisPrefix>> = MutableLiveData()
    val waitingLiveData = MutableLiveData<Boolean>(false)
    var prefix: String = ""
    var signUpValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
           return signUpLiveData.value?.fullName?.trim().isNullOrEmpty().not()
                   && signUpLiveData.value?.password?.trim().isNullOrEmpty().not()
                   && isValidPhone()
                   && isEmailValid()
        }
    }

    init {
        this.listPaisPrefix.value = listOf(PaisPrefix("Peru", "51", "### ### ###", "pe"))
        this.signUpLiveData.value = SignUp("", "", "", prefix,"", "", "android", "email")
    }

    fun navigateToMain() {
        navigateToRootLiveData.call()
    }

    fun onSignUp(context: Context) {
        signUpLiveData.value?.let { signUp ->
            signUp.fullName = signUp.fullName?.trim()
            signUp.email = signUp.email?.trim()
            signUp.password = signUp.password?.trim()
            signUp.prefix = prefix
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

    fun getListPaisPrefix() {
            getListPaisPrefixUseCase.execute(object : DisposableSingleObserver<List<PaisPrefixJson>>() {
                override fun onSuccess(t: List<PaisPrefixJson>) {
                    val presentationPaisPrefixList: List<com.babilonia.presentation.flow.main.profile.phone.PaisPrefix>? = t.map { domainPaisPrefix ->
                        com.babilonia.presentation.flow.main.profile.phone.PaisPrefix(
                            domainPaisPrefix.name.toString(),
                            domainPaisPrefix.prefix.toString(),
                            domainPaisPrefix.mask.toString(),
                            domainPaisPrefix.isoCode.toString()
                        )
                    }
                    setPaisPrefixList(presentationPaisPrefixList)
                }

                override fun onError(e: Throwable) {
                    val defaultPaisPrefix = com.babilonia.presentation.flow.main.profile.phone.PaisPrefix("Peru", "51", "### ### ###", "pe")
                    setPaisPrefixList(listOf(defaultPaisPrefix))
                }

            },GetListPaisPrefixUseCase.Params() )
    }

    fun setPaisPrefixList(paisPrefixList: List<com.babilonia.presentation.flow.main.profile.phone.PaisPrefix>?) {
        val convertedList = paisPrefixList?.map { paisPrefix ->
            PaisPrefix(paisPrefix.name, paisPrefix.prefix, paisPrefix.mask, paisPrefix.isoCode)
        }
        listPaisPrefix.value = convertedList
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