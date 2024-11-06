package com.appsmoviles.parchados

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailInput)
        val passwordEditText = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginBtn)
        val textView = findViewById<TextView>(R.id.textRegistro)
        val textRestablecer = findViewById<TextView>(R.id.textOlvido)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val text = "¿Se te olvido tu contraseña? Restablecela ya"

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task: Task<AuthResult>->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            userId?.let { uid ->
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            val userData = hashMapOf(
                                                "email" to email
                                            )

                                            db.collection("users").document(uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(this, MainActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            textRestablecer.text = text
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        textView.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        textRestablecer.setOnClickListener {
            val intent = Intent(this, RestablecerActivity::class.java)
            startActivity(intent)
        }
    }
}



