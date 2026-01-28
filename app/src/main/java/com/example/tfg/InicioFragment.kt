package com.example.tfg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.databinding.FragmentInicioBinding
import com.example.tfg.repository.MainRepository

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var binding: FragmentInicioBinding
    private val mainRepository = MainRepository()
    //private lateinit var citasAdapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInicioBinding.bind(view)

        setupUI()
        setupRecyclerView()
        cargarDatos()
    }

    private fun setupUI() {
        val usuario = mainRepository.getUsuarioActual()
        binding.tvNombreUsuario.text = usuario?.email ?: "Usuario"

        binding.cardAgregarCliente.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_formularioClientesFragment)
        }

        binding.cardAgregarCita.setOnClickListener {
            //findNavController().navigate(R.id.action_inicioFragment_to_citasFragment)
        }
    }

    private fun setupRecyclerView() {
        //citasAdapter = CitasAdapter(emptyList())
        //binding.rvDashboard.adapter = citasAdapter
    }

    private fun cargarDatos() {
        mainRepository.getCitasHoy { listaCitas ->
            if (listaCitas.isEmpty()) {
                binding.tvSinCitas.visibility = View.VISIBLE
                binding.rvDashboard.visibility = View.GONE
            } else {
                binding.tvSinCitas.visibility = View.GONE
                binding.rvDashboard.visibility = View.VISIBLE
            }
            //citasAdapter.actualizarLista(listaCitas)
        }
    }
}