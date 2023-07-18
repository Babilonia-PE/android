package com.babilonia.presentation.flow.main.search.map.common

import android.content.Context
import com.babilonia.domain.model.Listing
import com.babilonia.presentation.extension.safeLet
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

// Created by Anton Yatsenko on 08.07.2019.
class ListingsMarkerRenderer(private val context: Context?, map: GoogleMap?, clusterManager: ClusterManager<Listing>?) :
    DefaultClusterRenderer<Listing>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: Listing, markerOptions: MarkerOptions) {
        safeLet(item, context) { item, context ->
            markerOptions.icon(MarkerManager.createMarker(item, context))
                ?.zIndex(10f)
        }
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<Listing>): Boolean {
        return false
    }

    override fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<Listing>?) {

    }

    override fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<Listing>?) {

    }

}