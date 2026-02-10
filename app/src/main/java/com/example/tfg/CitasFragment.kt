package com.example.tfg

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.databinding.FragmentCitasBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.adapter.CitasAdapter
import com.google.firebase.firestore.core.ComponentProvider

class CitasFragment : Fragment(R.layout.fragment_citas) {

    private lateinit var binding: FragmentCitasBinding
    private val mainRepository = MainRepository()
    private lateinit var citasAdapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCitasBinding.bind(view)

        setupRecyclerView()
        cargarCitas()

        binding.fabAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_citasFragment_to_crearCitasFragment)
        }
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation

        //LÓGICA PARA QUE SALGAN EN DOS COLUMNAS LAS CITAS EN HORIZONTAL SOLO
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.rvCitas.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())
        }

        citasAdapter = CitasAdapter(emptyList()) { cita ->
            val bundle = Bundle().apply {
                putString("clienteId", cita.idCliente)
            }
            findNavController().navigate((R.id.action_citasFragment_to_detalleClienteFragment), bundle)
        }
        binding.rvCitas.adapter = citasAdapter
    }

    private fun cargarCitas(){
        mainRepository.getTodasLasCitas { listaCitas ->
            if(listaCitas.isEmpty()) {
                binding.tvSinCitas?.visibility = View.VISIBLE
                binding.rvCitas.visibility = View.GONE
            } else {
                binding.tvSinCitas?.visibility = View.GONE
                binding.rvCitas.visibility = View.VISIBLE
                citasAdapter.actualizarLista(listaCitas)
            }
        }
    }
}