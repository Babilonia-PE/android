package com.babilonia.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.babilonia.Constants
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yalantis.ucrop.util.BitmapLoadUtils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {

    fun compressBitmap(context: Context, uri: Uri): Single<File?> {
        return Single.just(uri)
            .map { processBitmap(context, uri) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun processBitmap(context: Context, uri: Uri): File? {
        var output: File? = File(uri.path)
        var size = output?.length() ?: 0
        var currentQuality = 100

        while (size > Constants.MAX_IMAGE_SIZE) {
            currentQuality -= 10
            if (currentQuality == 0) {
                return null
            }
            output = compressBitmap(context, uri, currentQuality)
            if (output != null) {
                size = output.length()
            } else {
                return null
            }
        }
        return output
    }

    private fun compressBitmap(context: Context, uri: Uri, quality: Int): File? {
        val inputBitmap = decodeBitmapFromFile(context, uri)

        return if (inputBitmap != null) {
            val outputFile = getOutputFile(context)

            FileOutputStream(getOutputFile(context)).use {
                inputBitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
                it.flush()
            }
            outputFile
        } else {
            null
        }

    }

    private fun getOutputFile(context: Context): File {
        val dir = File(context.externalCacheDir?.absolutePath + File.separator)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, COMPRESSED_IMAGE_FILE_NAME)

        if (file.exists()) {
            file.delete()
        }

        return file
    }

    @Throws(IOException::class)
    private fun decodeBitmapFromFile(context: Context, sourceUri: Uri): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        val parcelFileDescriptor: ParcelFileDescriptor?
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(Uri.fromFile(File(sourceUri.path)), "r")
        } catch (e: FileNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Timber.e(e)
            return null
        }

        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        var bitmap: Bitmap? = null

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inTempStorage = ByteArray(IMAGE_BUFFER_SIZE)
        }

        try {
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
        } catch (exception: OutOfMemoryError) {
            FirebaseCrashlytics.getInstance().recordException(exception)
            exception.printStackTrace()
        }

        BitmapLoadUtils.close(parcelFileDescriptor)

        bitmap?.let { bmp ->
            try {
                val exifOrientation = BitmapLoadUtils.getExifOrientation(context, sourceUri)
                val exifDegrees = BitmapLoadUtils.exifToDegrees(exifOrientation)
                val exifTranslation = BitmapLoadUtils.exifToTranslation(exifOrientation)

                val matrix = Matrix()
                if (exifDegrees != 0) {
                    matrix.preRotate(exifDegrees.toFloat())
                }
                if (exifTranslation != 1) {
                    matrix.postScale(exifTranslation.toFloat(), 1f)
                }
                bitmap = Bitmap.createBitmap(
                    bmp, 0, 0, bmp.width,
                    bmp.height, matrix, true
                )
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }

        return bitmap
    }

    private const val COMPRESSED_IMAGE_FILE_NAME = "output_image.jpg"
    private const val IMAGE_BUFFER_SIZE = 16 * 1024

}