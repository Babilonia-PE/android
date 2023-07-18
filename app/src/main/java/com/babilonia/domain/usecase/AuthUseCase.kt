package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 04.06.2019.
class AuthUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<User, String>() {
    override fun buildUseCaseSingle(params: String): Single<User> {
        return authRepository.authenticate(params)
    }
}