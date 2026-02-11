package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.R
import com.example.tfg.databinding.FragmentFormularioClientesBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository

class FormularioClientesFragment : Fragment(R.layout.fragment_formulario_clientes) {

    private lateinit var binding: FragmentFormularioClientesBinding
    private val mainRepository = MainRepository()
    private var clienteID: String? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFormularioClientesBinding.bind(view)

        clienteID = arguments?.getString("clienteId")

        if (clienteID != null) {
            binding.tvTituloFormulario.text = getString(R.string.editarCliente)
            rellenarDatosParaEditar()
        } else {
            binding.tvTituloFormulario.text = getString(R.string.editarCliente)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnGuardarCliente.setOnClickListener {
            guardarDatos()
        }
    }
    private fun rellenarDatosParaEditar(){
        binding.etNuevoNombre.setText(arguments?.getString("nombre"))
        binding.etNuevoTelefono.setText(arguments?.getString("telefono"))
        binding.etNuevoEmail.setText(arguments?.getString("email"))
        binding.etNuevoNotas.setText(arguments?.getString("notas"))

    }
    private fun guardarDatos() {
        val nombre = binding.etNuevoNombre.text.toString().trim()
        val telefono = binding.etNuevoTelefono.text.toString().trim()
        val email = binding.etNuevoEmail.text.toString().trim()
        val notas = binding.etNuevoNotas.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.etNuevoNombre.error = "El nombre es obligatorio"
            return
        }
        if (telefono.length != 9) {
            binding.etNuevoTelefono.error = "El teléfono tiene que tener 9 dígitos"
            return
        }
        if (email.isNotEmpty() && (!email.contains("@") || !email.contains("."))) {
            binding.etNuevoEmail.error = "El email tiene que ser válido"
            return
        }

        if (clienteID != null) {
            val datosActualizados = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "email" to email,
                "notas" to notas
            )

            mainRepository.actualizarCliente(clienteID!!, datosActualizados) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val nuevoCliente = Cliente(
                nombre = nombre,
                telefono = telefono,
                email = email,
                notas = notas
            )
            mainRepository.insertarCliente(nuevoCliente)
            Toast.makeText(requireContext(), "Cliente guardado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}