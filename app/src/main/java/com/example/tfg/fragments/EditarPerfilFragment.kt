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
        emailGuardado = prefs.getString("user_email", "") ?: ""

        // Cargar datos actuales en los campos
        repository.obtenerDatosEstilista(emailGuardado) { estilista ->
            if (estilista != null) {
                estilistaId = estilista.id
                binding.etNombreEditar.setText(estilista.nombre.capitalizarFormato())
                binding.etEmailEditar.setText(estilista.email)
            }
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = binding.etNombreEditar.text.toString().trim()
        val nuevoEmail = binding.etEmailEditar.text.toString().trim().lowercase()
        val passActual = binding.etPassActual.text.toString().trim()
        val passNueva = binding.etPassNueva.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty() || passActual.isEmpty()) {
            Toast.makeText(requireContext(), "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Evitar dobles clics
        binding.btnGuardarPerfil.isEnabled = false

        // Llamamos al repositorio que maneja la reautenticación
        repository.actualizarDatosEstilista(
            id = estilistaId,
            emailActual = emailGuardado,
            passActual = passActual,
            nuevoNombre = nuevoNombre,
            nuevoEmail = nuevoEmail,
            nuevaPass = passNueva.ifEmpty { null }
        ) { exito, mensaje ->

            binding.btnGuardarPerfil.isEnabled = true
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (exito) {
                // Actualizamos SharedPreferences para no romper el inicio por huella
                val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("user_email", nuevoEmail)
                    putString("user_name_key", nuevoNombre)
                    if (passNueva.isNotEmpty()) putString("user_pass", passNueva)
                    apply()
                }
                findNavController().popBackStack()
            }
        }
    }
}