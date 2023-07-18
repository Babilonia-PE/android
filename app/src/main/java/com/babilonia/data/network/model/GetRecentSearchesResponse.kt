package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.RecentSearchJson
import com.google.gson.annotations.SerializedName

class GetRecentSearchesResponse(
    @SerializedName("records")
    val records: List<RecentSearchJson>
)