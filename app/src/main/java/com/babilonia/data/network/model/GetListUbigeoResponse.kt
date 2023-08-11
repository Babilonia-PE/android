package com.babilonia.data.network.model

import com.google.gson.annotations.SerializedName

class GetListUbigeoResponse(@SerializedName("ubigeo") var ubigeos: List<String>)