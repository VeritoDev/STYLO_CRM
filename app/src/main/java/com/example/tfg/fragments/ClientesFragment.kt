package com.example.tfg.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
import com.example.tfg.databinding.FragmentClientesBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.adapter.ClientesAdapter

class ClientesFragment : Fragment(R.layout.fragment_clientes) {

    private lateinit var binding: FragmentClientesBinding
    private lateinit var adapter: ClientesAdapter
    private val mainRepository = MainRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientesBinding.bind(view)

        setupRecyclerView()
        cargarDatosDeFirebase()

        //BOTÓN AÑADIR CLIENTE
        binding.fabAddCliente.setOnClickListener {
            findNavController().navigate(R.id.action_clientesFragment_to_formularioClientesFragment)
        }

        //SEARCHBAR DE BUSQUEDA DE USUARIOS
        binding.tilBuscarCliente?.editText?.addTextChangedListener { editable ->
            val query = editable.toString().trim()

            if (::adapter.isInitialized) {
                // El adapter filtrará por Nombre, Teléfono y Servicio/Email internamente
                val resultados = adapter.filtrar(query)

                if (resultados == 0) {
                    binding.rvClientes.visibility = View.GONE
                    binding.tvSinClientes.visibility = View.VISIBLE

                    // Si no hay resultados pero hay texto escrito -> Mensaje de "no se encontró X"
                    if (query.isNotEmpty()) {
                        binding.tvSinClientes.text = getString(R.string.sin_resultados_busqueda, query)
                    } else {
                        // Si la lista está vacía de base -> Mensaje de "no hay clientes"
                        binding.tvSinClientes.text = getString(R.string.noClientes)
                    }
                } else {
                    // Si hay resultados, mostramos la lista
                    binding.tvSinClientes.visibility = View.GONE
                    binding.rvClientes.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation

        //LÓGICA PARA QUE SALGAN EN DOS COLUMNAS LOS CLIENTES EN HORIZONTAL SOLO
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvClientes.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.rvClientes.layoutManager = LinearLayoutManager(requireContext())
        }

        adapter = ClientesAdapter(emptyList()) { cliente ->
            val bundle = Bundle().apply {
                putString("clienteId", cliente.id)
            }
            findNavController().navigate(R.id.action_clientesFragment_to_detalleClienteFragment, bundle)

        }

        binding.rvClientes.adapter = adapter
    }

    private fun cargarDatosDeFirebase() {
        mainRepository.getClientes { listaClientes ->
            if (listaClientes.isEmpty()) {
                binding.tvSinClientes.visibility = View.VISIBLE
                binding.rvClientes.visibility = View.GONE
            } else {
                binding.tvSinClientes.visibility = View.GONE
                binding.rvClientes.visibility = View.VISIBLE
                adapter.actualizarLista(listaClientes)
            }
        }
    }
}