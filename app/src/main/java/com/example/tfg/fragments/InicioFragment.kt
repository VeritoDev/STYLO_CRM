package com.example.tfg.fragments

import android.content.Context
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
        val prefs = requireContext().getSharedPreferences("config_app", android.content.Context.MODE_PRIVATE)

        val nombreReal = prefs.getString("user_name_key", null)

        if (nombreReal != null) {
            binding.tvNombreUsuario.text = nombreReal.uppercase()
        } else {
            val usuario = mainRepository.getUsuarioActual()
            binding.tvNombreUsuario.text = usuario?.email?.uppercase()?.substringBefore("@") ?: "Profesional"
        }

        // NAVEGACIÓN PARA NUEVO CLIENTE
        binding.cardAgregarCliente.setOnClickListener {
            findNavController().navigate(R.id.action_inicioFragment_to_formularioClientesFragment)
        }

        // NAVEGACIÓN A NUEVA CITA
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
        val prefs = requireContext().getSharedPreferences("config_app", android.content.Context.MODE_PRIVATE)
        // Recuperamos el nombre y le quitamos espacios invisibles
        val nombreEstilista = prefs.getString("user_name_key", "")?.trim() ?: ""

        // ESTE LOG TE DIRÁ EN EL LOGCAT QUÉ ESTÁ BUSCANDO REALMENTE
        android.util.Log.d("PRUEBA_TFG", "Buscando en Firebase citas donde estilista sea igual a: '$nombreEstilista'")

        if (nombreEstilista.isNotEmpty()) {
            mainRepository.getCitasPorEstilista(nombreEstilista) { listaCitas ->
                android.util.Log.d("PRUEBA_TFG", "Citas encontradas: ${listaCitas.size}")

                if (listaCitas.isEmpty()) {
                    binding.tvSinCitas.visibility = View.VISIBLE
                    binding.rvDashboard.visibility = View.GONE
                } else {
                    binding.tvSinCitas.visibility = View.GONE
                    binding.rvDashboard.visibility = View.VISIBLE
                    citasAdapter.actualizarLista(listaCitas)
                }
            }
        } else {
            binding.tvSinCitas.visibility = View.VISIBLE
        }
    }
}