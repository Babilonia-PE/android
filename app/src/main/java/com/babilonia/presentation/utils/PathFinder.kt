package com.babilonia.presentation.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.*

/**
 *
 * Created by Renso Contreras on 04/11/2020
 * rcontreras@peruapps.com.pe
 * Lima, Peru.
 **/

object PathFinder {

    private val TAG = this.javaClass.simpleName

    fun getFilePathV2(context: Context, uri: Uri?) : String? {

        val isLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

        if (uri != null)

        // Document Provider
            if (isLollipop && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorage Provider
                if (isExternalStorageDocument(uri)) {
                    val documentID = DocumentsContract.getDocumentId(uri)
                    val splitted = documentID.split(":")
                    val type = splitted[0]

                    if ("primary" == type) {
                        return Environment.getExternalStorageState() + "/" + splitted[1]
                    }
                }
                // Downloads Provider
                else if (isDownloadsDocument(uri)) {

                    val documentID = DocumentsContract.getDocumentId(uri)

                    //First fix
                    if (documentID.startsWith("raw:/")){
                        return documentID.replace("raw:", "")
                    }

                    // search in these folders
                    val contentUriPrefixesToTry = arrayOf("content://downloads/public_downloads", "content://downloads/my_downloads", "content://downloads/all_downloads")

                    for (contentUriPrefix in contentUriPrefixesToTry) {

                        try {

                            val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), documentID.toLong())

                            val path = getDataColumn(context, contentUri, null, null)

                            if (path != null) {

                                return path
                            }

                        } catch (e: Exception){
                            e.printStackTrace()
                            FirebaseCrashlytics.getInstance().recordException(e)
                            Log.e(TAG, "content uri exception: ${e.message}")
                        }
                    }

                    // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                    val fileName = getFileNameV2(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }

                    return destinationPath

                    //region Old way
                    //val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), documentID.toLong())
                    //return getDataColumn(context, contentUri, null, null)
                    //endregion

                }
                // Media Provider
                else if (isMediaDocument(uri)) {
                    val documentID = DocumentsContract.getDocumentId(uri)
                    val splitted = documentID.split(":")
                    val type = splitted[0]

                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(splitted[1])

                    return getDataColumn(context, contentUri!!, selection, selectionArgs)
                }
            }
            // MediaStore and general
            else if ("content" == uri.scheme) {
                // Return the remote address
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }

                return getDataColumn(context, uri, null, null)
            }
            // File
            else if ("file" == uri.scheme) {
                return uri.path
            }

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(
                uri, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri
            .authority
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri
            .authority
    }

    // New functions for create temporal file in cache memory
    private fun getFileNameV2(@NonNull context: Context, uri: Uri): String? {

        val mimeType = context.contentResolver.getType(uri)

        var filename: String? = null

        if (mimeType == null && context != null) {

            val path = getFilePathV2(context, uri)

            filename = if (path == null) {
                getName(uri.toString())
            } else {
                val file = File(path)
                file.name
            }
        } else {

            val returnCursor = context.contentResolver.query(uri, null, null, null, null)

            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }

        return filename
    }

    private fun getName(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val index = filename.lastIndexOf('/')
        return filename.substring(index + 1)
    }

    private fun getDocumentCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        logDir(context.cacheDir)
        logDir(dir)

        return dir
    }

    private fun logDir(dir: File) {

        Log.d(TAG, "Dir=$dir")
        val files = dir.listFiles()
        for (file in files) {
            Log.d(TAG, "File=" + file.path)
        }
    }

    @Nullable
    fun generateFileName(@Nullable fileName: String?, directory: File): File? {

        var name: String? = fileName

        if (name != null) {

            var file = File(directory, name)

            if (file.exists()) {
                var fileName: String = name
                var extension = ""
                val dotIndex = name.lastIndexOf('.')
                if (dotIndex > 0) {
                    fileName = name.substring(0, dotIndex)
                    extension = name.substring(dotIndex)
                }

                var index = 0

                while (file.exists()) {
                    index++
                    name = "$fileName($index)$extension"
                    file = File(directory, name)
                }
            }

            try {
                if (!file.createNewFile()) {
                    return null
                }
            } catch (e: IOException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.w(TAG, e)
                return null
            }

            logDir(directory)

            return file
        }

        return null
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {

        var inputStream: InputStream? = null

        var bos: BufferedOutputStream? = null

        try {

            inputStream = context.contentResolver.openInputStream(uri)

            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))

            val buf = ByteArray(1024)

            inputStream!!.read(buf)

            do {

                bos.write(buf)

            } while (inputStream.read(buf) != -1)

        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                bos?.close()
            } catch (e: IOException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    @SuppressLint("Recycle")
    fun getFilePathV1(context: Context, uri: Uri):String? {
        val cursor = context.contentResolver?.query(uri, null, null, null, null)
        cursor?.let{ mCursor ->
            val nameIndex = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            mCursor.moveToFirst()

            val name = mCursor.getString(nameIndex)
            name?.let { currentName ->
                val file = File(context.cacheDir, currentName)
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    inputStream?.let { mInputStream ->
                        val outputStream = FileOutputStream(file)
                        var read = 0
                        val maxBufferSize = 1 * 1024 * 1024
                        val bytesAvailable = mInputStream.available()
                        //int bufferSize = 1024;
                        val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
                        val buffers = ByteArray(bufferSize)
                        while (inputStream.read(buffers).also { read = it } != -1) {
                            outputStream.write(buffers, 0, read)
                        }
                        //Log.e("File Size", "Size " + file.length())
                        inputStream.close()
                        outputStream.close()
                        //Log.e("File Path", "Path " + file.path)
                    }?: return null
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    e.printStackTrace()
                }
                return file.path
            }?: return null
        }?: return null
    }

    fun getFileType(@NonNull context: Context, uri: Uri): String?{
        val cr: ContentResolver = context.contentResolver
        return cr.getType(uri)
    }

    fun getFileName(@NonNull context: Context, uri: Uri): String?{
        val mCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = mCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        mCursor?.moveToFirst()
        val fileName = nameIndex?.let { mCursor.getString(it) }
        mCursor?.close()
        return fileName
    }

    fun getFileSize(@NonNull context: Context, uri: Uri): Long{
        val mCursor = context.contentResolver.query(uri, null, null, null, null)
        val sizeIndex = mCursor?.getColumnIndex(OpenableColumns.SIZE)
        mCursor?.moveToFirst()
        val fileSize = sizeIndex?.let { mCursor.getLong(it) }?:0
        mCursor?.close()
        return fileSize
    }
}