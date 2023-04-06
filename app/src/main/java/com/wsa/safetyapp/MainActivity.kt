package com.wsa.safetyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*



class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    lateinit var db: FirebaseFirestore
    public var coordinates = ""
    var routeHelp=""

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.sheetTv).movementMethod = ScrollingMovementMethod()

        BottomSheetBehavior.from(sheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
           // onNestedScroll()
            //isNestedScrollingCheckEnabled = true
        }

//        val intent = intent
//        val value = intent.getStringExtra("key1")
        // Initialize Google Maps and its callbacks
        idFABFeedback.setOnClickListener {
            val myIntent = Intent(this, FeedbackForm::class.java)
//            myIntent.putExtra("key1", "login")
            this.startActivity(myIntent)
        }

        //NOTIFICATION FOR LOCATION COORDS


        idStart.setOnClickListener{
            Intent(this,LocationService::class.java)
                .apply {
                    action=LocationService.START
                    startService(this)
                }
            if (c1.toString() == "" || c2.toString() == "" || c3.toString() == "") {
                return@setOnClickListener
            }else
            Toast.makeText(applicationContext,"Current Location Sent",Toast.LENGTH_SHORT).show()
            //  STEPS ADDED IN ROUTE WINDOW
//            val routeWindow=findViewById<TextView>(R.id.routeInfo)
//            routeWindow.setText(routeHelp)
        }

        idStop.setOnClickListener{
            Intent(this,LocationService::class.java)
                .apply {
                    action=LocationService.STOP
                    startService(this)
                }
            Toast.makeText(this, "Please fill the feedback form", Toast.LENGTH_LONG).show()
        }

        //END OF LOCATION
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {

        val cood= LatLng(13.067439, 80.237617)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(cood, 12f)

        googleMap?.animateCamera(cameraUpdate)
        this.googleMap = googleMap
        googleMap?.uiSettings?.apply {
            isMyLocationButtonEnabled=true
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap?.isMyLocationEnabled=true
        googleMap?.setPadding(0,250,0,0)

//        added clear view option
        clearBtn.setOnClickListener {
            this.googleMap!!.clear()
            routeInfo.isVisible = !routeInfo.isVisible
            val starts=findViewById<EditText>(R.id.startAddress)
            starts.setText("")
            val ends=findViewById<EditText>(R.id.destinationAddress)
            ends.setText("")
            val routeWindow=findViewById<TextView>(R.id.routeInfo)
            routeWindow.setText("No Route Info Provided")
            findViewById<TextView>(R.id.sheetTv).text = "Steps will be displayed here on search."
        }

//        added search route option
        searchBtn.setOnClickListener {
            findViewById<TextView>(R.id.sheetTv).text = "Steps will be displayed here on search."
            routeInfo.isVisible = !routeInfo.isVisible
            var start = startAddress.text.toString()
            var destination = destinationAddress.text.toString()
            Log.d("TAG", "onMapReady: $start")
            Log.d("TAG", "onMapReady: $destination")

            var latLngOrigin = getLocationFromAddress(start)
            var latLngDestination = getLocationFromAddress(destination)

            if(start=="" || destination==""){
                Toast.makeText(applicationContext,"Enter valid start/destination address",Toast.LENGTH_SHORT).show()

            }
            else{

                this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title(start))
                this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination)
                    .title(destination))
                this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
//            val path: MutableList<List<LatLng>> = ArrayList()
                val urlDirections =
                    "https://maps.googleapis.com/maps/api/directions/json?origin=${latLngOrigin?.latitude},${latLngOrigin?.longitude}&destination=${latLngDestination?.latitude},${latLngDestination?.longitude}&mode=walking&alternatives=true&key=AIzaSyDxRfxqFdgUlccCFu65mDq2C9Ao33G9q4A"
//            Log.d("latlg","${latLngDestination?.latitude}+${latLngDestination?.longitude}")
                val directionsRequest = @SuppressLint("SetTextI18n")
                object : StringRequest(Request.Method.GET,
                    urlDirections,
                    Response.Listener<String> { response ->
                        val jsonResponse = JSONObject(response)
                        // Get routes
                        val routes = jsonResponse.getJSONArray("routes")
                        Log.d("size",
                            "${routes.length()}+${latLngOrigin?.latitude}+${latLngOrigin?.longitude}+${latLngDestination?.latitude}+${latLngDestination?.longitude}")
                        //maximum five possible routes

                        if (routes.length() == 1) {
                            Log.d("size", "${routes.getJSONObject(0).get("summary")}")
                            val legs = routes.getJSONObject(0).getJSONArray("legs")
                            val dist = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                            val eta = legs.getJSONObject(0).getJSONObject("duration").getString("text")

                            //log ETA and distance

                            var result = ""

                            Log.d("mul", "${dist}+${eta}")
                            result = StringBuilder().append("Distance covered: ").append(dist)
                                .append(" ETA: ").append(eta).append("\n").toString()

                            val steps = legs.getJSONObject(0).getJSONArray("steps")
                            val path: MutableList<List<LatLng>> = ArrayList()

                            //DIRECTIONS STEPS

                            val content = Html.fromHtml(steps.getJSONObject(0).getString("html_instructions")).toString()
                            var routeSteps=StringBuilder().append("Step 1: ").append(content)
                                .appendLine().toString()
                            routeHelp+=routeSteps
                            findViewById<TextView>(R.id.sheetTv).text=""
                            findViewById<TextView>(R.id.sheetTv).append(routeSteps)
                            for(i in 1 until steps.length()){
                                val steps=steps.getJSONObject(i).getString("html_instructions")
                                val content = Html.fromHtml(steps).toString()
                                routeSteps=StringBuilder().append("Step ").append(i+1).append(": ").append(content)
                                    .appendLine().toString()
                                routeHelp+=routeSteps
                                findViewById<TextView>(R.id.sheetTv).append( routeSteps )
                            }

                            //STEPS EXTRACTED

                            for (i in 0 until steps.length()) {
                                val points =
                                    steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                                path.add(PolyUtil.decode(points))
                            }
                            Log.d("psize", "${path.size}")
                            for (i in 0 until path.size) {
                                this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i])
                                    .color(Color.parseColor("#FF8BC34A")))

                            }
                            val texts = findViewById<TextView>(R.id.routeInfo)
                            texts.movementMethod = ScrollingMovementMethod()
                            Log.d("ress", "${result}")

                            val word: Spannable = SpannableString(result)

                            word.setSpan(ForegroundColorSpan(Color.parseColor("#FF8BC34A")),
                                0,
                                word.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                            texts.setText(word)
                        }

                        if (routes.length() > 1) {
                            val cols =
                                arrayOf(Color.parseColor("#FF8BC34A"),
                                    Color.MAGENTA,
                                    Color.BLUE,
                                    Color.BLACK,
                                    Color.CYAN)
                            var counter = -1
                            var mresult: String = ""

                            val texts = findViewById<TextView>(R.id.routeInfo)
                            val word: Spannable = SpannableString(mresult)

                            word.setSpan(ForegroundColorSpan(Color.parseColor("#FF8BC34A")),
                                0,
                                word.length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                            texts.setText(word)

                            //index for saferoute
                            val res = findSafestRoute(routes)

                            //FETCH DIRECTION STEPS
                            val steps=routes.getJSONObject(res).getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
                            val content = Html.fromHtml(steps.getJSONObject(res).getString("html_instructions")).toString()
                            var routeSteps=StringBuilder().append("Step 1: ").append(content)
                                .appendLine().toString()
                            routeHelp+=routeSteps
                            findViewById<TextView>(R.id.sheetTv).text=""
                            findViewById<TextView>(R.id.sheetTv).append(routeSteps)
                            for(i in 1 until steps.length()){
                                val steps=steps.getJSONObject(i).getString("html_instructions")
                                val content = Html.fromHtml(steps).toString()
                                routeSteps=StringBuilder().append("Step ").append(i+1).append(": ").append(content)
                                    .appendLine().toString()
                                routeHelp+=routeSteps
                                findViewById<TextView>(R.id.sheetTv).append(routeSteps)
                            }
                            //END OF STEPS EXTRACTION


                            for (j in 0 until routes.length()) {
                                //maximum 5 routes allowed
                                if (j == 5) {
                                    break;
                                }
                                val jsonObject = routes.getJSONObject(j)
                                val legs = jsonObject.getJSONArray("legs")
                                val dist =
                                    legs.getJSONObject(0).getJSONObject("distance").getString("text")
                                val eta =
                                    legs.getJSONObject(0).getJSONObject("duration").getString("text")

                                //log ETA and distance

                                Log.d("mul", "${dist}+${eta}")
                                counter += 1

                                val routeInfo =
                                    StringBuilder().append("Distance covered: ").append(dist)
                                        .append(" ETA: ").appendLine(eta).appendLine().toString()
                                val wordTwo: Spannable = SpannableString(routeInfo)

                                wordTwo.setSpan(ForegroundColorSpan(cols[counter]),
                                    0,
                                    wordTwo.length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                texts.append(wordTwo)



                                Log.d("countss", "${counter}")
                                val steps = legs.getJSONObject(0).getJSONArray("steps")
                                val path: MutableList<List<LatLng>> = ArrayList()

                                for (i in 0 until steps.length()) {
                                    val points =
                                        steps.getJSONObject(i).getJSONObject("polyline")
                                            .getString("points")
                                    path.add(PolyUtil.decode(points))
                                }
                                Log.d("psize", "${path.size}")
                                for (i in 0 until path.size) {
                                    this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i])
                                        .color(cols[counter]))
                                }
                            }
                        }

                    },
                    Response.ErrorListener { _ ->
                    }) {}

                val requestQueue = Volley.newRequestQueue(this)
                requestQueue.add(directionsRequest)

            }
        }
    }

    private fun getTimeFromAndroid(): String {
        val dt = LocalDateTime.now()
        val currentTime: String = SimpleDateFormat("kk:mm:ss", Locale.getDefault()).format(Date())
        val hours=currentTime.split(":").get(0).toInt()
        Log.d("timee","${currentTime}")
        if (hours >= 1 && hours <= 12) {
            return "Morning"
        } else if (hours >= 12 && hours <= 16) {
            return "Noon"
        } else if (hours >= 16 && hours <= 21) {
            return "Evening"
        } else if (hours >= 21 && hours <= 24) {
            return "Night"
        }
        return ""
    }

    private fun findSafestRoute(routes: JSONArray): Int {
        var final: Int = 0
        var safetylevels : MutableList<Int> = mutableListOf()

        for (j in 0 until routes.length()) {
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            val l1 :MutableList<String> = ArrayList()
            for (x in 0 until steps.length()) {
                val lat =
                    steps.getJSONObject(x).getJSONObject("end_location").getString("lat").toDouble()
                val lng =
                    steps.getJSONObject(x).getJSONObject("end_location").getString("lng").toDouble()
                Log.d("coords", "${lat}+${lng}")

                var locality = ""

                @Suppress("DEPRECATION")
                fun Geocoder.getAddress(
                    latitude: Double,
                    longitude: Double,
                    address: (android.location.Address?) -> Unit,
                ) {

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
//                        return
//                    }

                    try {
                        address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
                    } catch (e: Exception) {
                        //will catch if there is an internet problem
                        address(null)
                    }
                }

                Geocoder(this, Locale("in"))
                    .getAddress(lat, lng) { address: android.location.Address? ->
                        if (address != null) {
                            //do your logic
                            locality = address.subLocality.toString()

                        }
                    }

                if (!(locality in l1) && locality != "" && locality!=" ") {
                    l1.add(locality.lowercase())
                    Log.d("locs", "${j}+${locality}")
                }

            }

            //RETRIEVE DISTINCT SUB LOCALITIES
            val l2=l1.distinct()

            var safIndex=0
            val timeOfTheDay=getTimeFromAndroid().lowercase()

            data class Fields(
                val area: String = "",
                val is_bar: String = "",
                val isPolice_Station: String = "",
                val peopleFrequency: String = "",
                val time: String = "",
                val zone: String = "",
            )

            val db = FirebaseFirestore.getInstance()

            db.collection("Feedback")
                .get()
                .addOnCompleteListener() { task ->

                    if (task.isSuccessful()) {

                        for (document in task.getResult()) {
                            Log.d("vanako", document.getId() + " => " + document.getData());

                            val name = document.get("feed").toString()
                            val list=name.split("},")
                            var flag=0
                            for(jo in list) {
                                val sublist=jo.split(",")
                                flag=0
                                for(ko in sublist){
                                    val group=ko
                                    group.removePrefix("[")
                                    group.removePrefix(" {")
                                    val fields=group.split("=")
//                                    Log.d("areass","${fields.get(1)}")

                                    if(fields.get(1) in l2){
//                                        Log.d("areass","${fields.get(1)}")
//                                        Log.d("areass","${fields.size}")
                                        flag=1
                                    }
                                    if(flag==1) {
                                        Log.d("areass", "${fields.get(0)}+${fields.get(1)}")

//                                        Log.d("yes","["+f1+f2)
                                        if (fields.get(0)==" police_Station") {
                                            if (fields.get(1) == "yes") {
                                                safIndex += 10
                                                Log.d("calc", "${safIndex}")
                                            }
                                        }

                                        if(fields.get(0)==" _bar") {
                                            if (fields.get(1) == "yes") {
                                                safIndex -= 3
                                            }
                                        }

                                        if(fields.get(0)==" time") {
                                            if (fields.get(1) == timeOfTheDay) {
                                                safIndex += 4
                                            }
                                        }


                                        if(fields.get(0)==" peopleFrequency"){
                                            if(fields.get(1)=="low"){
                                                safIndex-=2
                                            }
                                            else if(fields.get(1)=="medium"){
                                                safIndex+=3
                                            }
                                            else if(fields.get(1)=="high}]"){
                                                safIndex+=7
                                            }
                                            Log.d("calc","${safIndex}")
                                        }

                                    }

                                }
                                Log.d("userss", "${jo}")
                            }
                        }

                    } else {
//                            Log.w(TAG, "Error getting documents.", task.getException());
                    }
                    Log.d("safindex","${safIndex}+${timeOfTheDay}")
                    if(!safetylevels.isEmpty() && safIndex> safetylevels.maxOrNull()!!)
                        final=j
                    safetylevels.add(safIndex)

                }


        }

        return final
    }

    fun getLocationFromAddress(strAddress: String): LatLng? {
        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress,5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            p1 = LatLng(location.latitude, location.longitude)

            return p1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}