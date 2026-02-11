package com.example.tfg.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.example.tfg.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.databinding.ItemClientesBinding
import com.example.tfg.model.Cliente

class ClientesAdapter(
    private var lista: List<Cliente>,
    private val onClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    private var listaFiltrada: MutableList<Cliente> = lista.toMutableList()

    class ClienteViewHolder(val binding: ItemClientesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClientesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaFiltrada[position]

        holder.binding.apply {
            tvNombreItem.text = cliente.nombre
            tvTelefonoDetalle.text = cliente.telefono

            root.setOnClickListener { onClick(cliente) }
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size


    @SuppressLint("NotifyDataSetChanged")
    fun filtrar(texto: String): Int{
        val bus = texto.lowercase().trim()

        listaFiltrada = if (bus.isEmpty()){
            lista.toMutableList()
        } else {
            lista.filter { cliente ->
                cliente.nombre.lowercase().contains(bus) || cliente.telefono.contains(bus)
            }.toMutableList()
        }
        notifyDataSetChanged()
        return listaFiltrada.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun actualizarLista(nuevaLista: List<Cliente>) {
        lista = nuevaLista
        listaFiltrada = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }
}