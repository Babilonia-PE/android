package com.babilonia.data.model.geo

data class LocationRequest(val priority: Int, val interval: Long) {
    companion object {
        const val PRIORITY_HIGH_ACCURACY = 100
        const val PRIORITY_BALANCED_POWER_ACCURACY = 102
        const val PRIORITY_LOW_POWER = 104
        const val PRIORITY_NO_POWER = 105

        const val LOCATION_UPDATE_INTERVAL_SMALL = 2000L
        const val LOCATION_UPDATE_INTERVAL_LARGE = 5000L
    }
}