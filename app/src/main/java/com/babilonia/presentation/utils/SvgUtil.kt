package com.babilonia.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.widget.ImageView
import androidx.annotation.DimenRes
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.PlaceLocation
import com.babilonia.domain.model.RecentSearch
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.App
import com.caverock.androidsvg.SVG
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import timber.log.Timber
import java.io.IOException
import java.util.*

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

    fun convertEmptyToNull(value: String?): String?{
        return value?.let {
            val mValue = it.trim()
            if(mValue.isNotBlank()) mValue
            else null
        }
    }

    fun updateILocation(iLocation: ILocation): ILocation {
        val geocoder = Geocoder(App.applicationContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(iLocation.latitude, iLocation.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val obj = addresses[0]
                iLocation.address    = refactorAddress(obj.getAddressLine(0))
                iLocation.department = updateDepartment(obj.adminArea)
                iLocation.province   = updateProvince(obj.subAdminArea)
                iLocation.district   = updateDistrict(obj.locality)
                iLocation.zipCode    = (obj.postalCode?:"").trim()
                iLocation.country    = obj.countryName.trim()
            }
            iLocation
        } catch (e: Exception) {
            e.printStackTrace()
            iLocation
        }
    }

    fun updatePlaceLocation(placeLocation: PlaceLocation): PlaceLocation {
        val geocoder = Geocoder(App.applicationContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(placeLocation.latitude, placeLocation.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val obj = addresses[0]
                placeLocation.address    = refactorAddress(obj.getAddressLine(0))
                placeLocation.department = updateDepartment(obj.adminArea)
                placeLocation.province   = updateProvince(obj.subAdminArea)
                placeLocation.district   = updateDistrict(obj.locality)
                placeLocation.zipCode    = (obj.postalCode?:"").trim()
                placeLocation.country    = obj.countryName.trim()
            }
            placeLocation
        } catch (e: Exception) {
            e.printStackTrace()
            placeLocation
        }
    }

    fun updateDepartment(department: String?): String {
        return (department?:"")
            .replace("Departamento de", "")
            .replace("Municipalidad Metropolitana de", "")
            .replace("Gobierno Regional de", "")
            .replace("Provincia de", "")
            .replace("Cercado de", "").trim()
    }

    fun updateProvince(province: String?): String {
        return (province?:"")
            .replace("Provincia de", "")
            .replace("Cercado de", "").trim()
    }

    fun updateDistrict(district: String?): String {
        return (district?:"")
            .replace("Distrito de", "")
            .replace("Cercado de", "").trim()
    }

    fun convertRecentSearch(recentSearch: RecentSearch): Location {
        return Location(
            latitude = recentSearch.location.latitude?:EmptyConstants.ZERO_DOUBLE,
            longitude = recentSearch.location.longitude?:EmptyConstants.ZERO_DOUBLE,
            altitude = recentSearch.location.altitude?:EmptyConstants.ZERO_DOUBLE,
            address = recentSearch.location.address,
            department = recentSearch.location.department,
            district = recentSearch.location.district,
            province = recentSearch.location.province,
            zipCode = recentSearch.location.zipCode,
            country = recentSearch.location.country
        )
    }

    fun concatString(
        address: String?,
        district: String?,
        province: String?,
        department: String?
    ): String {
        address?.let {
            if (address.isEmpty()) {
                val listString =
                    listOf(district?.trim() ?: "", province?.trim() ?: "", department?.trim() ?: "")
                val listUbigeo = mutableListOf<String>()
                for (ubigeo in listString) {
                    if (ubigeo.isNotBlank())
                        listUbigeo.add(ubigeo.trim())
                }

                var concatString = ""

                val lastIndex = listUbigeo.size - 1
                for ((index, value) in listUbigeo.withIndex()) {
                    concatString = if (index != lastIndex)
                        "$concatString$value, "
                    else "$concatString$value"
                }

                return concatString.trim()
            } else {
                return address
            }
        } ?: run {
            return ""
        }
    }

    fun refactorAddress(address: String?): String{
        return address?.let{ mAddress ->
            if(mAddress.contains(","))
                (mAddress.split(",")[0]).trim()
            else mAddress.trim()
        }?: run{
            EmptyConstants.EMPTY_STRING
        }
    }

    fun updateCoordinateLocation(location: Location, listings: List<Listing>?): Location {
        val geocoder = Geocoder(App.applicationContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocationName(concatString(location.address, location.district, location.province, location.department), 1)
            if (addresses?.isNotEmpty() == true) {
                val obj = addresses[0]
                location.latitude  = obj.latitude
                location.longitude = obj.longitude
                //location.viewport  = getBuilder(listings, LatLng(obj.latitude, obj.longitude))
                  location.viewport  = LatLngBounds(LatLng(obj.latitude, obj.longitude), LatLng(obj.latitude, obj.longitude))
            }
            location
        } catch (e: Exception) {
            e.printStackTrace()
            location
        }
    }

    fun getBuilder(listings: List<Listing>?, latLng: LatLng): LatLngBounds{
        val builder = LatLngBounds.Builder()
        listings?.let{ mListings ->
            if(mListings.isNotEmpty()) {
                for (listing in mListings) {
                    builder.include(LatLng(listing.locationAttributes.latitude, listing.locationAttributes.longitude))
                }
            }else builder.include(latLng)
        }?:run{
            builder.include(latLng)
        }
        return builder.build()
    }


    fun getBuilder(listings: List<Listing>): LatLngBounds{
        val builder = LatLngBounds.Builder()
        if(listings.isNotEmpty()) {
            for (listing in listings) {
                builder.include(LatLng(listing.locationAttributes.latitude, listing.locationAttributes.longitude))
            }
        }
        return builder.build()
    }

    fun getLocation(latLng: LatLng, requireContext: Context): Location{
        val geocoder = Geocoder(requireContext, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        var mDepartment = EmptyConstants.EMPTY_STRING
        var mProvince = EmptyConstants.EMPTY_STRING
        var mDistrict = EmptyConstants.EMPTY_STRING
        var mZipCode = EmptyConstants.EMPTY_STRING
        var mCountry = EmptyConstants.EMPTY_STRING
        val address = if (addresses.isNullOrEmpty()) {
            App.applicationContext().getString(R.string.unnamed_road)
        } else {
            val obj = addresses[0]
            mDepartment = updateDepartment(obj.adminArea)
            mProvince = updateProvince(obj.subAdminArea)
            mDistrict = updateDistrict(obj.locality)
            mZipCode = (obj.postalCode ?: EmptyConstants.EMPTY_STRING).trim()
            mCountry = (obj.countryName ?: EmptyConstants.EMPTY_STRING).trim()
            obj.getAddressLine(0)
        }

        val locationDto = Location().apply {
            longitude = latLng.longitude
            latitude = latLng.latitude
            department = mDepartment
            province = mProvince
            district = mDistrict
            zipCode = mZipCode
            country = mCountry
            this.address = address.toString()
        }
        return locationDto
    }

    fun removeCharacter(text: String?, value: String = " "): String {
        return text?.replace(value, "")?.trim()?:""
    }

    fun insertString(originalString: String, stringToBeInserted: String, index: Int): String {
        return (originalString.substring(0, index + 1)
                + stringToBeInserted
                + originalString.substring(index + 1))
    }

    fun lastCharacter(originalString: String?): String {
        originalString?.let{ it ->
            return it.substring(it.length - 1)
        }?:run{
            return ""
        }
    }

    fun formatExpiryDate(date: String?): String {
        if(date==null) return ""
        return try{
            val arr = date.split("/")
            val month = arr[0]
            val year = "20".plus(arr[1])
            year.plus("/").plus(month)
        }catch (e: Exception){
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            ""
        }
    }

    fun formatByGroup(originalString: String?, byGroup: Int, divider: String): String {
        originalString?.let{ original ->
            val chunked = original.replace(divider, "").chunked(byGroup)
            var concatWords = ""
            for(word in chunked){
                concatWords = concatWords.plus(word).plus(divider)
            }
            return if(concatWords.isNotEmpty()) concatWords.dropLast(1) else concatWords.trim()
        }?:run{
            return ""
        }
    }

}