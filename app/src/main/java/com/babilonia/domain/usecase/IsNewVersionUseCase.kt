package com.babilonia.domain.usecase

import com.babilonia.domain.model.NewVersion
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class IsNewVersionUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<NewVersion, Unit>() {

    override fun buildUseCaseSingle(params: Unit): Single<NewVersion> {
        return authRepository.getAppConfig().map { it.newVersion }
    }
}