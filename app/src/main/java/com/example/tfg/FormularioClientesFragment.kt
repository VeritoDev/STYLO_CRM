package com.example.tfg

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.databinding.FragmentFormularioClientesBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository

class FormularioClientesFragment : Fragment(R.layout.fragment_formulario_clientes) {

    private lateinit var binding: FragmentFormularioClientesBinding
    private val mainRepository = MainRepository() // Usamos tu clase de confianza

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFormularioClientesBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnGuardarCliente.setOnClickListener {
            guardarNuevoCliente()
        }
    }

    private fun guardarNuevoCliente() {
        val nombre = binding.etNuevoNombre.text.toString()
        val telefono = binding.etNuevoTelefono.text.toString()
        val email = binding.etNuevoEmail?.text.toString()
        val notas = binding.etNuevoNotas.text.toString()

        if (nombre.isNotEmpty()) {
            val nuevoCliente = Cliente(nombre = nombre, telefono = telefono, email = email, notas = notas)

            android.util.Log.d("FIREBASE_TEST", "Intentando guardar cliente...")
            mainRepository.insertarCliente(nuevoCliente)
            android.util.Log.d("FIREBASE_TEST", "Función ejecutada")

            findNavController().navigateUp()
        } else {
            Toast.makeText(
                requireContext(),
                "@string/toast_campos_vacios",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}