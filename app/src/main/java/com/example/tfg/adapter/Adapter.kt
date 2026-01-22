package com.example.tfg.adapter

import android.view.LayoutInflater
import com.example.tfg.R
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.model.Cita
import com.example.tfg.model.Item

class Adapter(private val items: List<Item>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloTarjeta)
        val tvContenido = view.findViewById<TextView>(R.id.tvContenidoTarjeta)
        val containerCitas = view.findViewById<android.widget.LinearLayout>(R.id.containerCitas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitulo.text = item.titulo

        //LÓGICA PARA LA TARJETA DE CITAS
        if(item.isCita) {
            holder.tvContenido.visibility = View.GONE
            holder.containerCitas.visibility = View.VISIBLE

            //LIMPIAMOS EL CONTENEDOR DE CITAS PARA EVITAR DUPLICADOS AL HACER SCROLL
            holder.containerCitas.removeAllViews()

            //DATOS DE PRUEBA (EN UN FUTURO SERÁ DONDE SE CONECTE LA BASE DE DATOS)
            val listaCitas = listOf(
                Cita("10:00", "Ana García", "Corte + Tinte"),
                Cita("11:30", "Marta López", "Peinado"),
                Cita("14:15", "Carlos Rodríguez", "Corte"),
            )

            //INFLAMOS CADA CITA DENTRO DEL CONTENEDOR
            val inflater = LayoutInflater.from(holder.itemView.context)
            for (cita in listaCitas) {
                val viewCita = inflater.inflate(R.layout.item_citas_dashboard, holder.containerCitas, false)

                viewCita.findViewById<TextView>(R.id.tvHoraCita).text = cita.hora
                viewCita.findViewById<TextView>(R.id.tvNombreCliente).text = cita.nombre
                // Añadimos el guion visualmente si lo deseas
                viewCita.findViewById<TextView>(R.id.tvServicioCita).text = "- ${cita.servicio}"

                holder.containerCitas.addView(viewCita)
            }
        } else {
            //LÓGICA PARA TARJETAS NORMALES
            holder.containerCitas.visibility = View.GONE
            holder.tvContenido.visibility = View.VISIBLE
            holder.tvContenido.text = item.contenido
        }
    }

    override fun getItemCount(): Int = items.size
}