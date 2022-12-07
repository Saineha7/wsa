package com.livinideas.googlemapsdirectionsample

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.stopForeground
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

var lat = ""
var lng = ""


class LocationService: Service() {
    private val scope= CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private lateinit var locationClient: LocationTracking
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()

        locationClient= LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            START->start()
            STOP->stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {

        if (c1.toString() == "" || c2.toString() == "" || c3.toString() == "") {
            Toast.makeText(
                this,
                "Make sure 3 emergency contacts are added in user profile!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val notification = NotificationCompat.Builder(this, "location")
                .setContentTitle("Tracking intialised...")
                .setContentText("Location:NA")
                .setSmallIcon(R.drawable.wsa_bg)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            locationClient
                .getCurrentLocation(10000L)
                .catch { e -> e.printStackTrace() }
                .onEach { location ->
                    lat = location.latitude.toString()
                    lng = location.longitude.toString()

                    val updates = notification.setContentText(
                         "Location Coordinates:($lat,$lng)"
                    )
                    notificationManager.notify(1, updates.build())
                    var phno = "9361038746"
                    val message = "Hi this is my location 📍: ( $lat , $lng )"
                    var i = 1
                    while (i in 1..3) {
                        phno = when (i) {
                            1 -> c1.toString()
                            2 -> c2.toString()
                            3 -> c3.toString()
                            else -> ""
                        }
                        Log.d("c1", "start: $phno")
                        i++
                        //    }
                        //val phno="9361038746"

                        /** Creating a pending intent which will be broadcasted when an sms message is successfully sent */
                        val piSent =
                            PendingIntent.getBroadcast(
                                baseContext,
                                0,
                                Intent("sent_msg"),
                                FLAG_MUTABLE
                            )

                        /** Creating a pending intent which will be broadcasted when an sms message is successfully delivered */
                        val piDelivered =
                            PendingIntent.getBroadcast(
                                baseContext,
                                0,
                                Intent("delivered_msg"),
                                FLAG_MUTABLE
                            )

/* Getting an instance of SmsManager to sent sms message from the application*/
                        val smsManager: SmsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage("$phno", null, message, piSent, piDelivered)
                    }
                }
                .launchIn(scope)
            startForeground(1, notification.build())
        }
    }

        private fun stop() {
            stopForeground(true)
            stopSelf()

        }


        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
        }

        companion object {
            const val START = "START"
            const val STOP = "STOP"

        }
}