package com.babilonia.domain.usecase

import com.babilonia.domain.model.SignUp
import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject


class SignUpUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<User, SignUp>() {
    override fun buildUseCaseSingle(params: SignUp): Single<User> {
        return authRepository.signUp(params)
    }
}