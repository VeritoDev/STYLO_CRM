package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
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
                        binding.rvHistorialCitas.visibility = View.GONE
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.tvSinCitas.text = getString(R.string.sin_citas_historial)
                    } else {
                        binding.rvHistorialCitas.visibility = View.VISIBLE
                        binding.tvSinCitas.visibility = View.GONE

                        val adapter = HistorialAdapter(lista)
                        binding.rvHistorialCitas.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvHistorialCitas.adapter = adapter
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
                    // Extraemos solo el valor después de los dos puntos ":"
                    putString("nombre", binding.tvNombreDetalle.text.toString().substringAfter(": ").trim())
                    putString("telefono", binding.tvTelefonoDetalle.text.toString().substringAfter(": ").trim())
                    putString("email", binding.tvEmailDetalle.text.toString().substringAfter(": ").trim())
                    putString("notas", binding.tvNotasDetalle.text.toString().substringAfter(": ").trim())
                }
                findNavController().navigate(R.id.action_detalleClienteFragment_to_formularioClientesFragment2, bundle)
            }
        }

        binding.btnEliminar.setOnClickListener {
            if (clienteId.isNotEmpty()) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(getString(R.string.eliminar_cliente))
                builder.setMessage(getString(R.string.eliminar_mensaje))

                builder.setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                    repository.eliminarCliente(clienteId)
                    Toast.makeText(requireContext(), getString(R.string.cliente_eliminado), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }

                builder.setNegativeButton(getString(R.string.cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
                // Ponemos el botón de eliminar en rojo para advertir al usuario
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            }
        }

        binding.btnAAdirCita.setOnClickListener {
            findNavController().navigate(R.id.action_detalleClienteFragment_to_crearCitasFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun rellenarInterfaz(cliente: Cliente) {
        // Inyectamos los datos en las plantillas del strings.xml
        binding.tvNombreDetalle.text = getString(R.string.label_nombre, cliente.nombre.uppercase())
        binding.tvTelefonoDetalle.text = getString(R.string.label_telefono, cliente.telefono)
        binding.tvEmailDetalle.text = getString(R.string.email, cliente.email.uppercase())
        binding.tvNotasDetalle.text = getString(R.string.notas, cliente.notas)
    }
}