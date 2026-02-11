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

    private var listaFiltrada: MutableList<Cita> = listaCitas.toMutableList()

    class CitaViewHolder(val binding: ItemCitasBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val binding = ItemCitasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CitaViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaFiltrada[position]

        holder.binding.apply {
            val contexto = root.context
            tvNombreItem.text = "${contexto.getString(R.string.nombre_cliente)}: ${cita.nombre}"
            tvServicioItem.text = "${contexto.getString(R.string.servicio)}: ${cita.servicio}"
            tvFechaItem.text = "${contexto.getString(R.string.horaDef)}: ${cita.hora}"

            root.setOnClickListener { onCitaClick(cita) }
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    @SuppressLint("NotifyDataSetChanged")
    fun filtrar(texto: String): Int {
        val bus = texto.lowercase().trim()

        //SI EL BUSCADOR ESTÁ VACÍO, ENSEÑA TODA LA LISTA
        listaFiltrada = if (bus.isEmpty()) {
            listaCitas.toMutableList()
        } else {
        //SI NO, FILTRA LA LISTA CON LO QUE SE HA ESCRITO EN EL BUSCADOR
            listaCitas.filter {
                it.nombre.lowercase().contains(bus) || it.servicio.lowercase().contains(bus)
            }.toMutableList()
        }
        notifyDataSetChanged()
        return listaFiltrada.size
    }

    //FUNCIÓN PARA RECIBIR LOS DATOS DE FIREBASE
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Cita>) {
        listaCitas = nuevaLista
        listaFiltrada = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }
}