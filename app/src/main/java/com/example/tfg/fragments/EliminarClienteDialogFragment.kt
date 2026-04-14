package com.example.tfg.fragments

import com.example.tfg.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class EliminarClienteDialogFragment(val onConfirm: () -> Unit) : androidx.fragment.app.DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.fragment_confirmar_eliminar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnCancelarEliminar).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnConfirmarEliminar).setOnClickListener {
            onConfirm()
            dismiss()
        }
    }
}