package com.example.tfg

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.databinding.FragmentCrearCitasBinding
import com.example.tfg.model.Cita
import com.example.tfg.repository.MainRepository
import java.util.Calendar

class CrearCitasFragment : Fragment() {

    private var _binding: FragmentCrearCitasBinding? = null
    private val binding get() = _binding!!
    private val mainRepository = MainRepository()
    private val duracionServicios = mapOf(
        "Corte" to 30,
        "Tinte" to 60,
        "Barba" to 20,
        "Peinado" to 45
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearCitasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        configurarSpinner()
    }

    @SuppressLint("DefaultLocale")
    private fun setupListeners(){

        binding.btnBackCita.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnGuardarCita.setOnClickListener {
            validarYGuardarCita()
        }

        binding.etCitaFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val picker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etCitaFecha.setText(date)
            }, year, month, day)

            //LÓGICA PARA QUE NO TE DEJE ELEGIR LOS DÍAS ANTERIORES
            picker.datePicker.minDate = System.currentTimeMillis()

            picker.show()
        }

        binding.etCitaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.etCitaHora.setText(time)
            }, hour, minute, true).show()
        }
    }

    private fun validarYGuardarCita() {
        val nombreCliente = binding.etCitaCliente.text.toString().trim()
        val servicio = binding.spinnerServicios?.selectedItem.toString().trim()
        val fecha = binding.etCitaFecha.text.toString().trim()
        val hora = binding.etCitaHora.text.toString().trim()
        if (binding.spinnerServicios?.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Por favor, selecciona un servicio", Toast.LENGTH_SHORT).show()
            return
        }
        val duracion = duracionServicios[servicio] ?: 30

        if (nombreCliente.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        mainRepository.verificarClienteExiste(nombreCliente) { idRecuperado ->
            if (idRecuperado == null) {
                Toast.makeText(requireContext(), "El cliente no existe", Toast.LENGTH_SHORT).show()
                return@verificarClienteExiste
            }

            mainRepository.verificarHorasCitas(fecha, hora, duracion) { choque ->
                if (choque) {
                    Toast.makeText(requireContext(), "El peluquero está ocupado", Toast.LENGTH_LONG).show()
                } else {
                    val nuevaCita = Cita(
                        id = "",
                        idCliente = idRecuperado,
                        nombre = nombreCliente,
                        servicio = servicio,
                        fecha = fecha,
                        hora = hora
                    )

                    mainRepository.crearCita(nuevaCita) { exitoso ->
                        if (exitoso) {
                            Toast.makeText(requireContext(), "Cita confirmada", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun configurarSpinner() {
        val placeholder = getString(R.string.servicio)
        val serviciosConHint = mutableListOf<String>(placeholder)
        serviciosConHint.addAll(duracionServicios.keys)

        val adapter = object : android.widget.ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            serviciosConHint
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as android.widget.TextView

                if (position == 0) {
                    tv.setTextColor(android.graphics.Color.GRAY)
                } else {
                    tv.setTextColor(android.graphics.Color.BLACK)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerServicios?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}