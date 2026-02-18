package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistroBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            binding = ActivityRegistroBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //BOTÓN IR HACIA ARTÁS
            binding.btnBack.setOnClickListener {
                finish()
            }

            //BOTÓN PARA GUARDAR EL USUARIO
            binding.btnGuardarUsuario.setOnClickListener {
                registrarUsuario()
            }
    }

    private fun registrarUsuario() {
        val nombre = binding.etNuevoNombre.text.toString().trim()
        val correo = binding.etNuevoEmail.text.toString().trim()
        val pass = binding.etNuevaContrasena.text.toString().trim()

        if (correo.isEmpty() || pass.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos / Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 6) {
            Toast.makeText(this, "Mínimo 6 caracteres / Min 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        //CREAMOS EL USUARIO EN FIREBASE
        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                //SI SE HA COMPLETADO BIEN LOS CAMPOS, SE GUARDA EN LA BASE DE DATOS
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid

                    if(uid != null){
                        val datosEstilista = mapOf(
                            "nombre" to nombre,
                            "email" to correo,
                            "rol" to "estilista"
                        )

                        db.child("estilistas").child(uid).setValue(datosEstilista)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Bienvenido/a $nombre", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("NOMBRE_USUARIO", nombre)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "@string/errorPerfilGuardar", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    //SI NO, SALE UN TOAST CON QUE ERROR HA OCURRIDO
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}