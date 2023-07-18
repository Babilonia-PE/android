package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.AdPlanJson
import com.google.gson.annotations.SerializedName

class GetAdPlansResponse(@SerializedName("records") var records: List<AdPlanJson>)