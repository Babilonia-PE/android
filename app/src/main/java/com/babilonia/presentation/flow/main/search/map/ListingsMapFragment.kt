package com.babilonia.presentation.flow.main.search.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Outline
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.babilonia.EmptyConstants
import com.babilonia.R
import com.babilonia.databinding.ListingsMapFragmentBinding
import com.babilonia.domain.model.Listing
import com.babilonia.domain.model.enums.PaymentPlanKey
import com.babilonia.domain.model.geo.ILocation
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.listing.common.ListingImagesPagerAdapter
import com.babilonia.presentation.flow.main.search.ListingSearchViewModel
import com.babilonia.presentation.flow.main.search.map.common.ListingUtilsDelegateImpl
import com.babilonia.presentation.flow.main.search.map.common.ListingsMarkerRenderer
import com.babilonia.presentation.flow.main.search.map.common.ListingsUtilsDelegate
import com.babilonia.presentation.flow.main.search.map.common.MarkerManager
import com.babilonia.presentation.utils.SvgUtil.getBuilder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.tbruyelle.rxpermissions2.RxPermissions

typealias AndroidLocation = android.location.Location

class ListingsMapFragment : BaseFragment<ListingsMapFragmentBinding, ListingSearchViewModel>(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener, ListingsUtilsDelegate by ListingUtilsDelegateImpl {

    private var clusterManager: ClusterManager<Listing>? = null
    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    private var isCleared = true
    private var firstEnterScreen = true
    private var copyListings: List<Listing>?=null
    private var isLocationByBounds = false
    private var isFirstEnterByAutocomplete = false

    override fun viewCreated() {
        binding.model = viewModel
        viewModel.setIsShowScreenMap(true)
        isCleared = false
        setBottomSheetBehaviour()
        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        withRequestLocationPermission {
            if (it) {
                mapFragment?.getMapAsync(this)
            }
        }
        setClicks()
        Handler().post {
            removeSelectionFromMarker()
        }
    }

    override fun onResume() {
        setBottomSheetBehaviour()
        super.onResume()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.listings.removeObservers(this)
        viewModel.locationLiveData.removeObservers(this)
        viewModel.suggestions.removeObservers(this)
        viewModel.onPlaceFoundEvent.removeObservers(this)
        viewModel.arOnboardingLiveData.removeObservers(this)
        viewModel.notClearSearchLiveData.removeObservers(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.let{ mMap ->
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "ERROR MAP LOCATION PERMISSION", Toast.LENGTH_LONG).show()
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = true
        }?:run{
            Toast.makeText(requireContext(), "ERROR MAP LOCATION", Toast.LENGTH_LONG).show()
        }

        if (clusterManager == null) {
            clusterManager = ClusterManager(requireContext(), map)
            val listingsMarkerRenderer = ListingsMarkerRenderer(requireContext(), map, clusterManager)
            clusterManager?.renderer = listingsMarkerRenderer
            clusterManager?.setOnClusterItemClickListener {
                showBottomListing(it)

                MarkerManager.setSelected(it)

                clusterManager?.removeItem(it)
                clusterManager?.addItem(it)
                clusterManager?.cluster()

                zoomToPinWithMargin(it)
                true
            }
            map?.setInfoWindowAdapter(null)
            map?.setOnCameraIdleListener(this)
            map?.setOnMarkerClickListener(clusterManager)
            map?.uiSettings?.isMyLocationButtonEnabled = false
        }

        map?.setOnInfoWindowClickListener(null)
        map?.uiSettings?.isMapToolbarEnabled = false

        viewModel.listings.observe(this, Observer {
            copyListings = it
            var listings = it
            if (listings.size > LISTINGS_PAGE_SIZE) {
                listings = listings.subList(0, LISTINGS_PAGE_SIZE)
            }

            MarkerManager.clearCache()
            MarkerManager.cache(listings)
            refreshMarkers()
            binding.noResults.isVisible = listings.isEmpty()

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels

            copyListings?.let { mListings ->
                if (mListings.isNotEmpty() && viewModel.getSearchByBounds()) {
                    val mLatLong = getBuilder(mListings)
                    copyListings = null
                    if(viewModel.getIsForcedLocationGPS()){
                        isLocationByBounds = false
                    } else {
                        isLocationByBounds = true
                    }
                    map?.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLong, width.times(0.7).toInt(), height.times(0.7).toInt(), 30))
                    viewModel.setSearchByBounds(false)
                }else if(mListings.isEmpty() && viewModel.getSearchByBounds()){
                    val location = viewModel.onPlaceFoundEvent.value
                    isLocationByBounds = true
                    if (location != null) {
                        map?.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(location.latitude, location.longitude), LatLng(location.latitude, location.longitude)), width.times(0.7).toInt(), height.times(0.7).toInt(), 30))
                    }
                }
            }
        })

        viewModel.onFocusChangeEvent.observe(this, Observer {
            if (it) {
                BottomSheetBehavior.from(binding.clBottomListing.previewContainer).state =
                    BottomSheetBehavior.STATE_HIDDEN
            }
        })

        viewModel.arOnboardingLiveData.observe(this, Observer {
            showArOnboardingDialog()
        })

        viewModel.notClearSearchLiveData.observe(this, Observer {
            isCleared = false
        })


        viewModel.onPlaceFoundEvent.observe(this, Observer {
            if(viewModel.getIsSearchByAutocomplete() && isFirstEnterByAutocomplete){
                viewModel.getListingsFromMapNoCoordinate(it, LISTINGS_PAGE_SIZE)
            }else if(viewModel.getIsGPSLocation() && isFirstEnterByAutocomplete){
                viewModel.getListingsFromMapWithCoordinate(it, LISTINGS_PAGE_SIZE)
            }
            viewModel.setIsSearchByAutocomplete(false)
           // viewModel.setSearchByBounds(true)
        })

        isFirstEnterByAutocomplete = true
    }

    override fun onCameraIdle() {
        if(!isLocationByBounds) {
            map?.cameraPosition?.target?.let {
                //TODO viewModel.currentZoom = map?.cameraPosition?.zoom ?: EmptyConstants.EMPTY_FLOAT
                moveCurrentPosition(it)
                viewModel.clearSearchFromMap(isCleared)
                isCleared = true
            }
        }else isLocationByBounds = false

        firstEnterScreen = false
    }

    private fun moveCurrentPosition(latLng: LatLng) {
        val iLocation = viewModel.setLocationFromMapPosition(latLng.latitude, latLng.longitude)
        viewModel.getListingsFromMapWithCoordinate(iLocation, LISTINGS_PAGE_SIZE)
    }

    private fun calculateRadius(center: LatLng): Int {
        val from = AndroidLocation(EmptyConstants.EMPTY_STRING).apply {
            latitude = center.latitude
            longitude = center.longitude
        }

        val to = AndroidLocation(EmptyConstants.EMPTY_STRING).apply {
            val bounds = map?.projection?.visibleRegion?.latLngBounds?.northeast
            latitude = bounds?.latitude ?: 0.0
            longitude = bounds?.longitude ?: 0.0
        }
        viewModel.searchRadius = from.distanceTo(to).toInt()

        return viewModel.searchRadius
    }

    private fun refreshMarkers() {
        clusterManager?.clearItems()
        clusterManager?.addItems(MarkerManager.getCache())
        clusterManager?.cluster()
    }

    private fun removeSelectionFromMarker() {
        binding.btLocation.show()
        binding.btAr.show()
        binding.btToList.show()
        MarkerManager.getSelected()?.let {
            clusterManager?.removeItem(it)
            clusterManager?.addItem(it)
            MarkerManager.removeSelected()
            clusterManager?.cluster()
        }
    }

    private fun setBottomSheetBehaviour() {
        val curveRadius = 20F
        setRoundedCorersToPopup(curveRadius)
        BottomSheetBehavior.from(binding.clBottomListing.previewContainer).isHideable = true
        BottomSheetBehavior.from(binding.clBottomListing.previewContainer).state = BottomSheetBehavior.STATE_HIDDEN
        BottomSheetBehavior.from(binding.clBottomListing.previewContainer).setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    removeSelectionFromMarker()
                }
            }

        })
    }

    private fun setClicks() {
        binding.btLocation.setOnClickListener {
            withRequestLocationPermission { isGranted ->
                if (isGranted) {
                    viewModel.getLocation { zoomToMyLocation(it) }
                }
            }
        }
        binding.clBottomListing.previewContainer.setOnClickListener {
            MarkerManager.getSelected()?.let { listing ->
                viewModel.onPreviewClicked(listing)
            }
        }
        binding.clBottomListing.vpImages.setOnClickListener {
            MarkerManager.getSelected()?.let { listing ->
                viewModel.onPreviewClicked(listing)
            }
        }
        binding.clBottomListing.ivCollaps.setOnClickListener {
            hideBottomListing()
        }
        binding.btToList.setOnClickListener {
            findNavController().navigate(ListingsMapFragmentDirections.actionListingsMapFragmentToSearchFragment())
        }
        binding.btAr.setOnClickListener {
            if (viewModel.isLocationEnabled()) {
                viewModel.arButtonClicked()
            } else {
                showLocationUnavailableDialog()
            }
        }
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

    private fun showArOnboardingDialog() {
        AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_ar_onboarding)
            .create()
            .apply {
                setOnShowListener {
                    findViewById<Button>(R.id.btnGotIt)?.setOnClickListener {
                        viewModel.navigateToAr()
                        dismiss()
                    }
                }
            }.show()
    }

    private fun setRoundedCorersToPopup(curveRadius: Float) {
        binding.clBottomListing.previewContainer.outlineProvider = object : ViewOutlineProvider() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(
                    0, 0, view?.width ?: 0, (view?.height?.plus(curveRadius))?.toInt() ?: 0, curveRadius
                )
            }
        }
        binding.clBottomListing.previewContainer.clipToOutline = true
    }

    private fun showBottomListing(listing: Listing) {
        viewModel.onFocusChangeEvent.postValue(false)
        BottomSheetBehavior.from(binding.clBottomListing.previewContainer).state = BottomSheetBehavior.STATE_EXPANDED
        MarkerManager.push(listing)
        binding.btLocation.hide()
        binding.btAr.hide()
        binding.btToList.hide()
        listing.apply {
            binding.clBottomListing.tvListingBath.setVisibilityForBath(this)
            binding.clBottomListing.tvListingBeds.setVisibilityForBeds(this)
            binding.clBottomListing.clListingTypeContainer.text = getListingTypeSmall(requireContext(), this)
            binding.clBottomListing.tvSubPrice.text = setPriceSubtitle(this, requireContext())
            binding.clBottomListing.apply {
                model = listing

                if (viewModel.userIdLiveData.value == user?.id) {
                    ivFavorite.invisible()
                } else {
                    ivFavorite.visible()
                    ivFavorite.isChecked = isFavourite
                    ivFavorite.setOnClickListener {
                        id?.let { it -> viewModel.onFavouriteClicked(ivFavorite.isChecked, it) }
                    }
                }

                tvImagesCount.text = (images?.size ?: 0).toString()
                tvListingBath.text = getString(R.string.bath_numb_small, bathroomsCount)
                tvListingBeds.text = getString(R.string.beds_numb_small, bedroomsCount)
                tvListingArea.text = getString(R.string.area_numb, (area ?: 0).toString())
                tvAddress.text = locationAttributes?.address

                vpImages.adapter = ListingImagesPagerAdapter(images, 16) {
                    MarkerManager.getSelected()?.let { listing ->
                        viewModel.onPreviewClicked(listing)
                    }
                }

                pagerIndicator.attachToPager(binding.clBottomListing.vpImages)
                clListingTypeContainer.setCompoundDrawablesWithIntrinsicBounds(
                    getListingIconByType(propertyType),
                    0,
                    0,
                    0
                )
                clListingTypeContainer.backgroundTintList = getColorForListingType(listing, requireContext())

                when (adPlan) {
                    PaymentPlanKey.PLUS -> {
                        ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_plus_yellow_24)
                        ivPlanIcon.visible()
                    }
                    PaymentPlanKey.PREMIUM -> {
                        ivPlanIcon.setImageResource(R.drawable.ic_payment_plan_premium_blue_24)
                        ivPlanIcon.visible()
                    }
                    else -> ivPlanIcon.invisible()
                }
            }
        }

    }

    private fun hideBottomListing() {
        BottomSheetBehavior.from(binding.clBottomListing.previewContainer).state = BottomSheetBehavior.STATE_HIDDEN
    }

    @SuppressLint("CheckResult")
    private fun withRequestLocationPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                oncComplete(it)
            }
    }

    private fun zoomToMyLocation(it: ILocation) {
        val cameraPosition: CameraPosition = CameraPosition.Builder()
            .target(LatLng(it.latitude, it.longitude))      // Sets the center of the map to locationLiveData user
            .zoom(13f)                   // Sets the zoom
            .build()                   // Creates a CameraPosition from the builder
        map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun zoomToPinWithMargin(it: Listing) {
        val projection = map?.projection
        val markerPosition = it.position
        val markerPoint = projection?.toScreenLocation(markerPosition)
        val targetPoint = Point(markerPoint?.x ?: 0, markerPoint?.y?.plus(view?.height?.div(5) ?: 0) ?: 0)
        val targetPosition = projection?.fromScreenLocation(targetPoint)
        targetPosition?.let { it1 -> CameraUpdateFactory.newLatLng(it1) }
            ?.let { it2 -> map?.animateCamera(it2, 1000, null) }
    }

    companion object {
        private const val LISTINGS_PAGE_SIZE = 30
    }
}
