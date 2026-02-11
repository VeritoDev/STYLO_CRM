package com.example.tfg.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
import com.example.tfg.adapter.CitasAdapter
import com.example.tfg.databinding.FragmentInicioBinding
import com.example.tfg.repository.MainRepository

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var binding: FragmentInicioBinding
    private val mainRepository = MainRepository()
    private lateinit var citasAdapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInicioBinding.bind(view)

        setupUI()
        setupRecyclerView()
        cargarDatos()
    }

    private fun setupUI() {
        val usuario = mainRepository.getUsuarioActual()
        //MOSTRAMOS EL NOMBRE ANTES DEL @ SI EL EMAIL ES NULO
        binding.tvNombreUsuario.text = usuario?.email?.uppercase()?.substringBefore("@") ?: "Profesional"

        //NAVEGACIÓN PARA NUEVO CLIENTE
        binding.cardAgregarCliente.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_formularioClientesFragment)
        }

        //NAVEGACIÓN A NUEVA CITA
        binding.cardAgregarCita.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_crearCitasFragment)
        }
    }

    //LÓGICA PARA QUE SALGAN TODAS LAS CITAS QUE TIENE EL USUARIO EN EL RV DEL INICIO
    private fun setupRecyclerView() {
        citasAdapter = CitasAdapter(
            listaCitas = emptyList(),
            onCitaClick = { cita ->
                val bundle = Bundle().apply {
                    putString("clienteId", cita.idCliente)
                }
                findNavController().navigate(R.id.action_inicioFragment_to_formularioClientesFragment, bundle)
            }
        )

        binding.rvDashboard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = citasAdapter
            setHasFixedSize(true)
        }
    }

    //SI HAY DATOS LOS ENSEÑA, SI NO MUESTRA UN MENSAJE
    private fun cargarDatos() {
        mainRepository.getCitasHoy { listaCitas ->
            if (listaCitas.isEmpty()) {
                binding.tvSinCitas.visibility = View.VISIBLE
                binding.rvDashboard.visibility = View.GONE
            } else {
                binding.tvSinCitas.visibility = View.GONE
                binding.rvDashboard.visibility = View.VISIBLE
                citasAdapter.actualizarLista(listaCitas)
            }

        }
    }
}