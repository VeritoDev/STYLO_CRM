package com.example.tfg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RecuperarPassFragment : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recuperar_pass, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmailRecuperar)
        val btn = view.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

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