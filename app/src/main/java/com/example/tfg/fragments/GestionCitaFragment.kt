package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tfg.R
import androidx.fragment.app.DialogFragment
import com.example.tfg.databinding.FragmentGestionCitaBinding
import com.example.tfg.model.Cita

class GestionCitaFragment (
    private val cita: Cita,
    private val onFinalizada: () -> Unit,
    private val onEditar: () -> Unit
) : DialogFragment() {
    private var _binding: FragmentGestionCitaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGestionCitaBinding.inflate(inflater, container, false)

        binding.tvPregunta.text = getString(R.string.finalizarCita)

        binding.btnSi.setOnClickListener {
            onFinalizada()
            dismiss()
        }

        binding.btnNo.setOnClickListener {
            onEditar()
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val params = attributes

            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            params.width = width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT

            attributes = params
        }
    }
}