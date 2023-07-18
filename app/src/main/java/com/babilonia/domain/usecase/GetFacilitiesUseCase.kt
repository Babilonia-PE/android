package com.babilonia.domain.usecase

import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.enums.FacilityDataType
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

// Created by Anton Yatsenko on 07.06.2019.
class GetFacilitiesUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    FlowableUseCase<List<Facility>, GetFacilitiesUseCase.Params>() {
    override fun buildUseCaseFlowable(params: Params): Flowable<List<Facility>> {
        return listingRepository.getFacilitiesByType(params.type, params.propertyType)
    }

    class Params(val type: FacilityDataType, val propertyType: String)
}