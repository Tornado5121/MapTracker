package com.zhadko.mapsapp.screens.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.base.BaseFragment
import com.zhadko.mapsapp.databinding.FragmentMapBinding
import com.zhadko.mapsapp.utils.extensions.checkSinglePermission

class MapFragment :
    BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    OnMapReadyCallback {

    companion object {
        private const val MAP_LOG = "MAP_LOG"
    }

    @SuppressLint("MissingPermission")
    private val locationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                map.isMyLocationEnabled = true
            }
        }

    private lateinit var map: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this@MapFragment)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupCustomMapStyle()
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }
        checkLocationPermissions()
    }

    private fun setupCustomMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.my_custom_map
                )
            )
            if (!success) {
                Log.e(MAP_LOG, "Something wrong")
            }
        } catch (e: Exception) {
            Log.e(MAP_LOG, e.message.toString())
        }
    }

    private fun checkLocationPermissions() {
        if (checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}