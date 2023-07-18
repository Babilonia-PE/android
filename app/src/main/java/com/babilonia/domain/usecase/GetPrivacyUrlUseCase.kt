package com.babilonia.domain.usecase

import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import com.babilonia.presentation.flow.main.common.PrivacyContentType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetPrivacyUrlUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<String, GetPrivacyUrlUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<String> {
        return authRepository.getAppConfig()
            .map { appConfig ->
                if (params.contentType == PrivacyContentType.PRIVACY_POLICY) {
                    appConfig.privacyPolicy
                } else {
                    appConfig.terms
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    class Params(val contentType: PrivacyContentType)
}