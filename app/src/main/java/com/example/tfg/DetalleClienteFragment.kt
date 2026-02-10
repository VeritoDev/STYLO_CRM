package com.example.tfg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.adapter.HistorialAdapter
import com.example.tfg.databinding.FragmentDetalleClienteBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository

class DetalleClienteFragment : Fragment(R.layout.fragment_detalle_cliente) {

    private lateinit var binding: FragmentDetalleClienteBinding
    private val mainRepository = MainRepository()
    private val repository = MainRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleClienteBinding.bind(view)

        val clienteId = arguments?.getString("clienteId") ?: ""

        if(clienteId.isNotEmpty()){
            mainRepository.obtenerDetalleCliente(clienteId){ cliente ->
                if (cliente != null){
                    rellenarInterfaz(cliente)
                }
                repository.getHistorialCitasCliente(clienteId) { lista ->
                    if (lista.isNotEmpty()) {
                        val adapter = HistorialAdapter(lista)
                        binding.rvHistorialCitas?.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.rvHistorialCitas?.adapter = adapter
                    }
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditar.setOnClickListener {
            //LÓGICA PARA EDITAR EL CLIENTE
        }

        binding.btnEliminar.setOnClickListener {
            if (clienteId.isNotEmpty()) {
                repository.eliminarCliente(clienteId)
                findNavController().navigateUp()
            }
        }
    }

    private fun rellenarInterfaz(cliente: Cliente) {
        binding.tvNombreDetalle.text = "NOMBRE CLIENTE: ${cliente.nombre.uppercase()}"
        binding.tvTelefonoDetalle.text = "TELÉFONO: ${cliente.telefono}"
        binding.tvEmailDetalle?.text = "EMAIL: ${cliente.email.uppercase()}"
        binding.tvNotasDetalle.text = "NOTAS: ${cliente.notas}"
    }
}