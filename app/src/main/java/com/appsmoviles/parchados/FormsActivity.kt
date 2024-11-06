package com.appsmoviles.parchados

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import android.net.Uri
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.app.TimePickerDialog
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import java.util.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.appsmoviles.parchados.models.Eventos
import com.google.firebase.FirebaseApp
import java.text.SimpleDateFormat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FormsActivity : AppCompatActivity() {
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var locationLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var igLayout: TextInputLayout
    private lateinit var tkLayout: TextInputLayout
    private lateinit var linkLayout: TextInputLayout

    private lateinit var titleText: TextInputEditText
    private lateinit var descriptionText: TextInputEditText
    private lateinit var locationtText: TextInputEditText
    private lateinit var locationText: TextInputEditText
    private lateinit var localityText: AutoCompleteTextView
    private lateinit var phoneText: TextInputEditText
    private lateinit var categoryText: AutoCompleteTextView
    private lateinit var priceText: TextInputEditText
    private lateinit var entryTimeText: TextInputEditText
    private lateinit var exitTimeText: TextInputEditText
    private lateinit var igText: TextInputEditText
    private lateinit var tkText: TextInputEditText
    private lateinit var linkText: TextInputEditText

    private lateinit var imageView: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var removeImageButton: ImageView

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("eventos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_form)

        FirebaseApp.initializeApp(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)

        descriptionLayout = findViewById(R.id.event_description)
        locationLayout = findViewById(R.id.event_location)
        phoneLayout = findViewById(R.id.event_phone)
        igLayout = findViewById(R.id.event_ig)
        tkLayout = findViewById(R.id.event_tk)
        linkLayout = findViewById(R.id.event_link)

        val titleLayout = findViewById<TextInputLayout>(R.id.event_title)
        val localityLayout = findViewById<TextInputLayout>(R.id.event_locality)
        val locationtLayout = findViewById<TextInputLayout>(R.id.event_locationt)
        val categoryLayout = findViewById<TextInputLayout>(R.id.event_category)
        val priceLayout = findViewById<TextInputLayout>(R.id.event_price)
        val entryHoursLayout = findViewById<TextInputLayout>(R.id.event_hours_entry)
        val exitHoursLayout = findViewById<TextInputLayout>(R.id.event_hours_exit)

        titleText = titleLayout.editText as TextInputEditText
        descriptionText = descriptionLayout.editText as TextInputEditText
        locationtText = locationtLayout.editText as TextInputEditText
        locationText = locationLayout.editText as TextInputEditText
        localityText = localityLayout.editText as AutoCompleteTextView
        phoneText = phoneLayout.editText as TextInputEditText
        categoryText = categoryLayout.editText as AutoCompleteTextView
        priceText = priceLayout.editText as TextInputEditText
        entryTimeText = entryHoursLayout.editText as TextInputEditText
        exitTimeText = exitHoursLayout.editText as TextInputEditText
        igText = igLayout.editText as TextInputEditText
        tkText = tkLayout.editText as TextInputEditText
        linkText = linkLayout.editText as TextInputEditText


        // Imagen
        imageView = findViewById(R.id.selected_image) // Asegúrate de que tengas un ImageView con este ID
        removeImageButton = findViewById(R.id.remove_image_button)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageView.setImageURI(it) // Mostrar la imagen seleccionada
                imageView.visibility = View.VISIBLE
                removeImageButton.visibility = View.VISIBLE
            }
        }

        findViewById<Button>(R.id.event_image_button).setOnClickListener {
            openImageChooser()
        }

        removeImageButton.setOnClickListener {
            imageView.setImageDrawable(null) // Elimina la imagen
            imageView.visibility = View.GONE // Oculta la ImageView
            removeImageButton.visibility = View.GONE // Oculta el botón "X"
        }

        // Eliminar errores cuando se vuelve a escribir
        fun setupClearErrorOnType(textField: EditText, textLayout: TextInputLayout) {
            textField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Limpiar el error cuando el usuario comienza a escribir
                    if (textField == igText || textField == tkText || textField == linkText) {
                        textLayout.error = null
                    }
                    else if (!s.isNullOrEmpty()) {
                        textLayout.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        setupClearErrorOnType(titleText, titleLayout)
        setupClearErrorOnType(locationtText, locationtLayout)
        setupClearErrorOnType(localityText, localityLayout)
        setupClearErrorOnType(categoryText, categoryLayout)
        setupClearErrorOnType(priceText, priceLayout)
        setupClearErrorOnType(entryTimeText, entryHoursLayout)
        setupClearErrorOnType(exitTimeText, exitHoursLayout)
        setupClearErrorOnType(igText, igLayout)
        setupClearErrorOnType(tkText, tkLayout)
        setupClearErrorOnType(linkText, linkLayout)

        // Ubicación
        locationText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.matches(Regex("^[-+]?\\d*\\.?\\d+,[-+]?\\d*\\.?\\d+$"))) {
                        locationLayout.error = null
                    } else {
                        locationLayout.error = "Formato inválido. Usa el formato: latitud,longitud"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Descripción
        descriptionText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length > 250) {
                        descriptionLayout.error = "Excede caracteres"
                    } else {
                        descriptionLayout.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Teléfono
        phoneText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (s.length > 10) {
                        phoneLayout.error = "Excede caracteres"
                    } else {
                        phoneLayout.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Configura el TimePicker para la entrada
        entryTimeText.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val formattedTime = formatTime(hour, minute)
                entryTimeText.setText(formattedTime)
            }
        }

        // Configura el TimePicker para la salida
        exitTimeText.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                val formattedTime = formatTime(hour, minute)
                exitTimeText.setText(formattedTime)
            }
        }

        // Validación de los campos
        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val isValid = validateFields(titleLayout, descriptionLayout, locationtLayout, locationLayout, localityLayout, phoneLayout, categoryLayout, priceLayout, exitHoursLayout, entryHoursLayout)
            val allLinksValid = validateLinks()

            if (isValid && allLinksValid) {

                // Recoger datos del formulario
                val title = titleText.text.toString()
                val description = descriptionText.text.toString()
                val locationt = locationtText.text.toString()
                val location = locationText.text.toString()
                val locality = localityText.text.toString()
                val phone = phoneText.text.toString()
                val category = categoryText.text.toString()
                val price = priceText.text.toString()
                val entryTime = entryTimeText.text.toString()
                val exitTime = exitTimeText.text.toString()
                val instagram = igText.text.toString()
                val tiktok = tkText.text.toString()
                val link = linkText.text.toString()

                val eventId = database.push().key!!
                val eventos = Eventos(eventId, title, description, locationt, location, locality, phone, category, price, entryTime, exitTime, instagram, tiktok, link)

                // Crear un nuevo evento
                database.child(eventId).setValue(eventos)
                    .addOnCompleteListener{
                        Toast.makeText(this, "Formulario enviado correctamente", Toast.LENGTH_SHORT).show()
                        clearForm()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "error ${it.message}", Toast.LENGTH_SHORT).show()
                    }

            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Horario
    private fun showTimePickerDialog(onTimeSet: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, { _, selectedHour, selectedMinute ->
                onTimeSet(selectedHour, selectedMinute)
            },
            hour, minute, false // false para el formato de 12 horas
        )
        timePickerDialog.show()
    }

    // Función para formatear la hora en formato 12 horas
    private fun formatTime(hour: Int, minute: Int): String {
        val amPmFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return amPmFormat.format(calendar.time)
    }
    // Imagen
    private fun openImageChooser() {
        imagePickerLauncher.launch("image/*")
    }

    // Validaciones
    private fun validateFields(vararg fields: TextInputLayout): Boolean {
        var allValid = true
        for (field in fields) {
            if (field.editText?.text.isNullOrEmpty()) {
                field.error = "Campo obligatorio"
                allValid = false
            } else {
                field.error = null
            }
        }

        // Verifica si el campo de descripción excede 250 caracteres
        val descriptionText: TextInputEditText = descriptionLayout.editText as TextInputEditText
        val descriptionLength = descriptionText.text?.length ?: 0
        if (descriptionLength > 250) {
            allValid = false
        } else if (descriptionLength > 0) {
            descriptionLayout.error = null
        }

        // Verifica si el campo de teléfono tiene más de 10 caracteres
        val phoneText: TextInputEditText = phoneLayout.editText as TextInputEditText
        val phoneLength = phoneText.text?.length ?: 0
        if (phoneLength > 10) {
            allValid = false
        } else if (phoneLength < 7) {
            phoneLayout.error = "El número debe tener al menos 7 dígitos"
            allValid = false
        } else {
            phoneLayout.error = null
        }

        // Verifica si el campo de coordenadas es válido
        val locationText: TextInputEditText = locationLayout.editText as TextInputEditText
        val location = locationText.text.toString()
        if (!location.matches(Regex("^[-+]?\\d*\\.?\\d+,[-+]?\\d*\\.?\\d+$"))) {
            locationLayout.error = "Formato inválido. Usa el formato: latitud,longitud"
            allValid = false
        } else {
            locationLayout.error = null
        }
        return allValid
    }

    private fun validateLinks(): Boolean {
        val igLinkEditText: TextInputEditText = findViewById(R.id.ig_link)
        val tkLinkEditText: TextInputEditText = findViewById(R.id.tk_link)
        val fullLinkEditText: TextInputEditText = findViewById(R.id.link)

        val igUsername = igLinkEditText.text.toString().trim()
        val tkUsername = tkLinkEditText.text.toString().trim()
        val fullLink = fullLinkEditText.text.toString().trim()

        var allLinksValid = true

        // Validación de Instagram
        if (igUsername.isNotEmpty()) {
            val instagramLink = "https://instagram.com/${igUsername}"
            if (!isValidUrl(instagramLink)) {
                igLayout.error = "Usuario no válido"
                allLinksValid = false
            }
        }

        // Validación de TikTok
        if (tkUsername.isNotEmpty()) {
            val tiktokLink = "https://tiktok.com/@${tkUsername}"
            if (!isValidUrl(tiktokLink)) {
                tkLayout.error = "Usuario no válido"
                allLinksValid = false
            }
        }

        // Validación del enlace completo
        if (fullLink.isNotEmpty() && !isValidUrl(fullLink)) {
            linkLayout.error = "Link no válido"
            allLinksValid = false
        }
        return allLinksValid
    }

    private fun isValidUrl(url: String): Boolean {
        val regex = Regex("^(http|https)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}(/\\S*)?$")
        return regex.matches(url)
    }

    // Método para limpiar el formulario
    private fun clearForm() {
        titleText.text?.clear()
        descriptionText.text?.clear()
        locationtText.text?.clear()
        locationText.text?.clear()
        localityText.text?.clear()
        phoneText.text?.clear()
        categoryText.text?.clear()
        priceText.text?.clear()
        entryTimeText.text?.clear()
        exitTimeText.text?.clear()
        igText.text?.clear()
        tkText.text?.clear()
        linkText.text?.clear()
        imageView.setImageDrawable(null) // Opcional: limpia la imagen seleccionada
        imageView.visibility = View.GONE
        removeImageButton.visibility = View.GONE
    }

}
