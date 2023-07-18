package com.babilonia.domain.usecase.ar

import com.babilonia.data.repository.ArRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class NeedToShowArOnboardingUseCase @Inject constructor(private val arRepository: ArRepository) :
    SingleUseCase<Boolean, Unit>() {
    override fun buildUseCaseSingle(params: Unit): Single<Boolean> {
        return arRepository.needToShowOnboarding()
    }
}