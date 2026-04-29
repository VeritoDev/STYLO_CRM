package com.example.tfg.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
import com.example.tfg.adapter.CitasAdapter
import com.example.tfg.databinding.FragmentInicioBinding
import com.example.tfg.repository.MainRepository
import androidx.core.content.edit
import com.example.tfg.model.Cita
import com.example.tfg.repository.capitalizarFormato
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        val prefs = requireContext().getSharedPreferences("config_app", Context.MODE_PRIVATE)

        val nombreReal = prefs.getString("user_name_key", null)

        if (nombreReal != null) {
            val nombreMayus = nombreReal.capitalizarFormato()
            binding.tvNombreUsuario.text = nombreMayus
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
            esEstilista = true,
            onCitaClick = { cita ->
                abrirDialogoGestion(cita)
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
        val nombre = prefs.getString("user_name_key", "")?.trim() ?: ""

        if (nombre.isNotEmpty()) {
            ejecutarConsultaFirebase(nombre)
        } else {
            val uid = mainRepository.getUsuarioActual()?.uid
            if (uid != null) {
                mainRepository.getNombreEstilistaPorUID(uid) { nombreBD ->
                    if (!nombreBD.isNullOrEmpty()) {
                        prefs.edit { putString("user_name_key", nombreBD) }
                        ejecutarConsultaFirebase(nombreBD)
                    } else {
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.tvSinCitas.text = context?.getString(R.string.error_nombre_perfil)
                    }
                }
            }
        }
    }

    private fun ejecutarConsultaFirebase(nombre: String) {
        mainRepository.getCitasPorEstilista(nombre) { listaCitas ->

            if (!isAdded) return@getCitasPorEstilista

            val listaPendientes = listaCitas.filter {
                val estado = it.estado.trim().lowercase()
                estado != "finalizado" && estado != "cancelado"
            }

            val listaValida = limpiarCitasPasadasYFiltrar(listaPendientes)

            if (listaValida.isEmpty()) {
                binding.tvSinCitas.text = getString(R.string.citas_vacio)
                binding.tvSinCitas.visibility = View.VISIBLE
                binding.rvDashboard.visibility = View.GONE
                citasAdapter.actualizarLista(emptyList())
            } else {
                binding.tvSinCitas.visibility = View.GONE
                binding.rvDashboard.visibility = View.VISIBLE
                citasAdapter.actualizarLista(listaValida)
            }
        }
    }

    private fun abrirDialogoGestion(cita: Cita) {
        val dialogo = GestionCitaFragment(
            cita = cita,
            onFinalizada = {
                mainRepository.finalizarCita(cita.id) { exito ->
                    if (exito) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.citaFinalizada),
                            Toast.LENGTH_SHORT
                        ).show()
                        cargarDatos()
                    }
                }
            },
            onEditar = {
                val bundle = Bundle().apply {
                    putString("CITA_ID", cita.id)
                }
                findNavController().navigate(
                    R.id.action_inicioFragment_to_crearCitasFragment,
                    bundle
                )
            }
        )
        dialogo.show(parentFragmentManager, "GestionCita")
    }

    // DEVUELVE UNA LISTA SOLO CON LAS CITAS DE HOY Y FUTURAS (Y ORDENADA)
    private fun limpiarCitasPasadasYFiltrar(lista: List<Cita>): List<Cita> {
        val formatoCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val ahora = Calendar.getInstance()

        // Le damos un margen de 30 minutos: la cita no desaparece hasta 30 min después de su hora
        ahora.add(Calendar.MINUTE, -30)

        val listaFiltrada = lista.filter { cita ->
            try {
                val fechaHoraCita = formatoCompleto.parse("${cita.fecha} ${cita.hora}")
                // Solo incluimos la cita si su hora es DESPUÉS de "ahora" (hace 30 min)
                fechaHoraCita?.after(ahora.time) ?: false
            } catch (e: Exception) {
                false
            }
        }

        return listaFiltrada.sortedBy {
            try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
        }
    }
}