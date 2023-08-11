package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

/** Created by Renso Contreras on 14/07/2021.
 * rensocontreras91@gmail.com
 * Lima, Peru.
 **/

class PaginationJson(
    @SerializedName("current_page")
    val currentPage: Int? = null,

    @SerializedName("per_page")
    val perPage: Int? = null,

    @SerializedName("total_pages")
    val totalPages: Int? = null
)