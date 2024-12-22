package com.zhadko.mapsapp.screens.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.base.BaseFragment
import com.zhadko.mapsapp.databinding.FragmentMapBinding

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
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(
            this@MapFragment
        )
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
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}