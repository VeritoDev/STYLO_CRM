package com.example.tfg.adapter

import android.view.LayoutInflater
import com.example.tfg.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.model.Cliente

class ClientesAdapter(
    private var lista: List<Cliente>,
    private val onClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre = view.findViewById<TextView>(R.id.tvNombreItem)
        val servicio = view.findViewById<TextView>(R.id.tvServicioItem)
        val fecha = view.findViewById<TextView>(R.id.tvFechaItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clientes, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = lista[position]
        val contexto = holder.itemView.context

        //VARIABLES DE FIREBASE
        holder.nombre.text = contexto.getString(R.string.label_nombre, cliente.nombreCliente)
        holder.servicio.text = contexto.getString(R.string.label_servicio, cliente.ultimoServicio)
        holder.fecha.text = contexto.getString(R.string.label_fecha, cliente.ultimaCita)

        holder.itemView.setOnClickListener { onClick(cliente) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Cliente>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}