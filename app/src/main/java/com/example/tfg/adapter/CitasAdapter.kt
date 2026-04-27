package com.example.tfg.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.tfg.R
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.databinding.ItemCitasBinding
import com.example.tfg.model.Cita
import com.example.tfg.repository.capitalizarFormato

class CitasAdapter(
    private var listaCitas: List<Cita>,
    private val onCitaClick: (Cita) -> Unit,
    private val esEstilista: Boolean
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

            if (esEstilista) {
                val etiqueta = contexto.getString(R.string.label_cliente)
                val nombreCap = cita.nombreCliente.capitalizarFormato()
                val telefono = cita.telefonoCliente

                holder.binding.tvNombreItem.text = "$etiqueta: $nombreCap - $telefono"
            } else {
                val etiqueta = contexto.getString(R.string.label_estilista)
                holder.binding.tvNombreItem.text = "$etiqueta: ${cita.estilista.capitalizarFormato()}"
            }

            val etiquetaPersonal = contexto.getString(R.string.label_estilista)
            tvEstilistaItem?.text = "$etiquetaPersonal: ${cita.estilista.capitalizarFormato()}"

            val servicioTraducido = when (cita.servicio) {
                contexto.getString(R.string.servicio_corte), "Corte", "Haircut" -> contexto.getString(R.string.servicio_corte)
                contexto.getString(R.string.servicio_barba), "Barba", "Beard" -> contexto.getString(R.string.servicio_barba)
                contexto.getString(R.string.servicio_peinado), "Peinado", "Styling" -> contexto.getString(R.string.servicio_peinado)
                contexto.getString(R.string.servicio_tinte), "Tinte", "Dye" -> contexto.getString(R.string.servicio_tinte)
                else -> cita.servicio
            }

            tvServicioItem.text = "${contexto.getString(R.string.servicio)}: $servicioTraducido"

            tvFechaItem.text = "${cita.fecha} - ${cita.hora}"

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
                it.nombreCliente.lowercase().contains(bus) || it.servicio.lowercase().contains(bus) || it.telefonoCliente.lowercase().contains(bus)
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