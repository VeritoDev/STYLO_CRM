package com.example.tfg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.adapter.Adapter
import com.example.tfg.model.Item

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDashboard)

        // 1. Configuramos cómo se verán las tarjetas (una debajo de otra)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 2. Definimos qué tarjetas queremos ver
        val listaTarjetas = listOf(
            Item("PRÓXIMAS CITAS", isCita = true),
            Item("RESUMEN DIARIO", "Has atendido a 12 clientes hoy"),
            Item("CAJA", "Total acumulado: 450.00€")
        )

        // 3. Le pasamos los datos al adaptador
        recyclerView.adapter = Adapter(listaTarjetas)
    }
}