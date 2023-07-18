package com.babilonia.domain.usecase

import com.babilonia.domain.model.enums.LoginStatus
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 27.05.2019.
class IsLoggedInUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<LoginStatus, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<LoginStatus> {
        return authRepository.isLoggedIn()
    }
}