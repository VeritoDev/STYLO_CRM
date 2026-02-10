package com.example.tfg.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.tfg.R
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
        //VARIABLES DE FIREBASE
        holder.binding.apply {
            val contexto = root.context

            tvNombreItem.text = "${contexto.getString(R.string.nombre_cliente)}: ${cita.nombre}"
            tvServicioItem.text = "${contexto.getString(R.string.servicio)}: ${cita.servicio}"
            tvFechaItem.text = "${contexto.getString(R.string.fechaDef)}: ${cita.fecha}"
            tvEstadoItem.text = "${contexto.getString(R.string.horaDef)}: ${cita.hora}"
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