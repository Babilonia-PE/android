package com.babilonia.presentation.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ImageView
import androidx.annotation.DimenRes
import com.babilonia.R
import com.caverock.androidsvg.SVG
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import timber.log.Timber
import java.io.IOException


object SvgUtil {

    private var okHttpClient: OkHttpClient? = null
    private const val CACHE_SIZE: Long = 5 * 1024 * 1024

    fun loadSvg(target: ImageView, url: String?,
                placeholder: Int = R.drawable.ic_listing_placeholder,
                @DimenRes iconSizeDimen: Int = R.dimen.default_icon_size) {
        if (url.isNullOrEmpty()) {
            target.setImageResource(placeholder)
        }

        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                .cache(Cache(target.context.cacheDir, CACHE_SIZE))
                .build()
        }

        val request = Request.Builder().url(url).build()

        val disposable = Single.create<Bitmap> {  emitter ->
            okHttpClient?.newCall(request)?.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emitter.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body()?.byteStream()?.use {
                        val svg = SVG.getFromInputStream(it)

                        val imageSizePixels = target.context.resources.getDimensionPixelSize(iconSizeDimen)

                        if (svg.documentWidth != -1f) {
                            val bitmap = Bitmap.createBitmap(
                                imageSizePixels,
                                imageSizePixels,
                                Bitmap.Config.ARGB_8888
                            )
                            val bmCanvas = Canvas(bitmap)

                            // Clear background to white
                            bmCanvas.drawRGB(255, 255, 255)

                            svg.documentHeight = imageSizePixels.toFloat()
                            svg.documentWidth = imageSizePixels.toFloat()

                            // Render our image onto canvas
                            svg.renderToCanvas(bmCanvas)

                            emitter.onSuccess(bitmap)
                        }
                    }
                }
            })
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { bitmap ->
                    target.setImageBitmap(bitmap)
                }, { error ->
                    target.setImageResource(placeholder)
                    Timber.e(error)
                }
            )
    }
}