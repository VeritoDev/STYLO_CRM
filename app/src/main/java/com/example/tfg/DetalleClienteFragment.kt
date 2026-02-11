package com.example.tfg

import android.annotation.SuppressLint
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
    private val repository = MainRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleClienteBinding.bind(view)

        val clienteId = arguments?.getString("clienteId") ?: ""

        if (clienteId.isNotEmpty()) {
            repository.obtenerDetalleCliente(clienteId) { cliente ->
                if (cliente != null) {
                    rellenarInterfaz(cliente)
                }
                repository.getHistorialCitasCliente(clienteId) { lista ->
                    if (lista.isEmpty()) {
                        binding.rvHistorialCitas?.visibility = View.GONE
                        binding.tvSinCitas?.visibility = View.VISIBLE
                    } else {
                        binding.rvHistorialCitas?.visibility = View.VISIBLE
                        binding.tvSinCitas?.visibility = View.GONE

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
            if (clienteId.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("clienteId", clienteId)
                    putString("nombre", binding.tvNombreDetalle.text.toString().replace("NOMBRE CLIENTE: ", ""))
                    putString("telefono", binding.tvTelefonoDetalle.text.toString().replace("TELÉFONO: ", ""))
                    putString("email", binding.tvEmailDetalle.text.toString().replace("EMAIL: ", ""))
                    putString("notas", binding.tvNotasDetalle.text.toString().replace("NOTAS: ", ""))
                }
                findNavController().navigate(R.id.action_detalleClienteFragment_to_formularioClientesFragment2, bundle)
            }
        }

        binding.btnEliminar.setOnClickListener {
            if (clienteId.isNotEmpty()) {
                val builder = android.app.AlertDialog.Builder(requireContext())
                builder.setTitle("¿Eliminar cliente?")
                builder.setMessage("Esta acción no se puede deshacer. ¿Estás seguro de que quieres borrar a este cliente?")

                builder.setPositiveButton("Eliminar") { _, _ ->
                    repository.eliminarCliente(clienteId)
                    android.widget.Toast.makeText(requireContext(), "Cliente eliminado", android.widget.Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }

                builder.setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.RED)
            }
        }
        binding.btnAAdirCita.setOnClickListener {

        }
    }

    @SuppressLint("SetTextI18n")
    private fun rellenarInterfaz(cliente: Cliente) {
        binding.tvNombreDetalle.text = "NOMBRE CLIENTE: ${cliente.nombre.uppercase()}"
        binding.tvTelefonoDetalle.text = "TELÉFONO: ${cliente.telefono}"
        binding.tvEmailDetalle?.text = "EMAIL: ${cliente.email.uppercase()}"
        binding.tvNotasDetalle.text = "NOTAS: ${cliente.notas}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}