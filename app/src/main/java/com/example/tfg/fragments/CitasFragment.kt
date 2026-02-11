package com.example.tfg.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.R
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
        cargarCitas()

        binding.fabAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_citasFragment_to_crearCitasFragment)
        }

        binding.etBuscarCita.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (::citasAdapter.isInitialized) {
                    val resultados = citasAdapter.filtrar(query)

                    if (resultados == 0 && query.isNotEmpty()) {
                        binding.tvSinCitas.text = getString(R.string.sin_resultados_busqueda_citas, query)
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.rvCitas.visibility = View.GONE
                    } else if (resultados == 0 && query.isEmpty()) {
                        binding.tvSinCitas.text = getString(R.string.noClientes)
                        binding.tvSinCitas.visibility = View.VISIBLE
                        binding.rvCitas.visibility = View.GONE
                    } else {
                        // Hay resultados, ocultamos el aviso
                        binding.tvSinCitas.visibility = View.GONE
                        binding.rvCitas.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setupRecyclerView() {
        val orientation = resources.configuration.orientation

        //LÓGICA PARA QUE SALGAN EN DOS COLUMNAS LAS CITAS EN HORIZONTAL SOLO
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.rvCitas.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())
        }

        citasAdapter = CitasAdapter(
            listaCitas = emptyList(),
            onCitaClick = { cita ->
                val bundle = Bundle().apply {
                    putString("clienteId", cita.idCliente)
                }
                findNavController().navigate(R.id.action_citasFragment_to_detalleClienteFragment, bundle)
            }
        )
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