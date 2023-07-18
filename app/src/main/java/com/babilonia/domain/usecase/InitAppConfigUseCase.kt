package com.babilonia.domain.usecase

import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

// Created by Anton Yatsenko on 15.07.2019.
class InitAppConfigUseCase @Inject constructor(private val authRepository: AuthRepository) :
    CompletableUseCase<Unit>() {
    override fun buildUseCaseCompletable(params: Unit): Completable {
        return authRepository.initAppConfig()
    }
}