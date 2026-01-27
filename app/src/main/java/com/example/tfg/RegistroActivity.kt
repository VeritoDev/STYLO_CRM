package com.example.tfg

import android.annotation.SuppressLint
import com.example.tfg.R
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Firebase

class RegistroActivity : AppCompatActivity() {

    // Inicializamos la base de datos
    private val database = Firebase.firestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val btnRegistrar = findViewById<Button>(R.id.btnFinalizarRegistro)
        val etNombre = findViewById<EditText>(R.id.etNombreRegistro)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etPass = findViewById<EditText>(R.id.etPasswordRegistro)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val correo = etCorreo.text.toString()
            val pass = etPass.text.toString()

            if (nombre.isNotEmpty() && correo.isNotEmpty() && pass.isNotEmpty()) {
                guardarEnFirebase(nombre, correo, pass)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEnFirebase(nombre: String, correo: String, pass: String) {
        // Creamos el mapa de datos
        val usuario = hashMapOf(
            "nombre" to nombre,
            "correo" to correo,
            "password" to pass // Nota: En una app real, la pass no se guarda así en Firestore
        )

        database.collection("usuarios")
            .add(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro completado", Toast.LENGTH_SHORT).show()
                finish() // Cierra esta actividad y vuelve al Login
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
            }
    }
}