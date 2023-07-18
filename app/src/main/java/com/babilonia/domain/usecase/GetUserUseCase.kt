package com.babilonia.domain.usecase

import com.babilonia.domain.model.User
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

// Created by Anton Yatsenko on 26.06.2019.
class GetUserUseCase @Inject constructor(private val authRepository: AuthRepository) : FlowableUseCase<User, Unit>() {
    override fun buildUseCaseFlowable(params: Unit): Flowable<User> {
        return authRepository.getUser()
    }
}