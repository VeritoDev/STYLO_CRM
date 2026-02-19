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
            esEstilista = false,
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
        val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)
        var nombre = prefs.getString("user_name_key", "")?.trim() ?: ""

        android.util.Log.d("DIAGNOSTICO", "Paso 1 - SharedPreferences dice: '$nombre'")

        if (nombre.isNotEmpty()) {
            ejecutarConsultaFirebase(nombre)
        } else {
            val uid = mainRepository.getUsuarioActual()?.uid
            android.util.Log.d("DIAGNOSTICO", "Paso 2 - Buscando en BD para el UID: $uid")

            if (uid != null) {
                mainRepository.getNombreEstilistaPorUID(uid) { nombreBD ->
                    if (!nombreBD.isNullOrEmpty()) {
                        android.util.Log.d("DIAGNOSTICO", "Paso 3 - Nombre recuperado de Firebase: $nombreBD")
                        // Lo guardamos para que la próxima vez el Paso 1 funcione
                        prefs.edit().putString("user_name_key", nombreBD).apply()
                        ejecutarConsultaFirebase(nombreBD)
                    } else {
                        android.util.Log.e("DIAGNOSTICO", "Paso FINAL - No existe el campo 'nombre' en /estilistas/$uid")
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.tvSinCitas.text = "Error: No se encontró tu nombre en el perfil"
                    }
                }
            }
        }
    }

    // Función auxiliar para no repetir código
    private fun ejecutarConsultaFirebase(nombre: String) {
        mainRepository.getCitasPorEstilista(nombre) { listaCitas ->
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