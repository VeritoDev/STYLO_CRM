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

        binding.btnFinalizarRegistro.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val correo = binding.etCorreoRegistro.text.toString().trim()
        val pass = binding.etPasswordRegistro.text.toString().trim()
        val nombre = binding.etNombreRegistro.text.toString().trim()

        // VALIDACIÓN PASO A PASO
        if (correo.isNotEmpty() && pass.isNotEmpty() && nombre.isNotEmpty()) {
            if (pass.length >= 6) {
                // CREAR CUENTA EN FIREBASE
                auth.createUserWithEmailAndPassword(correo, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                //DEBERÍA SER EN INGLÉS Y ESPAÑOL
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        } else {
            //AQUI IGUAL
            Toast.makeText(this, "Rellena todos los campos, por favor", Toast.LENGTH_SHORT).show()
        }
    }
}