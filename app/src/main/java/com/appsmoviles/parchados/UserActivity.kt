package com.appsmoviles.parchados

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val lastnameInput = findViewById<EditText>(R.id.lastnameInput)
        val btn = findViewById<Button>(R.id.btnNext)

        btn.setOnClickListener {
            val name = nameInput.text.toString()
            val lastname = lastnameInput.text.toString()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                if (nameInput.text.isNotEmpty() and lastnameInput.text.isNotEmpty()) {
                    val userId = currentUser.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "lastname" to lastname
                    )

                    db.collection("Users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, UserActivity2::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Por favor, ingresa tu nombre y apellido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}