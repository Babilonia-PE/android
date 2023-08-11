package com.babilonia.presentation.flow.main.profile.account

import android.content.Context
import com.babilonia.data.network.error.AuthFailedException
import com.babilonia.data.network.model.BaseResponse
import com.babilonia.domain.usecase.DeleteAccountUseCase
import com.babilonia.presentation.base.BaseViewModel
import com.babilonia.presentation.base.SingleLiveEvent
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    private val deleteAccountUseCase: DeleteAccountUseCase
) : BaseViewModel() {
    val authFailedData = SingleLiveEvent<Unit>()

    fun deleteAccount(context: Context) {
        deleteAccountUseCase.execute(object : DisposableSingleObserver<BaseResponse<Any>>() {
            override fun onSuccess(t: BaseResponse<Any>) {
                signOut(true, context)
            }

            override fun onError(e: Throwable) {
                if (e is AuthFailedException) {
                    signOut {
                        authFailedData.call()
                    }
                } else {
                    dataError.postValue(e)
                    e.printStackTrace()
                }
            }

        }, Unit)
    }
}
