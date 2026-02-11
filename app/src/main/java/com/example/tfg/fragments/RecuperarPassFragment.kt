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

//UN DIALOG ES UNA VENTANA FLOTANTE ENCIMA DEL FRAGMENT O ACTIVITY
class RecuperarPassFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recuperar_pass, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmailRecuperar)
        val btn = view.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val auth = FirebaseAuth.getInstance()

        //SI ESTÁ DENTRO DE LA SESIÓN, SE PONE DIRECTAMENTE EL EMAIL QUE TIENE
        auth.currentUser?.email?.let { etEmail.setText(it) }

        btn.setOnClickListener {
            val email = etEmail.text.toString().trim()

            //COMPRUEBA SI EL EMAIL ES VÁLIDO
            if (email.isEmpty() || !email.contains("@")) {
                etEmail.error = "Introduce un email válido"
                return@setOnClickListener
            }

            //SE ENVIA UN EMAIL CON UN ENLACE PARA RESTAURAR LA CONTRASEÑA
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Email de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                    dismiss()   //CIERRA EL DIALOGFRAGMENT, PERO NO EL FRAGMENT O ACTIVITY QUE HAY DETRÁS
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }
}