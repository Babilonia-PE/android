package com.babilonia.presentation.flow.main.publish.placepicker

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.addCallback
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.PlacePickerFragmentBinding
import com.babilonia.domain.model.Location
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.google.android.gms.location.LocationServices
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
            updateMarkerLocation()
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

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map
        googleMap?.uiSettings?.isCompassEnabled = false
        sharedViewModel.location.value?.let {
            selectedMarker = googleMap?.addMarker(
                MarkerOptions().position(
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                ).title(sharedViewModel.location.value?.address)
            )
            selectedMarker?.showInfoWindow()
        }

        googleMap?.isMyLocationEnabled = true
        if (selectedMarker != null) {
            zoomToMarker()
        } else {
            viewModel.getLocation()
        }
        googleMap?.setOnMapClickListener {
            googleMap?.clear()
            getAddress(it)
        }
    }

    private fun initClicks() {
        binding.btPickLocation.setOnClickListener {
            viewModel.navigateBack()
        }
        binding.btCancel.setOnClickListener {
            updateMarkerLocation()
            viewModel.navigateBack()
        }
    }

    private fun initAutocomplete() {
        binding.btPickPlace.setOnClickListener {
            val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            ).build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

    }

    private fun setMarkerForPlace(data: Intent?) {
        val place = data?.let { Autocomplete.getPlaceFromIntent(it) }
        place?.let {
            val marker = googleMap?.addMarker(it.latLng?.let { latlng ->
                MarkerOptions().position(latlng).title(it.address)
            })
            marker?.showInfoWindow()
            updateMarkerLocation()
            val locationDto = Location().apply {
                longitude = marker?.position?.longitude ?: EmptyConstants.ZERO_DOUBLE
                latitude =  marker?.position?.latitude ?: EmptyConstants.ZERO_DOUBLE
                address = it.address
            }
            sharedViewModel.location.value = locationDto
            binding.executePendingBindings()
            moveCamera(it.latLng)
        }
    }

    private fun updateMarkerLocation() {
        val locationDto: Location? = if (selectedMarker != null) {
            Location().apply {
                longitude = selectedMarker?.position?.longitude ?: EmptyConstants.ZERO_DOUBLE
                latitude = selectedMarker?.position?.latitude ?: EmptyConstants.ZERO_DOUBLE
                address = selectedMarker?.title
            }
        } else {
            null
        }
        sharedViewModel.location.value = locationDto
    }

    private fun moveCamera(it: LatLng?) {
        val cameraPosition = CameraPosition.Builder()
            .target(it)
            .zoom(15f)
            .build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
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

    // TODO this was in original branch
//    private fun zoomToMyPosition() {
//        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
//            if (it != null) {
//                val cameraPosition = CameraPosition.Builder()
//                    .target(LatLng(it.latitude, it.longitude))      // Sets the center of the map to location user
//                    .zoom(15f)                   // Sets the zoom
//                    .build()                   // Creates a CameraPosition from the builder
//                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//
//            } else {
//                showErrorToast()
//            }
//        }.addOnFailureListener {
//            showErrorToast()
//        }
//    }
//
//    private fun showErrorToast() =
//        Toast.makeText(requireContext(), getString(R.string.cant_find_user_location), Toast.LENGTH_SHORT).show()


    private fun getAddress(latLng: LatLng) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = if (addresses.isEmpty()) {
                getString(R.string.unnamed_road)
            } else {
                val obj = addresses[0]
                obj.getAddressLine(0)

            }
            val locationDto = Location().apply {
                longitude = latLng.longitude
                latitude = latLng.latitude
                this.address = address
            }
            googleMap?.addMarker(MarkerOptions().position(latLng).title(address))?.showInfoWindow()
            sharedViewModel.location.value = locationDto
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }

    }

}
