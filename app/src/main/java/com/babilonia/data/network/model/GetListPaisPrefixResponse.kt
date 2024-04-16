package com.babilonia.data.network.model

import com.babilonia.data.network.model.json.PaisPrefixJson
import com.google.gson.annotations.SerializedName

class GetListPaisPrefixResponse(@SerializedName("records") var records: List<PaisPrefixJson>)