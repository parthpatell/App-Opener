package com.example.finalproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.view.Gravity
import android.widget.PopupWindow
import android.view.LayoutInflater
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.*
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finalproject.R
import com.google.android.gms.maps.model.Marker
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity2 : AppCompatActivity(),GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback,LocationListener {

    private lateinit var mMap: GoogleMap
    var rcode:Int=0
    var latlngstr:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var bundle=intent.extras
        var appname= bundle?.getString("ID")
        //Put database here
        var db: SQLiteDatabase= openOrCreateDatabase("eventlist.db", Context.MODE_PRIVATE,null)
        //assignment3.mydatabase?.execSQL("DROP TABLE List")
        //assignment3.mydatabase?.execSQL("CREATE TABLE IF NOT EXISTS List(_id VARCHAR, Latitude DOUBLE,Longitude DOUBLE,Time VARCHAR, Address VARCHAR,Checkedin BOOLEAN, PRIMARY KEY(Latitude, Longitude, Time));")

        var btn5=findViewById<Button>(R.id.button5)

        btn5.setOnClickListener{
            /*
            Toast.makeText(
                applicationContext,
                latlngstr+appname,
                Toast.LENGTH_SHORT
            ).show()
            */
            db.execSQL("Update eventlist set location='$latlngstr' where _ID=$appname")

        }


        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                rcode)

            // Permission is not granted
        }


        var stopcrash=0
        while(stopcrash==0) {
            try {
                locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10000,
                        0f,
                        this
                )
                stopcrash=1
            }
            catch(e:Exception){}
        }



    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        var settings=mMap.uiSettings
        settings.isCompassEnabled=true
        settings.isZoomControlsEnabled=true
        settings.isMyLocationButtonEnabled=true
        mMap.isMyLocationEnabled=true
        mMap.setOnMyLocationButtonClickListener {onMyLocationButtonClick()}


/*

        val myloc = LatLng(lat!!, long!!)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc))

 */


        //mMap.addMarker(MarkerOptions().position(myloc).title("My Location"))




    }
/*
    override fun onMyLocationClick(location: Location){
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show()
    }

 */

    override fun onLocationChanged(location: Location) {
        var lat=location.latitude
        var long=location.longitude
        val myloc = LatLng(lat, long)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,8f))

        var placebtn=findViewById<Button>(R.id.button4)

        var gcoder:Geocoder= Geocoder(this@MapsActivity2, Locale.getDefault())
        //location_name.text.toString()


        placebtn.setOnClickListener {
            var locationname=findViewById<EditText>(R.id.edit_text2).text.toString()
            /*
            Toast.makeText(
                applicationContext,
                locationname,
                Toast.LENGTH_SHORT
            ).show()

             */
            var arry:MutableList<Address>?=null
            try {

                arry = gcoder.getFromLocationName(
                    locationname,
                    2,
                    lat!! - .1,
                    long!! - .1,
                    lat!! + .1,
                    long!! + .1
                )


            }
            catch(e:Exception){}

            for (i in 0 until arry!!.size) {
                try {
                    var theloc = LatLng(arry[i].latitude, arry[i].longitude)
                    var marker_place: Marker =
                        mMap.addMarker(MarkerOptions().position(theloc).title(locationname))
                        mMap.setOnMarkerClickListener(object: GoogleMap.OnMarkerClickListener{
                            override fun onMarkerClick(m:Marker):Boolean {
                                val inflater = this@MapsActivity2.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                var inflatey = inflater.inflate(R.layout.pop_up, null, false)
                                val pw = PopupWindow(inflatey, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT , true)
                                var thebutton = inflatey.findViewById<Button>(R.id.button3)

                                pw.showAtLocation(this@MapsActivity2.findViewById(R.id.map), Gravity.CENTER, 0, 0)

                                thebutton.setOnClickListener{

                                    var latty=m.position.latitude
                                    var longy= m.position.longitude
                                    //right here you add this pressed location to the string
                                    latlngstr=latlngstr+"$latty,$longy;"
                                    pw.dismiss()
                                }
                                return true
                            }
                        })
                    //tv2.text = "${arry[0].latitude} and ${arry[0].longitude}"
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "No location found nearby",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        mMap.setOnMapClickListener(object: GoogleMap.OnMapClickListener{
            override fun onMapClick(point: LatLng) {
                val inflater = this@MapsActivity2.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var inflatey = inflater.inflate(R.layout.pop_up, null, false)
                val pw = PopupWindow(inflatey, 800, 400, true)
                var thebutton = inflatey.findViewById<Button>(R.id.button3)

                pw.showAtLocation(this@MapsActivity2.findViewById(R.id.map), Gravity.CENTER, 0, 0)

                thebutton.setOnClickListener{

                    var latty=point.latitude
                    var longy= point.longitude
                    var locky= LatLng(latty,longy)
                    var marker2=mMap.addMarker(MarkerOptions().position(locky))
                    //right here you add this pressed location to the string
                    latlngstr=latlngstr+"$latty,$longy;"
                    pw.dismiss()
                }



            }
        }
        )


    }

    override fun onMyLocationButtonClick(): Boolean {

        return false
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("Latitude","status");
    }

    override fun onProviderEnabled(provider: String?) {
        Log.d("Latitude","enable");
    }

    override fun onProviderDisabled(provider: String?) {
        Log.d("Latitude","disable");
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            rcode -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
