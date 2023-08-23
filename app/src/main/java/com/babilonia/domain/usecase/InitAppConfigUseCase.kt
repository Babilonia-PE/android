package com.babilonia.domain.usecase

import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

// Created by Anton Yatsenko on 15.07.2019.
class InitAppConfigUseCase @Inject constructor(private val authRepository: AuthRepository) :
    CompletableUseCase<InitAppConfigUseCase.Params>() {

    override fun buildUseCaseCompletable(params: Params): Completable {
        return authRepository.initAppConfig(version = params.version)
    }

    class Params(
        val version: Int,
    )
}