package com.babilonia.data.mapper

import com.babilonia.EmptyConstants
import com.babilonia.data.network.model.json.DataLocationJson
import com.babilonia.domain.model.DataLocation
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.Pagination
import javax.inject.Inject

class DataLocationMapper @Inject constructor() {

    fun mapRemoteToDomain(dataLocationJson: DataLocationJson?): DataLocation? {
        return dataLocationJson?.let{ mJson ->
            DataLocation(
                Pagination(
                    currentPage = mJson.paginationJson?.currentPage?:0,
                    perPage = mJson.paginationJson?.perPage?:0,
                    totalPages = mJson.paginationJson?.totalPages?:0
                ),
                mJson.listLocationJson?.map { mLocation ->
                    Location(
                        mLocation.latitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE,
                        mLocation.longitude?.toDouble() ?: EmptyConstants.ZERO_DOUBLE,
                        EmptyConstants.ZERO_DOUBLE,
                        mLocation.address?: EmptyConstants.EMPTY_STRING,
                        mLocation.department?: EmptyConstants.EMPTY_STRING,
                        mLocation.district?: EmptyConstants.EMPTY_STRING,
                        mLocation.province?: EmptyConstants.EMPTY_STRING,
                        mLocation.zipCode?: EmptyConstants.EMPTY_STRING,
                        mLocation.country?: EmptyConstants.EMPTY_STRING
                    )
                }
            )
        }
    }
}