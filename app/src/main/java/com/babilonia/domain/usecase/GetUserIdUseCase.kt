package com.babilonia.domain.usecase

import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<Long, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<Long> {
        return authRepository.getUserId()
    }
}