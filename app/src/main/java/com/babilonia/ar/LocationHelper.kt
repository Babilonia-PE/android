package com.babilonia.ar

import com.babilonia.domain.model.geo.ILocation
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationHelper {
    private const val WGS84_A = 6378137.0
    private const val WGS84_E2 = 0.00669437999014
    private const val EarthRadius = 6371000.0 // in meters

    fun WSG84toECEF(location: ILocation): FloatArray {
        val radLat = toRadians(location.latitude)
        val radLng = toRadians(location.longitude)

        val cosLat = cos(radLat)
        val sinLat = sin(radLat)
        val cosLng = cos(radLng)
        val sinLng = sin(radLng)

        val N = WGS84_A / sqrt(1.0 - WGS84_E2 * sinLat * sinLat)
        val x = (N + location.altitude) * cosLat * cosLng
        val y = (N + location.altitude) * cosLat * sinLng
        val z = (N * (1.0 - WGS84_E2) + 0) * sinLat // 0 instead of location.getAltitude , skip altitude

        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun ECEFtoENU(location: ILocation, ecefLocation: FloatArray, ecefPOI: FloatArray): FloatArray {
        val radLat = toRadians(location.latitude)
        val radLng = toRadians(location.longitude)

        val cosLat = cos(radLat)
        val sinLat = sin(radLat)
        val cosLng = cos(radLng)
        val sinLng = sin(radLng)

        val dx = (ecefLocation[0] - ecefPOI[0]).toDouble()
        val dy = (ecefLocation[1] - ecefPOI[1]).toDouble()
        val dz = (ecefLocation[2] - ecefPOI[2]).toDouble()

        val east = cosLng * dy - sinLng * dx
        val north = cosLat * dz - sinLat * sinLng * dy - sinLat * cosLng * dx
        // Do not use because all points will be put to the 0 height
        // val up = cosLat * cosLng * dx + cosLat * sinLng * dy + sinLat * dz
        // return floatArrayOf(east.toFloat(), north.toFloat(), up.toFloat(), 1F)

        return floatArrayOf(east.toFloat(), north.toFloat(), 0F, 1F)
    }

    fun distanceBetween(from: ILocation, to: ILocation): Double {

        val fromLat = toRadians(from.latitude)
        val toLat = toRadians(to.latitude)

        // The delta between latitudes
        val deltaLat = toLat - fromLat

        // The delta between longitudes
        val deltaLng = toRadians(to.longitude - from.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(fromLat) * cos(toLat) * sin(deltaLng / 2) * sin(deltaLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EarthRadius * c
    }

    fun azimuthBetween(from: ILocation, to: ILocation): Double {

        val fromLat = toRadians(from.latitude)
        val toLat = toRadians(to.latitude)

        // The delta between longitudes
        val deltaLng = toRadians(to.longitude - from.longitude)

        val y = sin(deltaLng) * cos(toLat)
        val x = cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(deltaLng)

        return toDegrees(atan2(y, x))
    }
}