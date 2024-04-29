package com.example.mytravelers.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import com.example.mytravelers.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mytravelers.databinding.ActivityMapsBinding
import android.location.Geocoder
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var binding: ActivityMapsBinding
    private var ACCESS_LOCATION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // We can show user dialog why this permission is necessary
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_REQUEST_CODE)
                }
            }

            initView()
        }

    private fun initView() {
        with(binding) {
            val hideSearchBar = intent.getBooleanExtra("hideSearchBar", false)

            if (hideSearchBar) {
                svSearch.visibility = android.view.View.GONE
                fbAdd.visibility = android.view.View.GONE

                // Get the latitude and longitude from the intent extras
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)

                // Move the camera to the location
                val location = LatLng(latitude, longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))

                // Add a marker with a red icon
                mMap.addMarker(MarkerOptions().position(location).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

            } else {
                svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val location = svSearch.query.toString()
                        var addressList: List<android.location.Address>? = null
                        if (location != null && location != "") {
                            val geocoder = Geocoder(this@MapsActivity)
                            try {
                                addressList = geocoder.getFromLocationName(location, 1)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            if (!addressList.isNullOrEmpty()) {
                                val address = addressList[0]
                                val latLng = LatLng(address.latitude, address.longitude)
                                mMap.addMarker(MarkerOptions().position(latLng).title(location))
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                            }
                        }
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })

                fbAdd.setOnClickListener {
                    addLocation()
                }
            }
        }
    }

    private fun addLocation() {
        // Get the selected location's latitude and longitude
        val latLng = mMap.cameraPosition.target

        // Extract latitude and longitude from the LatLng object
        val selectedLatitude = latLng.latitude
        val selectedLongitude = latLng.longitude

        // Log the latitude and longitude
        Log.d("AddLocation", "Latitude: $selectedLatitude, Longitude: $selectedLongitude")

        // Pass latitude and longitude to PackageActivity
        val intent = Intent(this, PackageActivity::class.java).apply {
            putExtra("latitude", selectedLatitude)
            putExtra("longitude", selectedLongitude)
        }
        startActivity(intent)
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                // We can show a dialog that permission is not granted
            }
        }
    }
}