package com.example.tfg.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
import com.example.tfg.databinding.FragmentCitasBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.adapter.CitasAdapter
import com.example.tfg.model.Cita
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        //FILTRO DE BÚSQUEDA DE CITAS Y DEPENDIENDO DEL RESULTADO SE ACTUALIZA
        binding.tilBuscarCita?.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val resultados = citasAdapter.filtrar(query)

                if (resultados == 0 && query.isNotEmpty()) {
                    binding.tvSinCitas.text = getString(R.string.sin_resultados_busqueda_citas, query)
                    binding.tvSinCitas.visibility = View.VISIBLE
                    binding.rvCitas.visibility = View.GONE
                } else if (resultados == 0 && query.isEmpty()) {
                    binding.tvSinCitas.text = getString(R.string.noCitas)
                    binding.tvSinCitas.visibility = View.VISIBLE
                    binding.rvCitas.visibility = View.GONE
                } else {
                    binding.tvSinCitas.visibility = View.GONE
                    binding.rvCitas.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.rvCitas.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())
        }

        citasAdapter = CitasAdapter(
            listaCitas = emptyList(),
            esEstilista = true,
            onCitaClick = { cita ->
                abrirDialogoGestion(cita)
            }
        )
        binding.rvCitas.adapter = citasAdapter
    }

    private fun cargarCitas() {
        mainRepository.getTodasLasCitas { listaTotal ->
            if (!isAdded) return@getTodasLasCitas
            val listaPendientes = listaTotal.filter { it.estado != "finalizado" }

            // FILTRAMOS Y BORRAMOS LAS CITAS PASADAS AL MISMO TIEMPO
            val listaValida = limpiarCitasPasadasYFiltrar(listaPendientes)

            if (listaValida.isEmpty()) {
                binding.tvSinCitas.text = getString(R.string.noCitas)
                binding.tvSinCitas.visibility = View.VISIBLE
                binding.rvCitas.visibility = View.GONE
                citasAdapter.actualizarLista(emptyList())
            } else {
                binding.tvSinCitas.visibility = View.GONE
                binding.rvCitas.visibility = View.VISIBLE
                citasAdapter.actualizarLista(listaValida)
            }
        }
    }

    private fun abrirDialogoGestion(cita: Cita) {
        val dialogo = GestionarReservaDialog(
            cita = cita,
            onEditar = { citaAEditar ->
                val bundle = Bundle().apply {
                    putString("CITA_ID", citaAEditar.id)
                }
                findNavController().navigate(R.id.action_citasFragment_to_crearCitasFragment, bundle)
            },
            onEliminar = { citaAEliminar ->
                confirmarCancelacion(citaAEliminar)
            }
        )
        dialogo.show(parentFragmentManager, "GestionarReserva")
    }

    private fun confirmarCancelacion(cita: Cita) {
        val dialog = CancelarCitaDialog {
            mainRepository.eliminarCita(cita.id) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), getString(R.string.cita_cancelada), Toast.LENGTH_SHORT).show()
                    cargarCitas()
                }
            }
        }
        dialog.show(parentFragmentManager, "CancelarCita")
    }

    // DEVUELVE UNA LISTA SOLO CON LAS CITAS DE HOY Y FUTURAS (Y BORRA LAS VIEJAS DE FIREBASE)
    private fun limpiarCitasPasadasYFiltrar(lista: List<Cita>): List<Cita> {
        val formatoCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val ahora = Calendar.getInstance()

        // Le damos un margen de 10 minutos: la cita no desaparece hasta 10 min después de su hora
        ahora.add(Calendar.MINUTE, -10)

        val listaFiltrada = lista.filter { cita ->
            try {
                val fechaHoraCita = formatoCompleto.parse("${cita.fecha} ${cita.hora}")
                // Solo incluimos la cita si su hora es DESPUÉS de "ahora" (hace 10 min)
                fechaHoraCita?.after(ahora.time) ?: true
            } catch (e: Exception) {
                true
            }
        }

        return listaFiltrada.sortedBy {
            try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
        }
    }
}