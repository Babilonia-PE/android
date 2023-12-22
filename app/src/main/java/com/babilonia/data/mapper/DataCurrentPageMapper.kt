package com.babilonia.data.mapper

import com.babilonia.data.network.model.BaseResponse
import com.babilonia.data.network.model.json.DataCurrentPageJson
import com.babilonia.domain.model.DataCurrentPage
import com.babilonia.domain.model.Pagination
import javax.inject.Inject

class DataCurrentPageMapper @Inject constructor() {

    fun mapRemoteToDomain(dataCurrentPageJson: BaseResponse<DataCurrentPageJson>): DataCurrentPage? {
        return dataCurrentPageJson?.let{ mJson ->
            DataCurrentPage(
                Pagination(
                    currentPage = mJson.data.paginationJson?.currentPage?:0,
                    perPage = mJson.data.paginationJson?.perPage?:0,
                    totalPages = mJson.data.paginationJson?.totalPages?:0
                )
            )
        }
    }
}