package com.babilonia.domain.usecase

import com.babilonia.Constants
import com.babilonia.domain.model.DataCurrentPage
import com.babilonia.domain.model.Facility
import com.babilonia.domain.model.Filter
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.SortType
import com.babilonia.domain.repository.ListingRepository
import com.babilonia.domain.usecase.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetListingsPageUseCase @Inject constructor(private val listingRepository: ListingRepository) :
    SingleUseCase<DataCurrentPage, GetListingsPageUseCase.Params>() {
    override fun buildUseCaseSingle(params: Params): Single<DataCurrentPage> {
        return with (params) {
            listingRepository.getListingsPage(
                page,
                pageSize,
                sortType,
                department,
                province,
                district,
                address
            )
        }
    }

    class Params(
        val page: Int = 1,
        val sortType: SortType,
        val pageSize: Int = Constants.PER_PAGE,
        val department: String?,
        val province: String?,
        val district: String?,
        val address: String?)
}