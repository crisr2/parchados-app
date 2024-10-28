package com.appsmoviles.parchados

import android.os.Bundle
import android.webkit.WebSettings.ZoomDensity
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mGoogleMap:GoogleMap? = null
    private lateinit var autocompleFragment: AutocompleteSupportFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext,getString(R.string.google_map_api_key))
        autocompleFragment = supportFragmentManager.findFragmentById(R.id.autcomplete_fragment)
                as AutocompleteSupportFragment
        autocompleFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleFragment.setOnPlaceSelectedListener(object :PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                //val add = place.address
                val id = place.id
                val latitudes = place.latLng
                ZoomOnMap(latitudes)
            }

            override fun onError(p0: Status) {
                Toast.makeText(this@MainActivity, "Error en la busqueda", Toast.LENGTH_SHORT).show()

            }

        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync(this)

        val mapOptionButton:ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu( this,mapOptionButton)
        popupMenu.menuInflater.inflate(R.menu.map_options,popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }

        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun ZoomOnMap(latitudes:LatLng)
    {
        val newLatLngZoom = mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes,16f))

    }


    private fun changeMap(itemId: Int) {
        when(itemId)
        {
            R.id.normal_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> mGoogleMap?.mapType =  GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mGoogleMap?.mapType =  GoogleMap.MAP_TYPE_TERRAIN

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        val latitudes = LatLng(4.60971, -74.08175)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes,13f))

        googleMap.uiSettings?.isMyLocationButtonEnabled = true
        googleMap.uiSettings?.isZoomControlsEnabled = true
    }
}