package com.babilonia.domain.usecase

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Alex Quevedo on 11.04.2023.
class DeleteAccountUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<BaseResponse<Any>, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<BaseResponse<Any>> {
        return authRepository.deleteAccount()
    }
}