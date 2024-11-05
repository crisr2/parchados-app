package com.appsmoviles.parchados

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.appsmoviles.parchados.models.Eventos
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import java.io.InputStream
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var mGoogleMap: GoogleMap? = null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private var currentMarker: Marker? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var database: DatabaseReference
    private val existingCoordinates = mutableListOf<String>()
    private var eventoId: String? = null


    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicialización de Places y Firebase
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        database = FirebaseDatabase.getInstance().getReference("eventos")

        // Configuración del AutocompleteSupportFragment
        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autcomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        val limitesBusqueda = RectangularBounds.newInstance(
            LatLng(4.4699, -74.2144),
            LatLng(4.8373, -73.9866)
        )
        autocompleteFragment.setLocationBias(limitesBusqueda)

        // Listener para selección de lugar
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                latLng?.let { ZoomOnMap(it) }
                if (latLng != null) {
                    addMarkerOnMap(latLng)
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(this@MainActivity, "Error en la búsqueda: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        // Configuración del DrawerLayout
        drawer = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Configuración de la vista del mapa
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Filtro de categorias
        val categoryFilterButton: ImageButton = findViewById(R.id.categoryFilterButton)
        val categoryMenu = PopupMenu(this, categoryFilterButton)
        categoryMenu.menuInflater.inflate(R.menu.category_menu, categoryMenu.menu)

        categoryMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.category_all -> {
                    addMarkers(null)
                }
                R.id.category_sport -> {
                    addMarkers("Deporte")
                }
                R.id.category_entertainment -> {
                    addMarkers("Entretenimiento")
                }
                R.id.category_food -> {
                    addMarkers("Comida")
                }
            }
            true
        }
        categoryFilterButton.setOnClickListener {
            categoryMenu.show()
        }

        // Filtro de localidades
        val locationFilterButton: ImageButton = findViewById(R.id.locationFilterButton)
        val locationMenu = PopupMenu(this, locationFilterButton)
        locationMenu.menuInflater.inflate(R.menu.localidades_menu, locationMenu.menu)

        locationMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.localidad_all -> {
                    addMarkersByLocalidad(null) // Muestra todos sin filtrar por localidad
                }
                R.id.localidad_antonio_narino -> {
                    addMarkersByLocalidad("Antonio Nariño")
                }
                R.id.localidad_barrios_unidos -> {
                    addMarkersByLocalidad("Barrios Unidos")
                }
                R.id.localidad_bosa -> {
                    addMarkersByLocalidad("Bosa")
                }
                R.id.localidad_candelaria -> {
                    addMarkersByLocalidad("Candelaria")
                }
                R.id.localidad_chapinero -> {
                    addMarkersByLocalidad("Chapinero")
                }
                R.id.localidad_ciudad_bolivar -> {
                    addMarkersByLocalidad("Ciudad Bolívar")
                }
                R.id.localidad_engativa -> {
                    addMarkersByLocalidad("Engativá")
                }
                R.id.localidad_fontibon -> {
                    addMarkersByLocalidad("Fontibón")
                }
                R.id.localidad_kennedy -> {
                    addMarkersByLocalidad("Kennedy")
                }
                R.id.localidad_los_martires -> {
                    addMarkersByLocalidad("Los Mártires")
                }
                R.id.localidad_puente_aranda -> {
                    addMarkersByLocalidad("Puente Aranda")
                }
                R.id.localidad_rafael_uribe -> {
                    addMarkersByLocalidad("Rafael Uribe Uribe")
                }
                R.id.localidad_san_cristobal -> {
                    addMarkersByLocalidad("San Cristóbal")
                }
                R.id.localidad_santa_fe -> {
                    addMarkersByLocalidad("Santa Fe")
                }
                R.id.localidad_suba -> {
                    addMarkersByLocalidad("Suba")
                }
                R.id.localidad_sumapaz -> {
                    addMarkersByLocalidad("Sumapaz")
                }
                R.id.localidad_teusaquillo -> {
                    addMarkersByLocalidad("Teusaquillo")
                }
                R.id.localidad_tunjuelito -> {
                    addMarkersByLocalidad("Tunjuelito")
                }
                R.id.localidad_usaquen -> {
                    addMarkersByLocalidad("Usaquén")
                }
                R.id.localidad_usme -> {
                    addMarkersByLocalidad("Usme")
                }
            }
            true
        }
        locationFilterButton.setOnClickListener {
            locationMenu.show()
        }
    }

    private fun ZoomOnMap(latitudes: LatLng) {
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes, 16f))
    }

    private fun addMarkerOnMap(position: LatLng) {
        val titleWithCoordinates = "${position.latitude}, ${position.longitude}"
        currentMarker?.remove()
        currentMarker = mGoogleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title(titleWithCoordinates)
        )
        currentMarker?.showInfoWindow()
    }

    private fun changeMap(itemId: Int) {
        when(itemId) {
            R.id.normal_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satellite_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        val initialLatLng = LatLng(4.60971, -74.08175)
        //googleMap?
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 13f))

        mGoogleMap!!.uiSettings.isMyLocationButtonEnabled = true
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
        mGoogleMap!!.uiSettings.isZoomGesturesEnabled = true
        mGoogleMap!!.uiSettings.isScrollGesturesEnabled = true

        loadMarkersFromFirebase(null)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        mGoogleMap?.setOnMarkerClickListener { marker ->
            val coordinates = "${marker.position.latitude},${marker.position.longitude}"

            if (!existingCoordinates.contains(coordinates)) {
                // Copiar las coordenadas al portapapeles
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Coordinates", coordinates)
                clipboard.setPrimaryClip(clip)
            }

            eventoId = marker.tag as? String  // Asegúrate de asignar el tag cuando crees el marcador
            eventoId?.let {
                mostrarDialogoEvento(it)
            }

            false
            //false
        }
    }

    // Se abre el evento
    @SuppressLint("InflateParams", "SetTextI18n")
    private fun mostrarDialogoEvento(eventoId: String) {

        val eventoRef = database.child(eventoId)

        eventoRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Cargar los datos del evento
                val titulo = snapshot.child("titulo").value.toString()
                val descripcion = snapshot.child("descripcion").value.toString()
                val horario = snapshot.child("horainicio").value.toString() + " - " + snapshot.child("horafinal").value.toString()
                val telefono = snapshot.child("telefono").value.toString()
                val precio = snapshot.child("precio").value.toString()
                val categoria = snapshot.child("categoria").value.toString()
                val direccion = snapshot.child("direccion").value.toString()
                val instagram = snapshot.child("instagram").value.toString()
                val tiktok = snapshot.child("tiktok").value.toString()
                val url = snapshot.child("url").value.toString()

                // Inflar el layout del Dialog
                val dialogView = layoutInflater.inflate(R.layout.event_item, null)
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(dialogView)

                // Asignar valores a los elementos del layout
                dialogView.findViewById<TextView>(R.id.titleTextView).text = titulo
                dialogView.findViewById<TextView>(R.id.descriptionTextView).text = descripcion
                dialogView.findViewById<TextView>(R.id.horarioTextView).text = "Horario: $horario"
                dialogView.findViewById<TextView>(R.id.telefonoTextView).text = "Teléfono: $telefono"
                dialogView.findViewById<TextView>(R.id.precioTextView).text = "Entrada: $$precio"
                dialogView.findViewById<TextView>(R.id.categoriaTextView).text = categoria
                dialogView.findViewById<TextView>(R.id.direccionTextView).text = "Dirección: $direccion"

                // Configurar los íconos de redes sociales
                val instagramIcon = dialogView.findViewById<ImageView>(R.id.instagramIcon)
                instagramIcon.setOnClickListener { abrirEnlace("https://instagram.com/${instagram}") }

                val tiktokIcon = dialogView.findViewById<ImageView>(R.id.tiktokIcon)
                tiktokIcon.setOnClickListener { abrirEnlace("https://tiktok.com/@${tiktok}") }

                val websiteIcon = dialogView.findViewById<ImageView>(R.id.websiteIcon)
                websiteIcon.setOnClickListener { abrirEnlace(url) }

                // Configurar el icono de categoría
                val categoriaIcono = dialogView.findViewById<ImageView>(R.id.categoryIcon) // Asegúrate de tener un ImageView para el icono de categoría en tu layout

                // Asignar el icono según la categoría
                val icono = when (categoria) {
                    "Deportes" -> R.drawable.ic_deportes
                    "Comida" -> R.drawable.ic_comida
                    else -> R.drawable.ic_entretenimiento
                }
                categoriaIcono.setImageResource(icono)

                // Mostrar el Dialog
                bottomSheetDialog.show()
            } else {
                Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirEnlace(url: String) {
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap?.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMarkersFromFirebase(cat: String?) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mGoogleMap?.clear() // Limpia el mapa para evitar duplicados
                existingCoordinates.clear() // Limpia la lista de coordenadas para evitar duplicados

                for (eventSnapshot in snapshot.children) {
                    val evento = eventSnapshot.getValue(Eventos::class.java)
                    evento?.let {
                        try {
                            // Parseo de la ubicación
                            val (lat, lng) = it.ubicacion?.split(",")!!.map { coord -> coord.toDouble() }
                            val position = LatLng(lat, lng)

                            // Almacena la coordenada en formato "lat,lng" en la lista de coordenadas existentes
                            existingCoordinates.add("${lat},${lng}")

                            // Asigna el color según la categoría
                            val color = when (it.categoria) {
                                "Deportes" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                                "Comida" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                "Entretenimiento" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) // Color por defecto
                            }

                            // Agregar solo los marcadores que coincidan con la categoría seleccionada
                            if (it.categoria == cat) {
                                // Crea el marcador
                                val marker = mGoogleMap?.addMarker(
                                    MarkerOptions()
                                        .position(position)
                                        .title(it.titulo)
                                        .icon(color)
                                )
                                marker?.tag = eventSnapshot.key
                            }
                            else if (cat == null){
                                // Crea el marcador
                                val marker = mGoogleMap?.addMarker(
                                    MarkerOptions()
                                        .position(position)
                                        .title(it.titulo)
                                        .icon(color)
                                )
                                marker?.tag = eventSnapshot.key
                            } else {
                                //?
                            }

                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error parsing location for ${it.titulo}: ${e.message}")
                        }
                    }
                }

                // Centra el mapa en la última ubicación agregada (opcional)
                snapshot.children.lastOrNull()?.getValue(Eventos::class.java)?.let { lastEvent ->
                    val (lat, lng) = lastEvent.ubicacion?.split(",")!!.map { coord -> coord.toDouble() }
                    mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 10f))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to load markers: ${error.message}")
            }
        })
    }

    // Función para filtrar marcadores por localidad
    private fun addMarkersByLocalidad(localidad: String?) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mGoogleMap?.clear() // Limpia el mapa para evitar duplicados
                existingCoordinates.clear() // Limpia la lista de coordenadas para evitar duplicados

                for (eventSnapshot in snapshot.children) {
                    val evento = eventSnapshot.getValue(Eventos::class.java)
                    evento?.let {
                        try {
                            // Parseo de la ubicación
                            val (lat, lng) = it.ubicacion?.split(",")!!.map { coord -> coord.toDouble() }
                            val position = LatLng(lat, lng)

                            // Almacena la coordenada en formato "lat,lng" en la lista de coordenadas existentes
                            existingCoordinates.add("${lat},${lng}")

                            // Asigna el color según la categoría
                            val color = when (it.categoria) {
                                "Deportes" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                                "Comida" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                                "Entretenimiento" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) // Color por defecto
                            }

                            if (localidad == null || it.localidad == localidad) {
                                // Crea el marcador
                                val marker = mGoogleMap?.addMarker(
                                    MarkerOptions()
                                        .position(position)
                                        .title(it.titulo)
                                        .icon(color)
                                )
                                marker?.tag = eventSnapshot.key
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Error al cargar marcadores desde JSON", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // Centra el mapa en la última ubicación agregada (opcional)
                snapshot.children.lastOrNull()?.getValue(Eventos::class.java)?.let { lastEvent ->
                    val (lat, lng) = lastEvent.ubicacion?.split(",")!!.map { coord -> coord.toDouble() }
                    mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 10f))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to load markers: ${error.message}")
            }
        })
    }


    // Manejo de navegación
    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_one -> {
                // Acción para el primer elemento
            }
            R.id.nav_item_two -> {
                val intent = Intent(this, FormsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_three -> {
                // Acción para el primer elemento
            }
            R.id.nav_item_four -> {
                // Acción para el segundo elemento
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
