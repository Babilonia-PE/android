package com.babilonia.presentation.flow.main.search.map

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Outline
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.tbruyelle.rxpermissions2.RxPermissions

typealias AndroidLocation = android.location.Location

class ListingsMapFragment : BaseFragment<ListingsMapFragmentBinding, ListingSearchViewModel>(), OnMapReadyCallback,
    GoogleMap.OnCameraIdleListener, ListingsUtilsDelegate by ListingUtilsDelegateImpl {

    private var clusterManager: ClusterManager<Listing>? = null
    private var map: GoogleMap? = null
    private var mapFragment: SupportMapFragment? = null
    override fun viewCreated() {
        binding.model = viewModel
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
        viewModel.listings.observe(this, Observer {
            var listings = it
            if (listings.size > LISTINGS_PAGE_SIZE) {
                listings = listings.subList(0, LISTINGS_PAGE_SIZE)
            }
            MarkerManager.clearCache()
            MarkerManager.cache(listings)
            refreshMarkers()
            binding.noResults.isVisible = listings.isEmpty()
        })
        viewModel.locationLiveData.observe(this, Observer {
            Handler().post {
                subscribeToMyLocation(it)
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
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.listings.removeObservers(this)
        viewModel.locationLiveData.removeObservers(this)
        viewModel.suggestions.removeObservers(this)
        viewModel.onPlaceFoundEvent.removeObservers(this)
        viewModel.arOnboardingLiveData.removeObservers(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.isMyLocationEnabled = true
        viewModel.onPlaceFoundEvent.observe(this, Observer {
            map?.moveCamera(CameraUpdateFactory.newLatLngBounds(it.viewport, 10))
            viewModel.currentZoom = map?.cameraPosition?.zoom ?: EmptyConstants.EMPTY_FLOAT
        })
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
            if (viewModel.locationLiveData.value == null) {
                viewModel.getLocation {
                    zoomToMyLocation(it)
                }
            }
            map?.setInfoWindowAdapter(null)
            map?.setOnCameraIdleListener(this)
            map?.setOnMarkerClickListener(clusterManager)
            map?.uiSettings?.isMyLocationButtonEnabled = false
        }

        map?.setOnInfoWindowClickListener(null)
        map?.uiSettings?.isMapToolbarEnabled = false
        viewModel.locationLiveData.value?.let {
            zoomToMyLocation(it)
        }
    }

    override fun onCameraIdle() {
        map?.cameraPosition?.target?.let {
            viewModel.currentZoom = map?.cameraPosition?.zoom ?: EmptyConstants.EMPTY_FLOAT
            viewModel.setLocationFromMapPosition(it.latitude, it.longitude, calculateRadius(it))
        }
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
            MarkerManager.getSelected()?.id?.let { id -> viewModel.onPreviewClicked(id) }
        }
        binding.clBottomListing.vpImages.setOnClickListener {
            MarkerManager.getSelected()?.id?.let { id -> viewModel.onPreviewClicked(id) }
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
                    MarkerManager.getSelected()?.id?.let { id ->
                        viewModel.onPreviewClicked(id)
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

    private fun subscribeToMyLocation(it: ILocation) {
        viewModel.getListings(it, calculateRadius(LatLng(it.latitude, it.longitude)), LISTINGS_PAGE_SIZE)
    }

    @SuppressLint("CheckResult")
    private fun withRequestLocationPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe {
                oncComplete(it)
            }
    }

    private fun zoomToMyLocation(it: ILocation, zoom: Float = viewModel.currentZoom) {
        val cameraPosition: CameraPosition = CameraPosition.Builder()
            .target(LatLng(it.latitude, it.longitude))      // Sets the center of the map to locationLiveData user
            .zoom(zoom)                   // Sets the zoom
            .build()                   // Creates a CameraPosition from the builder
        map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun zoomToPinWithMargin(it: Listing) {
        val projection = map?.projection
        val markerPosition = it.position
        val markerPoint = projection?.toScreenLocation(markerPosition)
        val targetPoint = Point(markerPoint?.x ?: 0, markerPoint?.y?.plus(view?.height?.div(5) ?: 0) ?: 0)
        val targetPosition = projection?.fromScreenLocation(targetPoint)
        map?.animateCamera(CameraUpdateFactory.newLatLng(targetPosition), 1000, null)
    }

    companion object {
        private const val LISTINGS_PAGE_SIZE = 30
    }
}
