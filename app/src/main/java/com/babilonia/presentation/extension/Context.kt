package com.babilonia.presentation.extension

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.babilonia.R


// Created by Anton Yatsenko on 26.02.2019.
val Context.networkInfo: NetworkInfo?
    @SuppressLint("MissingPermission")
    get() =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo

fun Context.showRationaleDialog(@StringRes titleResId: Int,
                                @StringRes messageResId: Int, rationaleGranted: (Boolean) -> Unit) {
    AlertDialog.Builder(this, R.style.DialogStyle)
        .setPositiveButton(R.string.allow) { _, _ -> rationaleGranted(true) }
        .setNegativeButton(R.string.deny) { _, _ -> rationaleGranted(false) }
        .setCancelable(false)
        .setTitle(titleResId)
        .setMessage(messageResId)
        .create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }.show()
}