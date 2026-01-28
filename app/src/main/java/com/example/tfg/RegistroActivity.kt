package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish() //CIERRA LA PANTALLA Y VUELVE A LA ANTERIOR
        }

        binding.btnGuardarCliente.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etNuevoNombre.text.toString().trim()
        val correo = binding.etNuevoEmail.text.toString().trim()
        val pass = binding.etNuevaContrasena.text.toString().trim() // Asegúrate de que el ID sea este

        if (correo.isNotEmpty() && pass.isNotEmpty() && nombre.isNotEmpty()) {
            if (pass.length >= 6) {
                auth.createUserWithEmailAndPassword(correo, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Bienvenido/a $nombre", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Mínimo 6 caracteres / Minimum 6 characters", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Rellena todos los campos / Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
}