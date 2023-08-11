package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 27.06.2019.
class UpdateUserUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<User, UpdateUserUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<User> {
        return authRepository.updateUser(params.user, params.password, params.photoId)
    }

    class Params(val user: User, val password: String?, val photoId: Int?)
}