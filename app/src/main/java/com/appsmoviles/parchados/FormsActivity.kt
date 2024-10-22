package com.appsmoviles.parchados

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FormsActivity : AppCompatActivity() {
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_form)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)

        val titleLayout = findViewById<TextInputLayout>(R.id.event_title)
        descriptionLayout = findViewById(R.id.event_description)
        val locationLayout = findViewById<TextInputLayout>(R.id.event_location)
        phoneLayout = findViewById(R.id.event_phone)
        val categoryLayout = findViewById<TextInputLayout>(R.id.event_category)
        val priceLayout = findViewById<TextInputLayout>(R.id.event_price)
        val hoursLayout = findViewById<TextInputLayout>(R.id.event_hours)
        val minutesLayout = findViewById<TextInputLayout>(R.id.event_minutes)

        //val titleText: TextInputEditText = titleLayout.editText as TextInputEditText
        val descriptionText: TextInputEditText = descriptionLayout.editText as TextInputEditText
        //val locationText: TextInputEditText = locationLayout.editText as TextInputEditText
        val phoneText: TextInputEditText = phoneLayout.editText as TextInputEditText
        //val categoryText: AutoCompleteTextView = categoryLayout.editText as AutoCompleteTextView
        //val priceText: TextInputEditText = priceLayout.editText as TextInputEditText
        val hoursText: TextInputEditText = hoursLayout.editText as TextInputEditText
        val minutesText: TextInputEditText = minutesLayout.editText as TextInputEditText

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
                    if (s.length > 250) {
                        descriptionLayout.error = "Excede caracteres"
                    } else {
                        descriptionLayout.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        hoursText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val hours = s.toString().toIntOrNull() ?: 0
                    if (hours < 0 || hours > 23) {
                        hoursText.setText("")
                        hoursText.error = "Ingrese horas entre 0 y 23"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        minutesText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val minutes = s.toString().toIntOrNull() ?: 0
                    if (minutes < 0 || minutes > 59) {
                        minutesText.setText("")
                        minutesText.error = "Ingrese minutos entre 0 y 59"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val isValid = validateFields(titleLayout, descriptionLayout, locationLayout, phoneLayout, categoryLayout, priceLayout, hoursLayout, minutesLayout)
            if (isValid) {
                Toast.makeText(this, "Formulario enviado correctamente", Toast.LENGTH_SHORT).show()
                // Aquí puedes agregar la lógica para manejar la validación de campos
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
}
