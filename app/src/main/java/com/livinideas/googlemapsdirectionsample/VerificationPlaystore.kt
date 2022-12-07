package com.livinideas.googlemapsdirectionsample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_verification.*

class VerificatonPlaystore : AppCompatActivity() {
    val keyResult: String = "key_result"
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101
    private lateinit var mCameraSource: CameraSource
    private lateinit var textRecognizer: TextRecognizer
    private val tag: String? = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_playstore)
        scanNow()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.CALL_PHONE
            ),
            0
        )
    }
    fun scanNow() {
        textRecognizer = TextRecognizer.Builder(this).build()
        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Dependencies are not loaded yet...please try after few moment!!", Toast.LENGTH_SHORT)
                .show()
            Log.e(tag, "Dependencies are downloading....try after few moment")
            return
        }

        //  Init camera source to use high resolution and auto focus
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(700, 700)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()

        surface_camera_preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(surface_camera_preview.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    toast("Error:" + e.message)
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }
        }
        )

        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            override fun release() {}

            @RequiresApi(Build.VERSION_CODES.S)
            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
                val items = detections.detectedItems
                Log.d("TAG", "receiveDetections: $items")


                if (items.size() <= 0) {
                    return
                }

                var aadhar = false
                var flag = 0
                tv_result.post {
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i)
                        Log.d("TAG", "receiveDetections1: ${item.value}")

                        if(item.value.findAnyOf(
                                listOf("GOVERNMENT OF INDIA"),0,true) != null) {
                            aadhar = true
                            flag = 2
                        }
                        if(aadhar && item.value.findAnyOf(listOf("female","FEMALE"),0,true) != null) { //== "female" || item.value.substring(0) == "FEMALE")
                            findViewById<TextView>(R.id.verification).visibility = View.INVISIBLE
                            flag = 1
                            toast("Verified!")
                            mCameraSource.stop()
                            val myIntent = Intent(this@VerificatonPlaystore,HomePage::class.java)
                            this@VerificatonPlaystore.startActivity(myIntent)
                        }
                    }
                    //
                    if(flag == 2)
                        toast("Not verified!")
                }
            }
        })
    }

    private fun requestForPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CAMERA
                    ),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
            }
        } else {
            // Permission has already been granted
        }
    }

    private fun isCameraPermissionGranted(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    }

    //method for toast
    fun toast(text: String) {

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    }

    //for handling permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                } else {
                    requestForPermission()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }
}

