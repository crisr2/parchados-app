package com.appsmoviles.parchados

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class UserActivity2 : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userAge: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val genderSpinner = findViewById<Spinner>(R.id.genderSpinner)
        val phoneInput = findViewById<EditText>(R.id.phone)
        val birthday = findViewById<Button>(R.id.birthday)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnFinish = findViewById<Button>(R.id.btnFinish)

        ArrayAdapter.createFromResource(
            this,
            R.array.genderArray,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        birthday.setOnClickListener {
            val calendario = Calendar.getInstance()
            val año = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, añoSeleccionado, mesSeleccionado, diaSeleccionado ->
                    userAge = age(diaSeleccionado, mesSeleccionado + 1, añoSeleccionado)
                },
                año,
                mes,
                dia
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        btnFinish.setOnClickListener {
            val selectGender = genderSpinner.selectedItem.toString()
            val phone = phoneInput.text.toString()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                if (phoneInput.text.isNotEmpty()) {
                    val userId = currentUser.uid
                    val userMap = hashMapOf(
                        "phone" to phone,
                        "gender" to selectGender,
                        "age" to userAge
                    )

                    db.collection("Users").document(userId)
                        .update(userMap as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Datos guardados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                } else {
                    Toast.makeText(this, "Por favor, ingresa tu numero", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

private fun age(dia: Int, mes: Int, año: Int): Int {
    val fechaActual = Calendar.getInstance()
    val añoActual = fechaActual.get(Calendar.YEAR)
    val mesActual = fechaActual.get(Calendar.MONTH) + 1
    val diaActual = fechaActual.get(Calendar.DAY_OF_MONTH)

    var edad = añoActual - año

    if (mes > mesActual || (mes == mesActual && dia > diaActual)) {
        edad--
    }
    return edad
}