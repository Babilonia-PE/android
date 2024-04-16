package com.babilonia.domain.usecase

import com.babilonia.data.network.model.json.PaisPrefixJson
import com.babilonia.domain.model.PaisPrefix
import com.babilonia.domain.repository.AuthRepository
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetListPaisPrefixUseCase @Inject constructor(private val authRepository: AuthRepository) :
    SingleUseCase<List<PaisPrefixJson>, GetListPaisPrefixUseCase.Params>() {
    class Params()

    override fun buildUseCaseSingle(params: Params): Single<List<PaisPrefixJson>> {
        return authRepository.getListPaisPrefix()
    }
}