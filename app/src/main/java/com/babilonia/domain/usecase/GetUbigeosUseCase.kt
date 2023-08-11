package com.babilonia.domain.usecase

import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetUbigeosUseCase @Inject constructor(private val listingRepository: ListingRepository) : SingleUseCase<List<String>, GetUbigeosUseCase.Params>() {

    override fun buildUseCaseSingle(params: Params): Single<List<String>> {
        return listingRepository.getListUbigeo(params.type, params.department, params.province)

    }

    class Params(val type: String, val department: String?, val province: String?)
}