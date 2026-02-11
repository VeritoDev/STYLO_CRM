package com.example.tfg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.tfg.R
import com.google.firebase.auth.FirebaseAuth

class RecuperarPassFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recuperar_pass, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmailRecuperar)
        val btn = view.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val auth = FirebaseAuth.getInstance()

        auth.currentUser?.email?.let { etEmail.setText(it) }

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty() || !email.contains("@")) {
                etEmail.error = "Introduce un email válido"
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Email de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }
}