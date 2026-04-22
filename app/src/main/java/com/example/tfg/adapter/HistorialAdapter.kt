package com.example.tfg.adapter

import com.example.tfg.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.model.Cita
import com.example.tfg.repository.capitalizarFormato

class HistorialAdapter(private val listaCitas: List<Cita>) :
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha = view.findViewById<TextView>(R.id.tvFechaHistorial)
        val tvServicio = view.findViewById<TextView>(R.id.tvServicioHistorial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial_simple, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.tvFecha.text = cita.fecha
        holder.tvServicio.text = cita.servicio.capitalizarFormato()
    }

    override fun getItemCount() = listaCitas.size
}