package com.zhadko.mapsapp.screens.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.base.BaseFragment
import com.zhadko.mapsapp.databinding.FragmentMapBinding
import com.zhadko.mapsapp.utils.extensions.checkSinglePermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapFragment :
    BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    OnMapReadyCallback,
    OnMyLocationClickListener {

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

    @SuppressLint("MissingPermission")
    private val backgroundLocationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                onStartButtonClicked()
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

        with(binding) {
            startButton.setOnClickListener { onStartButtonClicked() }
            stopButton.setOnClickListener { }
            resetButton.setOnClickListener { }
        }
    }

    private fun onStartButtonClicked() {
        if (checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            Log.d(MAP_LOG, "Permissions already enabled")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                backgroundLocationPermissionsLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupCustomMapStyle()
        map.setOnMyLocationClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
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

    override fun onMyLocationClick(myLocation: Location) {
        with(binding) {
            hintText.animate().alpha(0f).duration = 1500
            lifecycleScope.launch {
                delay(2500)
                hintText.isVisible = false
                startButton.isVisible = true
            }
        }
    }
}