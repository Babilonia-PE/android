package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.FacilityJson
import com.google.gson.annotations.SerializedName

// Created by Anton Yatsenko on 07.06.2019.
class GetFacilitiesResponse(@SerializedName("records") var records: List<FacilityJson>)