package com.babilonia.presentation.flow.main.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.ar.LocationHelper
import com.babilonia.databinding.FragmentFullscreenMapBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.RouteStep
import com.babilonia.domain.model.enums.PropertyType
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.extension.visibleOrGone
import com.babilonia.presentation.extension.withGlide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_fullscreen_map.*
import kotlinx.android.synthetic.main.layout_ar_navigation_preview.*
import java.text.NumberFormat
import java.util.*


class FullscreenMapFragment :
    BaseFragment<FragmentFullscreenMapBinding, FullscreenMapViewModel>(), OnMapReadyCallback {

    private val args: FullscreenMapFragmentArgs by navArgs()
    private val numberFormat = NumberFormat.getInstance(Locale.US)
    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null

    private var polyline: Polyline? = null
    private var myLocationMarker: Marker? = null

    override fun viewCreated() {
        binding.viewModel = viewModel
        btnClose.setOnClickListener { activity?.onBackPressed() }
        mapFragment = childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment

        with (viewModel) {
            getListingLiveData().observe(this@FullscreenMapFragment, Observer {
                initListingPreview(it)
                withRequestLocationPermission { granted ->
                    if (granted) {
                        mapFragment?.getMapAsync(this@FullscreenMapFragment)
                    }
                }
            })
            getCurrentLocationLiveData().observe(this@FullscreenMapFragment, Observer {
                getRoute()
                updateMyPositionMarker(it)
                updateDistanceText(it)
            })
            getOrientationLiveData().observe(this@FullscreenMapFragment, Observer {
                myLocationMarker?.rotation = it
            })
            getRouteLiveData().observe(this@FullscreenMapFragment, Observer { route ->
                setPath(route)
            })
            getListing(args.id)
            subscribeToMyLocation()
            subscribeToOrientation()
        }
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.authFailedData.observe(this, Observer {
            context?.let {
                requireAuth()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.authFailedData.removeObservers(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.setOnMapClickListener {
            toggleListingPreviewVisibility()
        }
        viewModel.getListingLiveData().value?.position?.let {
            showListingMarker(it)
        }
    }

    @SuppressLint("CheckResult")
    private fun withRequestLocationPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                oncComplete(it)
            }
    }

    private fun updateDistanceText(myLocation: ILocation) {
        viewModel.getListingLiveData().value?.let {
            val distance = LocationHelper.distanceBetween(myLocation, it.locationAttributes)
            tvMapDistance.text = distance.toInt().toString()
        }
    }

    private fun toggleListingPreviewVisibility() {
        clNavigationPreview.visibleOrGone(clNavigationPreview.isVisible.not())
    }

    private fun initListingPreview(listing: Listing) {
        with (listing) {
            images?.also {
                if (it.isNotEmpty()) {
                    ivPreviewPhoto.withGlide(it[0].url)
                }
            }
            tvPreviewPrice.text = getString(R.string.price_template, numberFormat.format(price))
            tvPreviewPropertyType.text = PropertyType.getLocalizedPropertyName(resources, propertyType)
            tvPreviewAddress.text = locationAttributes.address
            setNavigationPreviewCounters(this)

            btnExitPreview.setOnClickListener {
                activity?.onBackPressed()
            }
        }
        clNavigationPreview.visible()
    }

    private fun setNavigationPreviewCounters(listing: Listing) {
        with (listing) {
            if (bedroomsCount != null && bedroomsCount != 0) {
                tvPreviewCountBedroom.visible()
                ivPreviewFirstDivider.visible()
                tvPreviewCountBedroom.text = getString(R.string.beds_numb_small, bedroomsCount)
            } else {
                tvPreviewCountBedroom.invisible()
                ivPreviewFirstDivider.invisible()
            }
            if (bathroomsCount != null && bathroomsCount != 0) {
                tvPreviewCountBathroom.visible()
                ivPreviewSecondDivider.visible()
                tvPreviewCountBathroom.text = getString(R.string.bath_numb_small, bathroomsCount)
            } else {
                ivPreviewSecondDivider.invisible()
                tvPreviewCountBathroom.invisible()
            }
            if (area != null && area != 0) {
                tvPreviewTotalArea.visible()
                tvPreviewTotalArea.text = getString(R.string.area_numb, area.toString())
            } else {
                tvPreviewTotalArea.invisible()
                if (ivPreviewSecondDivider.isVisible) {
                    ivPreviewSecondDivider.invisible()
                } else {
                    ivPreviewFirstDivider.invisible()
                }
            }
        }
    }

    private fun showListingMarker(location: LatLng) {
        map?.let {
            val bitmap =
                (ContextCompat.getDrawable(requireContext(), R.drawable.ic_ar_pin_house) as? BitmapDrawable)
                    ?.bitmap
                    ?.run {
                        Bitmap.createScaledBitmap(
                            this,
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_width),
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_height),
                            false
                        )
                    }

            val markerOptions = MarkerOptions().position(location)

            it.addMarker(
                if (bitmap == null) {
                    markerOptions
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }
            )
        }
    }

    private fun updateMyPositionMarker(location: ILocation) {
        if (myLocationMarker == null) {
            createMyLocationMarker(location)
        } else {
            myLocationMarker?.position = LatLng(location.latitude, location.longitude)
        }
    }

    private fun zoomCamera() {
        val boundsBuilder = LatLngBounds.builder().apply {
            myLocationMarker?.position?.let { include(it) }
            viewModel.getListingLiveData().value?.position?.let { include(it) }
        }
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            boundsBuilder.build(),
            resources.getDimensionPixelSize(R.dimen.fullscreen_map_camera_padding)
        )
        map?.moveCamera(cameraUpdate)
    }

    private fun createMyLocationMarker(location: ILocation) {
        map?.let {
            val bitmap =
                (ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_navigation_direction) as? BitmapDrawable)
                    ?.bitmap
                    ?.run {
                        Bitmap.createScaledBitmap(
                            this,
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_width),
                            resources.getDimensionPixelSize(R.dimen.ar_map_pin_height),
                            false
                        )
                    }

            val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude)).anchor(0.5f, 0.5f)

            myLocationMarker = it.addMarker(
                if (bitmap == null) {
                    markerOptions
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }
            )
            zoomCamera()
        }
    }

    private fun setPath(route: List<RouteStep>) {
        val path = arrayListOf<LatLng>()
        route.forEach { step ->
            path.add(step.startLocation)
            path.add(step.endLocation)
        }
        if (polyline == null) {
            val newPolylineOptions = PolylineOptions()
            newPolylineOptions.width(Constants.ROUTE_POLYLINE_WIDTH)
            context?.let { newPolylineOptions.color(ContextCompat.getColor(it, R.color.topaz)) }
            newPolylineOptions.geodesic(false)
            newPolylineOptions.pattern(listOf(Dot(), Gap(Constants.ROUTE_POLYLINE_GAP_SIZE)))
            map?.let {
                polyline = it.addPolyline(newPolylineOptions)
            }
        }
        polyline?.points = path
    }

    fun dpToPx(context: Context?, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context?.resources?.displayMetrics
        ).toInt()
    }
}