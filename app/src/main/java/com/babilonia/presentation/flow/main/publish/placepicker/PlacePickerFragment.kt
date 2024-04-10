package com.babilonia.presentation.flow.main.publish.placepicker

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.PlacePickerFragmentBinding
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.utils.SvgUtil.getLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*


private const val AUTOCOMPLETE_REQUEST_CODE = 99

class PlacePickerFragment : BaseCreateListingFragment<PlacePickerFragmentBinding, CreateListingContainerViewModel>(),
    OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var selectedMarker: Marker? = null

    override fun viewCreated() {
        binding.model = sharedViewModel
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        initAutocomplete()
        initClicks()
        setOnBackPressedDispatcher()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.locationLiveData.observe(this, androidx.lifecycle.Observer {
            zoomToMyPosition(it)
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.locationLiveData.removeObservers(this)
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback {
            sharedViewModel.setMyTempLocation(null)
            viewModel.navigateBack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = data?.let { Autocomplete.getStatusFromIntent(it) }
                    Toast.makeText(requireContext(), status?.statusMessage, Toast.LENGTH_SHORT).show()
                }
                RESULT_OK -> {
                    setMarkerForPlace(data)
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        this.googleMap = p0
        googleMap?.clear()
        googleMap?.uiSettings?.isCompassEnabled = false

        sharedViewModel.setMyTempLocation(sharedViewModel.getMyLocation())

        sharedViewModel.location.value?.let {
            googleMap?.clear()
            selectedMarker?.remove()
            selectedMarker = googleMap?.addMarker(MarkerOptions()
                .position(LatLng(it.latitude, it.longitude))
                .title(sharedViewModel.location.value?.address))
            selectedMarker?.showInfoWindow()
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), getString(R.string.cant_find_user_location), Toast.LENGTH_SHORT).show()
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap?.isMyLocationEnabled = true
        if (selectedMarker != null) {
            zoomToMarker()
        } else {
            viewModel.getLocation()
        }
        googleMap?.setOnMapClickListener {
            googleMap?.clear()
            selectedMarker?.remove()
            getAddress(it)
        }
    }

    private fun initClicks() {
        binding.btPickLocation.setOnClickListener {
            val tempLocation = sharedViewModel.getMyTempLocation()
            sharedViewModel.setMyLocation(tempLocation)
            sharedViewModel.setMyTempLocation(null)
            viewModel.navigateBack()
        }
        binding.btCancel.setOnClickListener {
            sharedViewModel.setMyTempLocation(null)
            viewModel.navigateBack()
        }
    }

    private fun initAutocomplete() {
        binding.btPickPlace.setOnClickListener {
            val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    private fun setMarkerForPlace(data: Intent?) {
        val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
        place?.let {
            googleMap?.clear()
            val marker = it.latLng?.let { latlng ->
                MarkerOptions().position(latlng).title(it.address)
            }?.let { it1 -> googleMap?.addMarker(it1) }
            marker?.showInfoWindow()
            val longitude = marker?.position?.longitude ?: EmptyConstants.ZERO_DOUBLE
            val latitude =  marker?.position?.latitude ?: EmptyConstants.ZERO_DOUBLE
            val locationDto = getLocation(LatLng(latitude, longitude), requireContext())
            sharedViewModel.setMyTempLocation(locationDto)
            binding.executePendingBindings()
            moveCamera(it.latLng)
        }
    }

    private fun moveCamera(it: LatLng?) {
        val cameraPosition = it?.let { it1 ->
            CameraPosition.Builder()
                .target(it1)
                .zoom(15f)
                .build()
        }
        cameraPosition?.let { it1 -> CameraUpdateFactory.newCameraPosition(it1) }
            ?.let { it2 -> googleMap?.moveCamera(it2) }
    }


    private fun zoomToMarker() {
        selectedMarker?.let {
            moveCamera(it.position)
            googleMap?.addMarker(MarkerOptions().title(it.title).position(it.position))
        }
    }

    private fun zoomToMyPosition(location: ILocation) {
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
            .zoom(15f)                   // Sets the zoom
            .build()                   // Creates a CameraPosition from the builder
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun getAddress(latLng: LatLng) {
        try {
            val locationDto = getLocation(latLng, requireContext())
            googleMap?.addMarker(MarkerOptions().position(latLng).title((locationDto.address?:"").toString()))?.showInfoWindow()
            sharedViewModel.setMyTempLocation(locationDto)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.cant_find_user_location), Toast.LENGTH_SHORT).show()
        }
    }
}
