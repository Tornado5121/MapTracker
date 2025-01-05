package com.zhadko.mapsapp.screens.mapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.zhadko.mapsapp.R
import com.zhadko.mapsapp.base.BaseFragment
import com.zhadko.mapsapp.databinding.FragmentMapBinding
import com.zhadko.mapsapp.model.Result
import com.zhadko.mapsapp.service.LocationTrackerService
import com.zhadko.mapsapp.utils.Const.ACTION_START_LOCATION_TRACK
import com.zhadko.mapsapp.utils.Const.ACTION_STOP_LOCATION_TRACK
import com.zhadko.mapsapp.utils.extensions.checkSinglePermission
import com.zhadko.mapsapp.utils.map.MapUtil.calculateElapsedTime
import com.zhadko.mapsapp.utils.map.MapUtil.calculateTheDistance
import com.zhadko.mapsapp.utils.map.MapUtil.setCameraPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment :
    BaseFragment<FragmentMapBinding>(FragmentMapBinding::inflate),
    OnMapReadyCallback,
    OnMarkerClickListener,
    OnMyLocationClickListener {

    companion object {
        private const val MAP_LOG = "MAP_LOG"
    }

    private var markerList = mutableListOf<Marker?>()
    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var startTime = 0L
    private var stopTime = 0L

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
            stopButton.setOnClickListener { onStopButtonClicked() }
            resetButton.setOnClickListener { onResetButtonClicked() }
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun onResetButtonClicked() {
        mapReset()
    }

    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(
                it.result.latitude,
                it.result.longitude,
            )
            for (polyline in polylineList) {
                polyline.remove()
            }
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    setCameraPosition(lastKnownLocation)
                )
            )
            locationList.clear()
            for (marker in markerList) {
                marker?.remove()
            }
            binding.resetButton.isVisible = false
            binding.startButton.isVisible = true
        }
    }

    private fun onStopButtonClicked() {
        stopForegroundService()
        with(binding) {
            stopButton.isVisible = false
            startButton.isVisible = true
        }
    }

    private fun stopForegroundService() {
        binding.startButton.isEnabled = false
        sendActionCommandToService(ACTION_STOP_LOCATION_TRACK)
    }

    private fun onStartButtonClicked() {
        if (checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            sendActionCommandToService(ACTION_START_LOCATION_TRACK)
            with(binding) {
                startButton.isEnabled = false
                startButton.isVisible = false
                stopButton.isVisible = true
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                backgroundLocationPermissionsLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupCustomMapStyle()
        map.setOnMyLocationClickListener(this)
        map.setOnMarkerClickListener(this)
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
        observeLocationList()
    }

    private fun observeLocationList() {
        lifecycleScope.launch {
            LocationTrackerService.locationList.collect {
                locationList = it
                drawPolyline()
                followPolyLine()
            }
        }

        lifecycleScope.launch {
            LocationTrackerService.startTime.collect {
                startTime = it
            }
        }

        lifecycleScope.launch {
            LocationTrackerService.stopTime.collect {
                stopTime = it
                if (stopTime != 0L) {
                    showBiggerPicture()
                    displayResults()
                }
            }
        }
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()

        for (location in locationList) {
            bounds.include(location)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(), 100
            ), 2000, null
        )
        addMarker(locationList.first())
        addMarker(locationList.last())
    }

    private fun displayResults() {
        val result = Result(
            time = calculateElapsedTime(startTime, stopTime),
            distance = calculateTheDistance(locationList)
        )
        lifecycleScope.launch {
            delay(2500)
            navigateToResultScreen(result)
            binding.startButton.apply {
                isVisible = false
                isEnabled = true
            }
            with(binding) {
                stopButton.isVisible = false
                resetButton.isVisible = true
            }
        }
    }

    private fun drawPolyline() {
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    private fun followPolyLine() {
        if (locationList.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    setCameraPosition(locationList.last())
                ), 1000, null
            )
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

    private fun sendActionCommandToService(action: String) {
        Intent(requireContext(), LocationTrackerService::class.java).apply {
            this.action = action
            requireContext().startService(this)
        }
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

    private fun addMarker(position: LatLng) {
        val marker = map.addMarker(MarkerOptions().position(position))
        markerList.add(marker)
    }

    private fun navigateToResultScreen(result: Result) {
        findNavController().navigate(MapFragmentDirections.actionMapFragmentToResultFragment(result))
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }
}