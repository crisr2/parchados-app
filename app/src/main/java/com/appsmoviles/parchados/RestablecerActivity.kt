package com.appsmoviles.parchados

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class RestablecerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restablecer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val resetButton = findViewById<Button>(R.id.btnRestablecer)
        val emailField = findViewById<EditText>(R.id.EmailRestablecer)

        resetButton.setOnClickListener {
            val email = emailField.text.toString()
            if (email.isNotEmpty()) {
                resetPassword(this, email)
            } else {
                Toast.makeText(this, "Por favor, ingresa un correo electrónico", Toast.LENGTH_SHORT).show()
            }
        }

    }
}

private fun resetPassword(context: Context, email: String) {
    val auth = FirebaseAuth.getInstance()
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context,"Correo de restablecimiento enviado a $email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context,"Error al enviar correo de restablecimiento", Toast.LENGTH_SHORT).show()
            }
        }
}