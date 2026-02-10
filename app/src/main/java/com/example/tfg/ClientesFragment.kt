package com.example.tfg

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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

        binding.fabAddCliente?.setOnClickListener {
            findNavController().navigate(R.id.action_clientesFragment_to_formularioClientesFragment)
        }
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvClientes?.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.rvClientes?.layoutManager = LinearLayoutManager(requireContext())
        }

        adapter = ClientesAdapter(emptyList()) { cliente ->
            val bundle = Bundle().apply {
                putString("clienteId", cliente.id)
            }
            findNavController().navigate(R.id.action_clientesFragment_to_detalleClienteFragment, bundle)

        }

        binding.rvClientes?.adapter = adapter
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