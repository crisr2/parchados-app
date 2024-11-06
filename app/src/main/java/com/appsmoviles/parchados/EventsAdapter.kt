package com.appsmoviles.parchados

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.appsmoviles.parchados.models.Eventos
import com.google.firebase.database.FirebaseDatabase

class EventsAdapter(
    private val eventsList: MutableList<Eventos>,
    private val onItemClick: (Eventos) -> Unit,
    private val refreshEventList: () -> Unit // Método para actualizar la lista de eventos
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.titleTextView.text = event.titulo ?: ""
        holder.descriptionTextView.text = event.descripcion ?: ""
        holder.locationTextView.text = event.direccion ?: ""

        // Llama a la función onItemClick cuando se hace clic en el elemento
        holder.editIcon.setOnClickListener {
            onItemClick(event)
        }

        // Configurar icono de borrado con diálogo de confirmación
        holder.deleteIcon.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar evento")
                .setMessage("¿Estás seguro de que deseas eliminar este evento?")
                .setPositiveButton("Sí") { dialog, _ ->
                    event.eventId?.let { eventId ->
                        // Pasar el contexto y el callback para actualizar la lista
                        deleteEvent(holder.itemView.context, eventId, position)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    private fun deleteEvent(context: Context, eventId: String, position: Int) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("eventos").child(eventId)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Eliminar el evento de la lista local y notificar al adapter
                if (position < eventsList.size) {
                    eventsList.removeAt(position)
                    notifyItemRemoved(position)

                    // Mostrar un Toast con el resultado
                    Toast.makeText(
                        context,
                        "Evento eliminado",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Actualizar la lista de eventos en la actividad
                    refreshEventList()
                }
            } else {
                // En caso de error, mostrar un mensaje de error
                Toast.makeText(
                    context,
                    "Error al eliminar el evento",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewEventTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewEventDescription)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewEventLocation)
        val deleteIcon: ImageView = itemView.findViewById(R.id.iconDelete)
        val editIcon: ImageView = itemView.findViewById(R.id.iconEdit)
    }
}
