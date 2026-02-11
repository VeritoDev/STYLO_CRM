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
            val nombre = binding.etNuevoNombre.text.toString()
            val telefono = binding.etNuevoTelefono.text.toString()
            val email = binding.etNuevoEmail.text.toString()
            val notas = binding.etNuevoNotas.text.toString()

            if (nombre.isEmpty()) {
                binding.etNuevoNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            if (telefono.length != 9){
                binding.etNuevoTelefono.error = "El teléfono tiene que tener 9 dígitos"
                return@setOnClickListener
            }
            if (!email.contains("@") || !email.contains(".")){
                binding.etNuevoEmail.error = "El email tiene que ser válido"
                return@setOnClickListener
            }
             val nuevoCliente = Cliente (
                 nombre = nombre,
                 telefono = telefono,
                 email = email,
                 notas = notas
             )
            mainRepository.insertarCliente(nuevoCliente)
            Toast.makeText(requireContext(), "Cliente guardado correctamente", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}