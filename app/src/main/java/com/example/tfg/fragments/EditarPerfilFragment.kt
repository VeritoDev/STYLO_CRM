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

        //BOTON GUARDAR
        binding.btnGuardarPerfil.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarDatos() {
        val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
        emailGuardado = prefs.getString("user_email", "") ?: ""

        if (emailGuardado.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
            return
        }

        repository.obtenerDatosEstilista(emailGuardado) { estilista ->
            if (estilista != null) {
                estilistaId = estilista.id
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

        //SI EL USUARIO DEJA EL CAMPO DEL EMAIL VACIO, MANTENEMOS EL QUE YA TENIA
        val emailAFijar = if (inputEmail.isEmpty()) emailGuardado else inputEmail

        //SI DEJA EL NOMBRE VACIO, MANTENEMOS EL NOMBRE QUE TENIAMOS AL PRINCIPIO
        val nombreAFijar = inputNombre.ifEmpty {
            val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
            prefs.getString("user_name_key", "") ?: ""
        }

        binding.btnGuardarPerfil.isEnabled = false

        repository.actualizarDatosEstilista(
            id = estilistaId,
            emailActual = emailGuardado,
            passActual = passActual,
            nuevoNombre = nombreAFijar,
            nuevoEmail = emailAFijar,
            nuevaPass = passNueva.ifEmpty { null }
        ) { exito, mensaje ->
            binding.btnGuardarPerfil.isEnabled = true
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

            if (exito) {
                //ACTUALIZAMOS LAS PREFERENCIAS PARA LA PRÓXIMA SESIÓN
                val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("user_email", emailAFijar)
                    putString("user_name_key", nombreAFijar)
                    if (passNueva.isNotEmpty()) putString("user_pass", passNueva)
                    apply()
                }

                //ACTUALIZAMOS LA VARIABLE LOCAL PARA QUE EL FRAGMENT SEPA EL NUEVO CORREO
                emailGuardado = emailAFijar

                findNavController().popBackStack()
            }
        }
    }
}