package com.livinideas.googlemapsdirectionsample

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationTracking {
    //emit location for span amount of time
    fun getCurrentLocation(span:Long):Flow<Location>
    class LocationException(msg:String):Exception()
}