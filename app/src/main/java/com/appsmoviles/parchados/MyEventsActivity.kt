package com.appsmoviles.parchados

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.appsmoviles.parchados.models.Eventos
import com.google.firebase.database.*

class MyEventsActivity : AppCompatActivity() {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var eventsList: MutableList<Eventos>

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events) // Asegúrate de tener este layout

        eventsRecyclerView = findViewById(R.id.recyclerViewEvents) // Asegúrate de que el RecyclerView esté en tu layout
        eventsList = mutableListOf()

        // Al configurar el adaptador en MyEventsActivity, se pasa un callback refreshEventList para actualizar la lista después de eliminar
        eventsAdapter = EventsAdapter(eventsList, { selectedEvent ->
            val intent = Intent(this, FormsActivity::class.java).apply {
                putExtra("eventId", selectedEvent.eventId ?: "")
                putExtra("eventTitle", selectedEvent.titulo ?: "")
                putExtra("eventDescription", selectedEvent.descripcion ?: "")
                putExtra("eventLocation", selectedEvent.ubicacion ?: "")
                // Agrega los demás campos que necesites
            }
            startActivity(intent)
        }, ::refreshEventList) // Pasamos el método de actualización

        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsRecyclerView.adapter = eventsAdapter

        fetchMyEvents()  // Función para obtener eventos del usuario

        // Obtener el ImageButton
        val imageButtonBack = findViewById<ImageButton>(R.id.imageButtonBack)
        imageButtonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Iniciar la MainActivity
            finish()
        }

    }

    private fun fetchMyEvents() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid  // Obtener el ID del usuario autenticado

            // Consulta en la base de datos para obtener eventos del usuario
            firebaseDatabase.child("eventos").orderByChild("userId").equalTo(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newEventList = mutableListOf<Eventos>()
                        for (eventSnapshot in snapshot.children) {
                            val event = eventSnapshot.getValue(Eventos::class.java)
                            if (event != null) {
                                newEventList.add(event)
                            }
                        }

                        // Actualizar la lista solo si es necesario
                        if (newEventList != eventsList) {
                            eventsList.clear()
                            eventsList.addAll(newEventList)
                            eventsAdapter.notifyDataSetChanged()  // Notificar que la lista ha cambiado
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MyEventsActivity, "Error al cargar los eventos", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "No estás autenticado", Toast.LENGTH_SHORT).show()
        }
    }



    // Método para actualizar la lista de eventos después de la eliminación
    private fun refreshEventList() {
        fetchMyEvents()  // Recargamos los eventos desde Firebase
    }
}
