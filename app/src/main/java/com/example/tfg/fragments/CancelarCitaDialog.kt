package com.example.tfg.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.tfg.R

class CancelarCitaDialog(
    private val onConfirmar: () -> Unit
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_cancelar_cita, container, false)

        val btnNo = view.findViewById<Button>(R.id.btnNoCancelar)
        val btnSi = view.findViewById<Button>(R.id.btnSiCancelar)

        btnNo.setOnClickListener {
            dismiss()
        }

        btnSi.setOnClickListener {
            onConfirmar()
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            clearFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}