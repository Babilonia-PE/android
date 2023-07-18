package com.babilonia.presentation.flow.main.listing.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.babilonia.R
import com.babilonia.databinding.FragmentListingFullscreenMapBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.view.dialog.StyledAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tbruyelle.rxpermissions2.RxPermissions

class ListingFullscreenMapFragment :
    BaseFragment<FragmentListingFullscreenMapBinding, ListingFullscreenMapViewModel>(), OnMapReadyCallback {

    private val args: ListingFullscreenMapFragmentArgs by navArgs()

    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null

    override fun viewCreated() {
        setToolbar()
        observeViewModel()
        mapFragment = childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        viewModel.getListing(args.id)
    }

    override fun onStart() {
        super.onStart()
        viewModel.subscribeToMyLocation()
    }

    override fun onStop() {
        viewModel.unsubscribeFromMyLocation()
        super.onStop()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        viewModel.getListingLiveData().value?.position?.let { location ->
            showListingMarker(location)
            zoomToLocation(location)
            binding.btnBuildRoute.setOnClickListener { navigateToThirdPartyRoutes(location) }
        }

        binding.btnMyLocation.setOnClickListener {
            zoomToLocation(
                LatLng(viewModel.currentLocation.latitude, viewModel.currentLocation.longitude),
                true
            )
        }
    }

    private fun navigateToThirdPartyRoutes(location: LatLng) {
        val url = "waze://?ll=" + location.latitude + ", " + location.longitude + "&navigate=yes"
        val intentWaze = Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.waze")

        val uriGoogle = "google.navigation:q=" + location.latitude + "," + location.longitude + "&mode=w"
        val intentGoogleNav = Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle)).setPackage("com.google.android.apps.maps")

        val title: String = getString(R.string.choose_the_app)
        val chooserIntent = Intent.createChooser(intentGoogleNav, title)
        val arr = arrayOfNulls<Intent>(1)
        arr[0] = intentWaze
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr)
        startActivity(chooserIntent)
    }

    private fun zoomToLocation(location: LatLng, withAnimation: Boolean = false) {
        val cameraPosition: CameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(CAMERA_ZOOM)
            .build()
        if (withAnimation) {
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
            map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun observeViewModel() {
        viewModel.getListingLiveData().observe(this, Observer {
            if (it.id == args.id) {
                binding.tvAddress.text = it.locationAttributes.address
                withRequestLocationPermission { granted ->
                    if (granted) {
                        mapFragment?.getMapAsync(this)
                    }
                }
            }
        })
        viewModel.getGpsUnavailableErrorLiveData().observe(this, Observer {
            showLocationUnavailableDialog()
        })
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

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION)

    private fun needToShowLocationRationale() = ActivityCompat.shouldShowRequestPermissionRationale(
        requireActivity(),
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun withRequestLocationPermission(onComplete: (isGranted: Boolean) -> Unit) {
        when (checkLocationPermission()) {
            PackageManager.PERMISSION_GRANTED -> {
                onComplete(true)
            }
            else -> {
                if (needToShowLocationRationale()) {
                    context?.let {
                        StyledAlertDialog.Builder(it)
                            .setTitleText(getString(R.string.location_rationale_title))
                            .setBodyText(getString(R.string.location_rationale_body))
                            .setRightButton(getString(R.string.allow)) {
                                requestPermission(onComplete)
                            }
                            .setLeftButton(getString(R.string.deny)) {
                                onComplete(false)
                            }
                            .build()
                            .show()
                    }
                } else {
                    requestPermission(onComplete)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun requestPermission(onComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this)
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                onComplete(it)
            }
    }

    private fun setToolbar() {
        binding.btnBack.setOnClickListener { viewModel.navigateBack() }
    }

    private fun showLocationUnavailableDialog() {
        AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_gps_unavailable)
            .create()
            .apply {
                setOnShowListener {
                    findViewById<Button>(R.id.btnGoToSettings)?.setOnClickListener {
                        try {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (ignore: Exception) {
                        }
                        dismiss()
                    }
                    findViewById<Button>(R.id.btnNotNow)?.setOnClickListener {
                        dismiss()
                    }
                }
            }.show()
    }

    companion object {
        const val CAMERA_ZOOM = 15f
    }
}