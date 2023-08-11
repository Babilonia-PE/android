package com.babilonia.presentation.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


object NetworkUtil {
    fun getMobileIPAddress(): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (i in interfaces) {
                val addresses: List<InetAddress> = Collections.list(i.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
        } // for now eat exceptions
        return ""
    }

    fun getWifiIPAddress(context: Context): String? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        return InetAddress.getByAddress(
            ByteBuffer
                .allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(wifiManager.connectionInfo.ipAddress)
                .array()
        ).hostAddress
    }

    fun getIPAddress(context: Context): String? {
        return if (getWifiIPAddress(context).equals("0.0.0.0")) {
            getMobileIPAddress();
        } else {
            getWifiIPAddress(context);
        }
    }
}