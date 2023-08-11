package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

/** Created by Renso Contreras on 14/07/2021.
 * rensocontreras91@gmail.com
 * Lima, Peru.
 **/

data class DataLocationJson(
    @SerializedName("pagination") val paginationJson: PaginationJson?,
    @SerializedName("records") val listLocationJson: List<LocationJson>?,
)