package com.babilonia.presentation.flow.main.profile

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.babilonia.R
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.data.network.error.EmailAlreadyTakenException
import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.domain.model.ListingImage
import com.babilonia.domain.model.PaisPrefix
import com.babilonia.domain.model.SignUp
import com.babilonia.domain.model.User
import com.babilonia.domain.model.enums.SuccessMessageType
import com.babilonia.domain.usecase.GetListPaisPrefixUseCase
import com.babilonia.domain.usecase.GetUserUseCase
import com.babilonia.domain.usecase.UpdateUserUseCase
import com.babilonia.domain.usecase.UploadImagesUseCase
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
    private val getListPaisPrefixUseCase: GetListPaisPrefixUseCase,
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deeplinkHandler: DeeplinkHandler
) : BaseViewModel() {
    val authFailedData = SingleLiveEvent<Unit>()
    val userLiveData = MutableLiveData<User>()
    val listPaisPrefix : MutableLiveData<List<PaisPrefix>> = MutableLiveData()
    var prefix: String = ""
    var updateUserNameValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return userLiveData.value?.fullName?.trim().isNullOrEmpty().not()
        }
    }
    var updateEmailValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return isEmailValid()
        }
    }
    var updatePhoneValidator = object : ObservableBoolean() {
        override fun get(): Boolean {
            return isPhoneNumberValid()
        }
    }
    var editType: SuccessMessageType? = null
    val photoUploadProgressLiveData = MutableLiveData<Boolean>(false)
    val emailAlreadyTakenLiveData = SingleLiveEvent<Unit>()

    init {
        this.listPaisPrefix.value = listOf(PaisPrefix("Peru", "51", "### ### ###", "pe"))
    }

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

    fun navigateToPhoneEdit() {
        navigate(R.id.action_profileFragment_to_profilePhoneFragment)
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
            user.fullName = user.fullName?.trim()
            user.email = user.email?.trim()
            user.phoneNumber = user.phoneNumber?.trim()
            updateUserUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(user: User) {
                    userLiveData.postValue(user)
                    editType?.let {
                        messageEvent.postValue(it)
                    }
                    navigateBack()
                }

                override fun onError(e: Throwable) {
                    when (e) {
                        is AuthFailedException -> {
                            authFailedData.call()
                        }
                        is EmailAlreadyTakenException -> {
                            emailAlreadyTakenLiveData.call()
                        }
                        else -> {
                            dataError.postValue(e)
                        }
                    }
                }
            }, UpdateUserUseCase.Params(user, null, prefix, null))
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

    fun updateUser(password: String?, photoId: Int?) {
        userLiveData.value?.let { user ->
            user.fullName = user.fullName?.trim()
            user.email = user.email?.trim()
            user.phoneNumber = user.phoneNumber?.trim()
            updateUserUseCase.execute(object : DisposableSingleObserver<User>() {
                override fun onSuccess(user: User) {
                    userLiveData.postValue(user)
                    editType?.let {
                        messageEvent.postValue(it)
                    }
                    getUser()
                    navigateBack()
                }

                override fun onError(e: Throwable) {
                    when (e) {
                        is AuthFailedException -> {
                            authFailedData.call()
                        }
                        is EmailAlreadyTakenException -> {
                            emailAlreadyTakenLiveData.call()
                        }
                        else -> {
                            dataError.postValue(e)
                        }
                    }
                }
            }, UpdateUserUseCase.Params(user, password, prefix, photoId))
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
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else
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
            uploadImagesUseCase.execute(object : DisposableSingleObserver<List<ListingImage>>() {
                override fun onStart() {
                    super.onStart()
                    photoUploadProgressLiveData.value = true
                }
                override fun onError(e: Throwable) {
                    if (e is AuthFailedException) {
                        signOut {
                            authFailedData.call()
                        }
                    } else {
                        dataError.postValue(e)
                        photoUploadProgressLiveData.value = false
                    }
                }

                override fun onSuccess(images: List<ListingImage>) {
                    updateUser(null, images[0].id)
                    //userLiveData.value = user
                    //messageEvent.postValue(SuccessMessageType.AVATAR)
                    photoUploadProgressLiveData.value = false
                }

            }, UploadImagesUseCase.Params(path, "profile"))
        }

    }

    private fun isEmailValid(): Boolean {
        return userLiveData.value?.email?.trim().isNullOrEmpty().not() &&
                Patterns.EMAIL_ADDRESS.matcher(userLiveData.value?.email).matches()
    }

    private fun isPhoneNumberValid(): Boolean {
        return userLiveData.value?.phoneNumber?.trim().isNullOrEmpty() ||
                Patterns.PHONE.matcher(userLiveData.value?.phoneNumber).matches()
        //return true
    }
}
