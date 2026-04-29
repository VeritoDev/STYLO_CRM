package com.example.tfg.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.R
import com.example.tfg.databinding.FragmentPerfilBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.repository.capitalizarFormato

class EditarPerfilFragment : Fragment(R.layout.fragment_perfil) {

    private lateinit var binding: FragmentPerfilBinding
    private val repository = MainRepository()
    private var estilistaId: String = ""
    private var emailGuardado: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPerfilBinding.bind(view)

        cargarDatos()

        binding.btnGuardarPerfil.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarDatos() {
        val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
        // Recuperamos siempre el email actualizado de las preferencias
        emailGuardado = prefs.getString("user_email", "") ?: ""

        if (emailGuardado.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            return
        }

        repository.obtenerDatosEstilista(emailGuardado) { estilista ->
            if (estilista != null) {
                estilistaId = estilista.id
                // Limpiamos errores previos y seteamos textos nuevos
                binding.etNombreEditar.setText(estilista.nombre.capitalizarFormato())
                binding.etEmailEditar.setText(estilista.email)
            }
        }
    }

    private fun guardarCambios() {
        val inputNombre = binding.etNombreEditar.text.toString().trim()
        val inputEmail = binding.etEmailEditar.text.toString().trim().lowercase()
        val passActual = binding.etPassActual.text.toString().trim()
        val passNueva = binding.etPassNueva.text.toString().trim()

        if (passActual.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.escribir_contraseña), Toast.LENGTH_SHORT).show()
            return
        }

        // Si el usuario deja el campo de email vacío, mantenemos el que ya tenía
        val emailAFijar = if (inputEmail.isEmpty()) emailGuardado else inputEmail

        // Si deja el nombre vacío, mantenemos el nombre que cargamos al principio
        val nombreAFijar = if (inputNombre.isEmpty()) {
            val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
            prefs.getString("user_name_key", "") ?: ""
        } else {
            inputNombre
        }

        binding.btnGuardarPerfil.isEnabled = false

        repository.actualizarDatosEstilista(
            id = estilistaId,
            emailActual = emailGuardado, // Email con el que entraste
            passActual = passActual,
            nuevoNombre = nombreAFijar,
            nuevoEmail = emailAFijar,    // Email que quieres ahora
            nuevaPass = passNueva.ifEmpty { null }
        ) { exito, mensaje ->
            binding.btnGuardarPerfil.isEnabled = true
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (exito) {
                // ¡MUY IMPORTANTE!: Actualizar las preferencias para la próxima sesión
                val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("user_email", emailAFijar)
                    putString("user_name_key", nombreAFijar)
                    if (passNueva.isNotEmpty()) putString("user_pass", passNueva)
                    apply()
                }

                // Actualizamos la variable local para que el fragmento sepa el nuevo correo
                emailGuardado = emailAFijar

                findNavController().popBackStack()
            }
        }
    }
}