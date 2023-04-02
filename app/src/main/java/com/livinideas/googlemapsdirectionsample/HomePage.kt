package com.livinideas.googlemapsdirectionsample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import kotlinx.android.synthetic.main.activity_main.view.*

lateinit var sharedpreferences: SharedPreferences

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        sharedpreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)

        displayHomePageOptions()
    }

    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun displayHomePageOptions() {
        findViewById<Button>(R.id.searchBtn).setOnClickListener() {
            if(isLocationEnabled()) {
                val myIntent = Intent(this, MainActivity::class.java)
                // myIntent.putExtra("key1", "login")
                this.startActivity(myIntent)
            }else
                Toast.makeText(this, "Turn location ON to search", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.userProfileBtn).setOnClickListener() {
            val myIntent = Intent(this, UserProfile::class.java)
            // myIntent.putExtra("key1", "login")
            this.startActivity(myIntent)
        }

        findViewById<Button>(R.id.sosBtn).setOnClickListener() {
            val callIntent = Intent(Intent.ACTION_CALL)
            // Call to women helpline - 1091
             callIntent.data = Uri.parse("tel:1091")
            this.startActivity(callIntent)
        }
    }
}