package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.ListingJson

class GetMyRecordsResponse(var records: RecordsJson)

data class RecordsJson(
    var listings: List<ListingJson>
)