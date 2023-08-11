package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 26.06.2019.
class GetRemoteUserUseCase @Inject constructor(private val authRepository: AuthRepository) : SingleUseCase<User, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<User> {
        return authRepository.getRemoteUser()
    }
}