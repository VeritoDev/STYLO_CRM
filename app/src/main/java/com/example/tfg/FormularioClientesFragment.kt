package com.example.tfg

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tfg.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.databinding.FragmentFormularioClientesBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.Repository

class FormularioClientesFragment : Fragment(R.layout.fragment_formulario_clientes) {

    private lateinit var binding: FragmentFormularioClientesBinding
    private val repository = Repository() // Usamos tu clase de confianza

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFormularioClientesBinding.bind(view)

        binding.btnGuardarCliente.setOnClickListener {
            guardarNuevoCliente()
        }
    }

    private fun guardarNuevoCliente() {
        val nombre = binding.etNuevoNombre.text.toString()
        val telefono = binding.etNuevoTelefono.text.toString()
        val email = binding.etNuevoEmail.text.toString()
        val notas = binding.etNuevoNotas.text.toString()

        if (nombre.isNotEmpty() && telefono.isNotEmpty()) {
            val nuevoCliente = Cliente(
                nombreCliente = nombre,
                telefono = telefono,
                email = email,
                notas = notas,
                ultimoServicio = "Nuevo Cliente",
                ultimaCita = "Pendiente"
            )

           repository.insertarCliente(nuevoCliente)

            Toast.makeText(requireContext(), "Cliente guardado con éxito", Toast.LENGTH_SHORT)
                .show()

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