package com.babilonia.data.network.model.json

import com.google.gson.annotations.SerializedName

class RecentSearchJson {
    @SerializedName("query_string")
    var queryText: String? = null
    @SerializedName("google_places_location_id")
    var googlePlaceId: String? = null
}