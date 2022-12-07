package com.livinideas.googlemapsdirectionsample

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationClient(
    private val context:Context,
    private val client:FusedLocationProviderClient):LocationTracking {
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(span: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()){
                throw LocationTracking.LocationException("Location Permissions Disabled")
            }
            val locationManager=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val gps=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val network=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!gps && !network){
                throw LocationTracking.LocationException("GPS disabled")
            }
            val request=LocationRequest.create().setInterval(span).setFastestInterval(span)
//            val request=LocationRequest.Builder(span).build()
            val locationCallback=object :LocationCallback(){
                override fun onLocationResult(res: LocationResult) {
                    super.onLocationResult(res)
                    res.locations.lastOrNull()?.let{ location->
                        launch {
                            send(location)
                        }
                    }
                }
            }
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper())
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}