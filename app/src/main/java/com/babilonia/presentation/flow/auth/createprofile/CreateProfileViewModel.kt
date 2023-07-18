package com.babilonia.presentation.flow.auth.createprofile

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.data.network.error.EmailAlreadyTakenException
import com.babilonia.domain.model.User
import com.babilonia.domain.usecase.GetUserUseCase
import com.babilonia.domain.usecase.UpdateUserUseCase
import com.babilonia.domain.usecase.UploadUserAvatarUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import com.babilonia.presentation.utils.ImageUtil
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber
import timber.log.Timber
import javax.inject.Inject

class CreateProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val uploadUserAvatarUseCase: UploadUserAvatarUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : BaseViewModel() {
    val userLiveData = MutableLiveData<User>()
    val photoUploadProgressLiveData = MutableLiveData<Boolean>(false)
    val emailAlreadyTakenLiveData = SingleLiveEvent<Unit>()
    val navigateToRootLiveData = SingleLiveEvent<Unit>()
    var avatarUri: Uri? = null
    var updateCreateProfileValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return userLiveData.value?.firstName?.trim().isNullOrEmpty().not()
                    && userLiveData.value?.lastName?.trim().isNullOrEmpty().not()
                    && isEmailValid()
        }
    }

    fun navigateToMain() {
        navigateToRootLiveData.call()
    }


    fun updateUser(context: Context) {
        userLiveData.value?.let { user ->
            user.firstName = user.firstName?.trim()
            user.lastName = user.lastName?.trim()
            updateUserUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(user: User) {
                    if (avatarUri == null) {
                        navigateToMain()
                    } else {
                        avatarUri?.let {
                            compressImageAndUpload(context, it)
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    if (e is EmailAlreadyTakenException) {
                        emailAlreadyTakenLiveData.call()
                    } else {
                        dataError.postValue(e)
                    }
                }
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

    private fun compressImageAndUpload(context: Context, uri: Uri) {
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
                        photoUploadProgressLiveData.value = false
                        Timber.e(throwable)
                    }
                )
        )
    }

    private fun uploadImage(path: String) {
        userLiveData.value?.let {
            uploadUserAvatarUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onError(e: Throwable) {
                    photoUploadProgressLiveData.value = false
                    dataError.postValue(e)
                }

                override fun onSuccess(user: User) {
                    photoUploadProgressLiveData.value = false
                    navigateToMain()
                }

            }, UploadUserAvatarUseCase.Params(path, it.firstName, it.lastName, it.email))
        }

    }

    fun isEmailValid(): Boolean {
        return userLiveData.value?.email?.trim().isNullOrEmpty().not()
                && Patterns.EMAIL_ADDRESS.matcher(userLiveData.value?.email).matches()
    }
}
