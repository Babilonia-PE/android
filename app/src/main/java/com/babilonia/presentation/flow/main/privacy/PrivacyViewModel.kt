package com.babilonia.presentation.flow.main.privacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babilonia.domain.usecase.GetPrivacyUrlUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.flow.main.common.PrivacyContentType
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class PrivacyViewModel @Inject constructor(
    private val getPrivacyUrlUseCase: GetPrivacyUrlUseCase
) : BaseViewModel() {

    private val privacyUrlLiveData = MutableLiveData<String>()

    fun getPrivacyUrl(contentType: PrivacyContentType) {
        getPrivacyUrlUseCase.execute(object : DisposableSingleObserver<String>() {
            override fun onSuccess(privacyUrl: String) {
                privacyUrlLiveData.value = privacyUrl
            }

            override fun onError(e: Throwable) {
                dataError.postValue(e)
                e.printStackTrace()
            }
        }, GetPrivacyUrlUseCase.Params(contentType))
    }

    fun getPrivacyUrlLiveData(): LiveData<String> = privacyUrlLiveData
}