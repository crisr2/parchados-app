package com.appsmoviles.parchados

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val genderSpinner = findViewById<Spinner>(R.id.genderSpinnerEdit)
        val phoneInput = findViewById<EditText>(R.id.phoneEdit)
        val nameInput = findViewById<EditText>(R.id.nameEdit)
        val lastnameInput = findViewById<EditText>(R.id.lastnameEdit)
        val btnConfir = findViewById<Button>(R.id.btnConfir)

        ArrayAdapter.createFromResource(
            this,
            R.array.genderArray,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            genderSpinner.adapter = adapter
        }

        btnConfir.setOnClickListener {
            val selectGender = genderSpinner.selectedItem.toString()
            val phone = phoneInput.text.toString()
            val name = nameInput.text.toString()
            val lastname = lastnameInput.text.toString()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                if (phoneInput.text.isNotEmpty()) {
                    val userId = currentUser.uid
                    val userMap = hashMapOf(
                        "phone" to phone,
                        "gender" to selectGender,
                        "name" to name,
                        "lastname" to lastname
                    )

                    db.collection("Users").document(userId)
                        .update(userMap as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Datos editados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                } else {
                    Toast.makeText(this, "Por favor, ingrese todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}