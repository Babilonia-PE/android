package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 26.06.2019.
class UploadUserAvatarUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<User, UploadUserAvatarUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<User> {
        return authRepository.uploadUserAvatar(
            params.avatar,
            params.firstName.toString(),
            params.lastName.toString(),
            params.email.toString()
        )
    }

    class Params(val avatar: String, val firstName: String?, val lastName: String?, val email: String?)
}