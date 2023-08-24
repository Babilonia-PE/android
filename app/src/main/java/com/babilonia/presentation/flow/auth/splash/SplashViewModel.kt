package com.babilonia.presentation.flow.auth.splash

import android.net.Uri
import com.babilonia.BuildConfig
import com.babilonia.R
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.LoginStatus
import com.babilonia.domain.model.NewVersion
import com.babilonia.domain.usecase.AuthUseCase
import com.babilonia.domain.usecase.GetRemoteUserUseCase
import com.babilonia.domain.usecase.InitAppConfigUseCase
import com.babilonia.domain.usecase.IsLoggedInUseCase
import com.babilonia.domain.usecase.IsNewVersionUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.utils.deeplink.DeeplinkHandler
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject


class SplashViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val authUseCase: AuthUseCase,
    private val initAppConfigUseCase: InitAppConfigUseCase,
    private val deeplinkHandler: DeeplinkHandler,
    private val getUserUseCase: GetRemoteUserUseCase,
    private val isNewVersionUseCase: IsNewVersionUseCase,
) : BaseViewModel() {
    val isLoggedInEvent = SingleLiveEvent<LoginStatus>()
    val navigateToRootLiveData = SingleLiveEvent<Unit>()
    val navigateToPlayStore = SingleLiveEvent<Unit>()

    fun navigateToCreateProfile() {
        navigate(R.id.action_splashFragment_to_createProfileFragment)
    }

    fun navigateToRoot() {
        navigateToRootLiveData.call()
    }

    fun onNewDeeplink(link: Uri) {
        deeplinkHandler.onNewLink(link)
    }

    fun initAppConfig() {
        initAppConfigUseCase.execute(object : DisposableCompletableObserver() {
            override fun onComplete() {
                validateVersion()
            }

            override fun onError(e: Throwable) {
                //User should be able to login even if we cant get app config
                validateVersion()
            }

        }, InitAppConfigUseCase.Params(version = BuildConfig.VERSION_CODE))
    }

    fun validateVersion() {
        isNewVersionUseCase.execute(object : DisposableSingleObserver<NewVersion>() {
            override fun onSuccess(t: NewVersion) {
                if (t.update) {
                    navigateToPlayStore.postValue(Unit)
                } else {
                    isLoggedIn()
                }
            }

            override fun onError(e: Throwable) {
                isLoggedIn()
            }
        }, Unit)
    }

    fun isLoggedIn() {
        isLoggedInUseCase.execute(object : DisposableSingleObserver<LoginStatus>() {
            override fun onSuccess(status: LoginStatus) {
                when (status) {
                    LoginStatus.AUTHORIZED -> getUser(false)
                    LoginStatus.UNAUTHORIZED -> isLoggedInEvent.postValue(status)
                    LoginStatus.PARTIALLY -> getUser(false)
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, Unit)
    }

    private fun getUser(toCreateProfile: Boolean) {
        getUserUseCase.execute(object : DisposableSingleObserver<User>() {
            override fun onSuccess(user: User) {
                if (toCreateProfile)
                    navigateToCreateProfile()
                else
                    navigateToRoot()
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        isLoggedInEvent.postValue(LoginStatus.UNAUTHORIZED)
                    }
                }
            }
        }, Unit)
    }

    fun loginWithFirebaseCode(code: String) {
        authUseCase.execute(object : DisposableSingleObserver<User>() {
            override fun onSuccess(user: User) {
                if (isNew(user)) {
                    navigateToCreateProfile()
                } else {
                    navigateToRoot()
                }
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }

        }, code)

    }

    fun navigateToSignUp() {
        navigate(R.id.action_splashFragment_to_signUpFragment)
    }

    fun navigateToLogIn() {
        navigate(R.id.action_splashFragment_to_logInFragment)
    }

    override fun onCleared() {
        authUseCase.dispose()
        isLoggedInUseCase.dispose()
        super.onCleared()
    }

    private fun isNew(user: User): Boolean {
        return user.fullName.isNullOrEmpty()
    }

}
