package com.babilonia.domain.usecase

import com.babilonia.domain.model.Location
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

// Created by Anton Yatsenko on 29.07.2019.
class GetDefaultLocationUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<Location, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<Location> {
        return authRepository.getAppConfig().map { it.locationDto }
    }
}