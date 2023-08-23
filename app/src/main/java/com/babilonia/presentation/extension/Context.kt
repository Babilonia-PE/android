package com.babilonia.presentation.extension

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.babilonia.R
import timber.log.Timber


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

fun Context.openPlayStore(packageId: String = packageName) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=$packageId")
        }
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
    }
}