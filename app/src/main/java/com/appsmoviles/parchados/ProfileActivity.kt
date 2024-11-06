package com.appsmoviles.parchados

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name = findViewById<TextView>(R.id.nameProfile)
        val email = findViewById<TextView>(R.id.emailProfile)
        val age = findViewById<TextView>(R.id.ageProfile)
        val gender = findViewById<TextView>(R.id.genderProfile)
        val phone = findViewById<TextView>(R.id.phoneProfile)
        val currentUser = auth.currentUser
        val btnEdit = findViewById<ImageButton>(R.id.btn_edit_profile)
        val btnClose = findViewById<ImageButton>(R.id.btn_close)

        if (currentUser != null) {
            val userId = currentUser.uid

            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nameDb = document.getString("name")
                        val lastnameDb = document.getString("lastname")
                        val ageDb = document.getLong("age")
                        val phoneDb = document.getString("phone")
                        val genderDb = document.getString("gender")

                        name.text = "${nameDb} ${lastnameDb}"
                        age.text = ageDb.toString()
                        gender.text = genderDb
                        phone.text  = phoneDb
                        email.text = currentUser.email
                    }
                }
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}