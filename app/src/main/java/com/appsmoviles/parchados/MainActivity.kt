package com.appsmoviles.parchados

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import org.json.JSONArray
import java.io.InputStream
import android.Manifest

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var mGoogleMap: GoogleMap? = null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private var currentMarker: Marker? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autcomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        val limitesBusqueda = RectangularBounds.newInstance(
            LatLng(4.4699, -74.2144),
            LatLng(4.8373, -73.9866)
        )
        autocompleteFragment.setLocationBias(limitesBusqueda)

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                latLng?.let { ZoomOnMap(it) }
                if (latLng != null) {
                    addMarkerOnMap(latLng, place.name ?: place.name)
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(this@MainActivity, "Error en la búsqueda: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val mapOptionButton: ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(this, mapOptionButton)
        popupMenu.menuInflater.inflate(R.menu.map_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }

        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun ZoomOnMap(latitudes: LatLng) {
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes, 16f))
    }

    private fun addMarkerOnMap(position: LatLng, title: String) {
        currentMarker?.remove()

        currentMarker = mGoogleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
        )
        currentMarker?.showInfoWindow()
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        val initialLatLng = LatLng(4.60971, -74.08175)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 13f))

        googleMap.uiSettings?.isMyLocationButtonEnabled = true
        googleMap.uiSettings?.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap?.isMyLocationEnabled = true
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
