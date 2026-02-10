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
        val telefono = view.findViewById<TextView>(R.id.tvTelefonoDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clientes, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = lista[position]
        val contexto = holder.itemView.context

        //VARIABLES DE FIREBASE
        holder.nombre.text = contexto.getString(R.string.label_nombre, cliente.nombre)
        holder.telefono.text = contexto.getString(R.string.label_telefono, cliente.telefono)
        holder.itemView.setOnClickListener { onClick(cliente) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Cliente>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}