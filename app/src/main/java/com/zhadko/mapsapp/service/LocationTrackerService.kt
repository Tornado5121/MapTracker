package com.zhadko.mapsapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.zhadko.mapsapp.utils.Const.ACTION_START_LOCATION_TRACK
import com.zhadko.mapsapp.utils.Const.ACTION_STOP_LOCATION_TRACK
import com.zhadko.mapsapp.utils.Const.NOTIFICATION_CHANNEL_ID
import com.zhadko.mapsapp.utils.Const.NOTIFICATION_CHANNEL_NAME
import com.zhadko.mapsapp.utils.Const.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackerService : Service() {

    companion object {
        val locationList = MutableStateFlow(mutableListOf<LatLng>())
    }

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->
                for (location in locations) {
                    updateLocationList(location)
                }
            }
        }
    }

    override fun onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_LOCATION_TRACK -> {
                    startForegroundService()
                    startTrackingLocation()
                }

                ACTION_STOP_LOCATION_TRACK -> {}
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation() {
        val locationRequest = LocationRequest().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateLocationList(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)
        locationList.value.apply {
            add(newLatLng)
            locationList.update { mutableListOf() }
            locationList.update { this }
            Log.d("ssfsfdsfds", this.toString())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    IMPORTANCE_LOW
                )
            notificationManager.createNotificationChannel(channel)
        }
    }
}