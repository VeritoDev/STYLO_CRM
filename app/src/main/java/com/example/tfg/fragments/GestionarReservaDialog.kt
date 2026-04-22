package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tfg.R
import com.example.tfg.model.Cita

class GestionarReservaDialog(
    private val cita: Cita,
    private val onEditar: (Cita) -> Unit,
    private val onEliminar: (Cita) -> Unit
) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gestionar_reserva, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvNombre = view.findViewById<TextView>(R.id.tvDialogNombre)
        val tvFechaHora = view.findViewById<TextView>(R.id.tvDialogFechaHora)
        val tvServicio = view.findViewById<TextView>(R.id.tvDialogServicio)
        val btnEditar = view.findViewById<Button>(R.id.btnDialogEditar)
        val btnEliminar = view.findViewById<Button>(R.id.btnDialogEliminar)
        val tvCerrar = view.findViewById<TextView>(R.id.tvDialogCerrar)

        tvNombre.text = getString(R.string.label_nombre_param, cita.nombreCliente.uppercase())
        tvFechaHora.text = "${cita.fecha} - ${cita.hora}"
        tvServicio.text = cita.servicio

        btnEditar.setOnClickListener {
            onEditar(cita)
            dismiss()
        }

        btnEliminar.setOnClickListener {
            onEliminar(cita)
            dismiss()
        }

        tvCerrar.setOnClickListener {
            dismiss()
        }

        return view
    }
}