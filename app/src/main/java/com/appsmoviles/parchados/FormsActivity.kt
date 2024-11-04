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
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import java.util.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat

class FormsActivity : AppCompatActivity() {
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout

    private lateinit var entryTimeText: TextInputEditText
    private lateinit var exitTimeText: TextInputEditText

    private lateinit var imageView: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var removeImageButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_form)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)

        descriptionLayout = findViewById(R.id.event_description)
        phoneLayout = findViewById(R.id.event_phone)

        val titleLayout = findViewById<TextInputLayout>(R.id.event_title)
        val locationLayout = findViewById<TextInputLayout>(R.id.event_location)
        val categoryLayout = findViewById<TextInputLayout>(R.id.event_category)
        val priceLayout = findViewById<TextInputLayout>(R.id.event_price)
        val entryHoursLayout = findViewById<TextInputLayout>(R.id.event_hours_entry)
        val exitHoursLayout = findViewById<TextInputLayout>(R.id.event_hours_exit)

        //val titleText: TextInputEditText = titleLayout.editText as TextInputEditText
        val descriptionText: TextInputEditText = descriptionLayout.editText as TextInputEditText
        //val locationText: TextInputEditText = locationLayout.editText as TextInputEditText
        val phoneText: TextInputEditText = phoneLayout.editText as TextInputEditText
        //val categoryText: AutoCompleteTextView = categoryLayout.editText as AutoCompleteTextView
        //val priceText: TextInputEditText = priceLayout.editText as TextInputEditText

        entryTimeText = entryHoursLayout.editText as TextInputEditText
        exitTimeText = exitHoursLayout.editText as TextInputEditText


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

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val isValid = validateFields(titleLayout, descriptionLayout, locationLayout, phoneLayout, categoryLayout, priceLayout, exitHoursLayout, entryHoursLayout)

            if (isValid && validateLinks()) {
                Toast.makeText(this, "Formulario enviado correctamente", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar la lógica para manejar el envío del formulario
            } else {
                Toast.makeText(this, "Por favor, corrige los errores antes de enviar.", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }

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
        return allValid
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

    fun validateLinks(): Boolean {
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
                Log.e("Validation", "El enlace de Instagram no es válido.")
                allLinksValid = false
            } else {
                Log.d("Links", "Instagram: $instagramLink")
                openLinkInBrowser(instagramLink)
            }
        }

        // Validación de TikTok
        if (tkUsername.isNotEmpty()) {
            val tiktokLink = "https://tiktok.com/@${tkUsername}"
            if (!isValidUrl(tiktokLink)) {
                Log.e("Validation", "El enlace de TikTok no es válido.")
                allLinksValid = false
            } else {
                Log.d("Links", "TikTok: $tiktokLink")
                openLinkInBrowser(tiktokLink)
            }
        }

        // Validación del enlace completo
        if (fullLink.isNotEmpty() && !isValidUrl(fullLink)) {
            Log.e("Validation", "El enlace completo no es válido.")
            allLinksValid = false
        } else if (fullLink.isNotEmpty()) {
            Log.d("Validation", "El enlace completo es válido.")
            openLinkInBrowser(fullLink)
        }
        return allLinksValid
    }

    private fun isValidUrl(url: String): Boolean {
        val regex = Regex("^(http|https)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}(/\\S*)?$")
        return regex.matches(url)
    }

    // Función para abrir el enlace en el navegador
    private fun openLinkInBrowser(link: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(browserIntent)
    }

}
