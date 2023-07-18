package com.babilonia.presentation.flow.main.profile

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.R
import com.babilonia.data.network.error.EmailAlreadyTakenException
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.usecase.GetUserUseCase
import com.babilonia.domain.usecase.UpdateUserUseCase
import com.babilonia.domain.usecase.UploadUserAvatarUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.flow.main.common.PrivacyContentType
import com.babilonia.presentation.utils.ImageUtil
import com.babilonia.presentation.utils.deeplink.DeeplinkHandler
import com.babilonia.presentation.utils.deeplink.DeeplinkNavigationConstants
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val uploadUserAvatarUseCase: UploadUserAvatarUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deeplinkHandler: DeeplinkHandler
) : BaseViewModel() {
    val userLiveData = MutableLiveData<User>()
    var updateUserNameValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return userLiveData.value?.firstName?.trim().isNullOrEmpty().not()
                    && userLiveData.value?.lastName?.trim().isNullOrEmpty().not()
        }
    }
    var updateEmailValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return isEmailValid()
        }
    }
    var editType: SuccessMessageType? = null
    val photoUploadProgressLiveData = MutableLiveData<Boolean>(false)
    val emailAlreadyTakenLiveData = SingleLiveEvent<Unit>()

    fun checkDeeplinks() {
        if (deeplinkHandler.hasDeeplink()) {
            val deeplink = deeplinkHandler.getLink()
            if (deeplink?.destination == DeeplinkNavigationConstants.TO_PRIVACY_POLICY) {
                deeplinkHandler.clearLink()
                navigateToPrivacyPolicy()
            }
        }
    }

    fun navigateToEmailEdit() {
        navigate(R.id.action_profileFragment_to_profileEmailFragment)
    }

    fun navigateToEditName() {
        navigate(R.id.action_profileFragment_to_userNameProfileFragment)
    }

    fun navigateToAccount() {
        navigate(R.id.action_profileFragment_to_accountFragment)
    }

    fun navigateToTermsAndConditions() {
        navigate(ProfileFragmentDirections.actionProfileFragmentToPrivacyFragment(PrivacyContentType.TERMS_AND_CONDITIONS))
    }

    fun navigateToPrivacyPolicy() {
        navigate(ProfileFragmentDirections.actionProfileFragmentToPrivacyFragment(PrivacyContentType.PRIVACY_POLICY))
    }

    fun updateUser() {
        userLiveData.value?.let { user ->
            user.firstName = user.firstName?.trim()
            user.lastName = user.lastName?.trim()
            user.email = user.email?.trim()
            updateUserUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(user: User) {
                    userLiveData.postValue(user)
                    editType?.let {
                        messageEvent.postValue(it)
                    }
                    navigateBack()
                }

                override fun onError(e: Throwable) {
                    if (e is EmailAlreadyTakenException) {
                        emailAlreadyTakenLiveData.call()
                    } else {
                        dataError.postValue(e)
                    }                }
            }, user)
        }
    }


    fun getUser() {
        getUserUseCase.execute(object : DisposableSubscriber<User>() {
            override fun onComplete() {

            }

            override fun onNext(user: User) {
                userLiveData.value = user
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
            }
        }, Unit)
    }

    fun compressImageAndUpload(context: Context, uri: Uri) {
        disposables.add(
            ImageUtil.compressBitmap(context, uri)
                .doOnSubscribe {
                    photoUploadProgressLiveData.value = true
                }
                .subscribe(
                    { file ->
                        if (file != null) {
                            uploadImage(file.path)
                        } else {
                            photoUploadProgressLiveData.value = false
                        }
                    }, { throwable ->
                        Timber.e(throwable)
                        photoUploadProgressLiveData.value = false
                    }
                )
        )
    }

    private fun uploadImage(path: String) {
        userLiveData.value?.let {
            uploadUserAvatarUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onStart() {
                    super.onStart()
                    photoUploadProgressLiveData.value = true
                }
                override fun onError(e: Throwable) {
                    dataError.postValue(e)
                    photoUploadProgressLiveData.value = false
                }

                override fun onSuccess(user: User) {
                    userLiveData.value = user
                    messageEvent.postValue(SuccessMessageType.AVATAR)
                    photoUploadProgressLiveData.value = false
                }

            }, UploadUserAvatarUseCase.Params(path, it.firstName, it.lastName, it.email))
        }

    }

    private fun isEmailValid(): Boolean {
        return userLiveData.value?.email?.trim().isNullOrEmpty().not() &&
                Patterns.EMAIL_ADDRESS.matcher(userLiveData.value?.email).matches()
    }
}
