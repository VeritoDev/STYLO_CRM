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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recuperar_pass, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmailRecuperar)
        val btn = view.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val auth = FirebaseAuth.getInstance()

        val emailDesdeLogin = arguments?.getString("EMAIL_PREVIO")

        if (!emailDesdeLogin.isNullOrEmpty()) {
            etEmail.setText(emailDesdeLogin)
        } else {
            auth.currentUser?.email?.let { etEmail.setText(it) }
        }

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty() || !email.contains("@")) {
                etEmail.error = context?.getString(R.string.email_valido)
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.toast_recuperacion_enviado, email),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "ERROR: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }
}