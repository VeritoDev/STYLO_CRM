package com.example.tfg.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.databinding.ItemCitasBinding
import com.example.tfg.model.Cita

class CitasAdapter(
    private var listaCitas: List<Cita>,
    private val onCitaClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(val binding: ItemCitasBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CitaViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.binding.apply {
            tvNombreItem.text = cita.nombre
            tvServicioItem.text = cita.servicio
            tvEstadoItem.text = cita.hora
            tvFechaItem.text = cita.fecha

            root.setOnClickListener { onCitaClick(cita) }
        }
    }

    override fun getItemCount(): Int = listaCitas.size

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Cita>) {
        listaCitas = nuevaLista
        notifyDataSetChanged()
    }
}