package com.appsmoviles.parchados

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioButton
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
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

        // Mostrar filtros
        val openFilterButton = findViewById<ImageButton>(R.id.openFilter)
        openFilterButton.setOnClickListener {
            val filterView = layoutInflater.inflate(R.layout.filter_item, null)
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.window?.setDimAmount(0f)
            bottomSheetDialog.setContentView(filterView)

            // Acceder al filtro de categorias
            val categoryFilterButton: Button = filterView.findViewById(R.id.categoryFilterButton)
            categoryFilterButton.setOnClickListener {
                showCategoryBottomSheet() // Función que crearemos para mostrar las categorías
            }

            // Acceder al filtro de localidades
            val locationFilterButton: Button = filterView.findViewById(R.id.locationFilterButton)
            locationFilterButton.setOnClickListener {
                showLocationBottomSheet(bottomSheetDialog) // Pasamos el diálogo de filtros
            }
            // Mostrar el BottomSheet
            bottomSheetDialog.show()
        }

    }

    // Función para mostrar el Bottom Sheet de localidades, cerrando también el diálogo de filtros
    private fun showLocationBottomSheet(filterBottomSheetDialog: BottomSheetDialog) {
        val locationView = layoutInflater.inflate(R.layout.location_item, null)
        val locationBottomSheetDialog = BottomSheetDialog(this)
        locationBottomSheetDialog.window?.setDimAmount(0f)
        locationBottomSheetDialog.setContentView(locationView)

        // Obtener las localidades existentes de los marcadores y mostrarlas en el Bottom Sheet
        val locationContainer = locationView.findViewById<LinearLayout>(R.id.locationContainer)
        getLocationsFromFirebase { existingLocations ->
            existingLocations.forEach { location ->
                val button = Button(this).apply {
                    text = location
                    background = null  // Sin fondo
                    setTextColor(ContextCompat.getColor(context, R.color.black)) // Color de texto
                    elevation = 8f     // Elevación
                    setPadding(0, 20, 0, 20) // Padding opcional para un diseño más limpio
                    setOnClickListener {
                        addMarkersByLocalidad(location)
                        locationBottomSheetDialog.dismiss()
                    }
                }
                locationContainer.addView(button)
            }

            // Agregar botón para mostrar todas las localidades
            val allLocationsButton = Button(this).apply {
                text = "Todas las localidades"
                background = null
                setTextColor(ContextCompat.getColor(context, R.color.black))
                elevation = 8f
                setPadding(0, 20, 0, 20)
                setTypeface(null, Typeface.BOLD)
                setOnClickListener {
                    addMarkersByLocalidad(null)  // Mostrar todos los marcadores
                    locationBottomSheetDialog.dismiss()
                }
            }
            locationContainer.addView(allLocationsButton)
        }

        // Mostrar el Bottom Sheet de localidades
        locationBottomSheetDialog.show()
    }

    // Función para obtener localidades únicas de Firebase
    private fun getLocationsFromFirebase(onLocationsLoaded: (Set<String>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("eventos")
        val uniqueLocations = mutableSetOf<String>()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { eventSnapshot ->
                    val localidad = eventSnapshot.child("localidad").getValue(String::class.java)
                    if (localidad != null) {
                        uniqueLocations.add(localidad)
                    }
                }
                onLocationsLoaded(uniqueLocations)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error en la lectura de Firebase si es necesario
            }
        })
    }

    // Función para mostrar el Bottom Sheet de categorías
    private fun showCategoryBottomSheet() {
        val categoryView = layoutInflater.inflate(R.layout.category_item, null)
        val categoryBottomSheet = BottomSheetDialog(this)
        categoryBottomSheet.window?.setDimAmount(0f)
        categoryBottomSheet.setContentView(categoryView)

        // Configurar listeners para cada botón en el Bottom Sheet de categorías
        categoryView.findViewById<RadioButton>(R.id.category_all).setOnClickListener {
            loadMarkersFromFirebase(null)
            categoryBottomSheet.dismiss()
        }

        categoryView.findViewById<RadioButton>(R.id.category_sport).setOnClickListener {
            loadMarkersFromFirebase("Deportes")
            categoryBottomSheet.dismiss()
        }

        categoryView.findViewById<RadioButton>(R.id.category_entertainment).setOnClickListener {
            loadMarkersFromFirebase("Entretenimiento")
            categoryBottomSheet.dismiss()
        }

        categoryView.findViewById<RadioButton>(R.id.category_food).setOnClickListener {
            loadMarkersFromFirebase("Comida")
            categoryBottomSheet.dismiss()
        }

        // Mostrar el Bottom Sheet de categorías
        categoryBottomSheet.show()
    }


    private fun ZoomOnMap(latitudes: LatLng) {
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes, 16f))
    }

    private fun ZoomOnIt(latitudes: LatLng) {
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudes, 12f))
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 13f))

        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true

        loadMarkersFromFirebase(null)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        mGoogleMap?.setOnMarkerClickListener { marker ->
            val currentLocation = LatLng(mGoogleMap?.myLocation?.latitude ?: 0.0, mGoogleMap?.myLocation?.longitude ?: 0.0)
            val origin = "${currentLocation.latitude},${currentLocation.longitude}"
            val coordinates = "${marker.position.latitude},${marker.position.longitude}"

            if (!existingCoordinates.contains(coordinates)) {
                // Copiar las coordenadas al portapapeles
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Coordinates", coordinates)
                clipboard.setPrimaryClip(clip)
            }

            // Llamar a la función ZoomOnIt para hacer zoom en el marcador
            ZoomOnIt(marker.position)



            eventoId = marker.tag as? String  // Asegúrate de asignar el tag cuando crees el marcador
            eventoId?.let {
                mostrarDialogoEvento(it, origin, coordinates)
            }

            false
            //false
        }
    }

    // Se abre el evento
    @SuppressLint("InflateParams", "SetTextI18n")
    private fun mostrarDialogoEvento(eventoId: String, origen: String, destino: String) {

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
                instagramIcon.setOnClickListener {
                    if (instagram.isNotEmpty()) {
                        abrirEnlace("https://instagram.com/$instagram")
                    } else {
                        Toast.makeText(this, "El enlace de Instagram está vacío", Toast.LENGTH_SHORT).show()
                    }
                }

                val tiktokIcon = dialogView.findViewById<ImageView>(R.id.tiktokIcon)
                tiktokIcon.setOnClickListener {
                    if (tiktok.isNotEmpty()) {
                        abrirEnlace("https://tiktok.com/@$tiktok")
                    } else {
                        Toast.makeText(this, "El enlace de TikTok está vacío", Toast.LENGTH_SHORT).show()
                    }
                }

                val websiteIcon = dialogView.findViewById<ImageView>(R.id.websiteIcon)
                websiteIcon.setOnClickListener {
                    if (url.isNotEmpty()) {
                        abrirEnlace(url)
                    } else {
                        Toast.makeText(this, "El enlace del sitio web está vacío", Toast.LENGTH_SHORT).show()
                    }
                }
                // Configurar el icono de categoría
                val categoriaIcono = dialogView.findViewById<ImageView>(R.id.categoryIcon) // Asegúrate de tener un ImageView para el icono de categoría en tu layout

                // Configurar botón de 'ir al evento'
                val rutaBoton = dialogView.findViewById<Button>(R.id.eventButton)
                rutaBoton.setOnClickListener { abrirEnlace("https://www.google.com/maps/dir/?api=1&origin=$origen&destination=$destino")}

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
                            val (lat, lng) = it.ubicacion?.split(",")!!
                                .map { coord -> coord.toDouble() }
                            val position = LatLng(lat, lng)

                            // Almacena la coordenada en formato "lat,lng" en la lista de coordenadas existentes
                            existingCoordinates.add("${lat},${lng}")

                            // Asigna el color según la categoría
                            val color = when (it.categoria) {
                                "Deportes" -> BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_BLUE
                                )

                                "Comida" -> BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_GREEN
                                )

                                "Entretenimiento" -> BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED
                                )

                                else -> BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_ORANGE
                                ) // Color por defecto
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
                            } else {
                                // ??
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error parsing location for ${it.titulo}: ${e.message}")
                        }
                    }
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
                val intent = Intent(this, FormsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_two -> {
                val intent = Intent(this, MyEventsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_three -> {
                // Cerrar sesión
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
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
