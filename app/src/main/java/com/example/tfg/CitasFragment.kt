package com.example.tfg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.databinding.FragmentCitasBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.adapter.CitasAdapter

class CitasFragment : Fragment(R.layout.fragment_citas) {

    private lateinit var binding: FragmentCitasBinding
    private val mainRepository = MainRepository()
    private lateinit var citasAdapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCitasBinding.bind(view)

        setupRecyclerView()

        binding.fabAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_citasFragment_to_crearCitasFragment)
        }
    }

    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(emptyList()) { cita ->
        }
        binding.rvCitas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
        }
    }
}