package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 27.06.2019.
class UpdateUserUseCase @Inject constructor(private val authRepository: AuthRepository) : SingleUseCase<User, User>() {
    override fun buildUseCaseSingle(params: User): Single<User> {
        return authRepository.updateUser(params)
    }
}