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
import com.example.tfg.model.Cita
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository
import com.example.tfg.repository.capitalizarFormato
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleClienteFragment : Fragment(R.layout.fragment_detalle_cliente) {

    private lateinit var binding: FragmentDetalleClienteBinding
    private val repository = MainRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetalleClienteBinding.bind(view)

        val clienteId = arguments?.getString("clienteId") ?: ""

        if (clienteId.isNotEmpty()) {
            repository.obtenerDetalleCliente(clienteId) { cliente ->
                if (!isAdded) return@obtenerDetalleCliente

                if (cliente != null) {
                    rellenarInterfaz(cliente)
                }

                repository.getHistorialCitasCliente(clienteId) { lista ->
                    if (!isAdded) return@getHistorialCitasCliente

                    if (lista.isEmpty()) {
                        binding.rvHistorialCitas.visibility = View.GONE
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.tvSinCitas.text = getString(R.string.sin_citas_historial)
                    } else {
                        binding.tvSinCitas.visibility = View.GONE
                        binding.rvHistorialCitas.visibility = View.VISIBLE

                        val listaOrdenada = ordenarCitas(lista)

                        val adapter = HistorialAdapter(listaOrdenada)
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
                val dialog = EliminarClienteDialogFragment(onConfirm = {
                    repository.eliminarClienteYSusCitas(clienteId) { exito ->
                        if(exito) {
                            Toast.makeText(requireContext(), getString(R.string.cliente_eliminado), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                })
                dialog.show(parentFragmentManager, "EliminarClienteDialog")
            }
        }

        binding.btnAAdirCita.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TELEFONO_CLIENTE", binding.tvTelefonoDetalle.text.toString().substringAfter(": ").trim())
            }
            findNavController().navigate(R.id.action_detalleClienteFragment_to_crearCitasFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun rellenarInterfaz(cliente: Cliente) {
        binding.tvNombreDetalle.text = getString(R.string.label_nombre_param, cliente.nombre.capitalizarFormato())
        binding.tvTelefonoDetalle.text = getString(R.string.label_telefono_param, cliente.telefono)
        binding.tvEmailDetalle.text = getString(R.string.email_param, cliente.email.uppercase())
        binding.tvNotasDetalle.text = getString(R.string.notas_param, cliente.notas)
    }

    private fun ordenarCitas(lista: List<Cita>): List<Cita> {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        return lista.sortedByDescending { cita ->
            try{
                formato.parse("${cita.fecha} ${cita.hora}")
            } catch (e: Exception) {
                Date(0)
            }
        }
    }
}