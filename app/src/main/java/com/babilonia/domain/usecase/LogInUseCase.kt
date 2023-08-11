package com.babilonia.domain.usecase

import com.babilonia.domain.model.LogIn
import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject


class LogInUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<User, LogIn>() {
    override fun buildUseCaseSingle(params: LogIn): Single<User> {
        return authRepository.logIn(params)
    }
}