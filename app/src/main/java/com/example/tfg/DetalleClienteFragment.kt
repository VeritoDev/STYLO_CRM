package com.example.tfg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.databinding.FragmentDetalleClienteBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository

class DetalleClienteFragment : Fragment(R.layout.fragment_detalle_cliente) {

    private lateinit var binding: FragmentDetalleClienteBinding
    private val repository = MainRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleClienteBinding.bind(view)

        val clienteId = arguments?.getString("clienteId") ?: ""

        if (clienteId.isNotEmpty()) {
            repository.getDetalleCliente(clienteId) { cliente ->
                if (cliente != null) {
                    rellenarInterfaz(cliente)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEliminar.setOnClickListener {
            if (clienteId.isNotEmpty()) {
                repository.eliminarCliente(clienteId)
                findNavController().navigateUp()
            }
        }
    }

    private fun rellenarInterfaz(cliente: Cliente) {
        binding.tvNombreDetalle.text = "NOMBRE CLIENTE: ${cliente.nombreCliente.uppercase()}"
        binding.tvTelefonoDetalle.text = "TELÉFONO: ${cliente.telefono}"
        binding.tvEmailDetalle.text = "EMAIL: ${cliente.email.uppercase()}"
        binding.tvNotasDetalle.text = "NOTAS: ${cliente.notas}"
    }
}