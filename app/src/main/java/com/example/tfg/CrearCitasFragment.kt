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

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etCitaFecha.setText(date)
            }, year, month, day).show()
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
        val servicio = binding.etCitaServicio.text.toString().trim()
        val fecha = binding.etCitaFecha.text.toString().trim()
        val hora = binding.etCitaHora.text.toString().trim()

        // Paso 1: Verificar que no haya campos vacíos
        if (nombreCliente.isEmpty() || servicio.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return // Si entra aquí, no sigue
        }

        // Paso 2: Verificar si el cliente existe
        mainRepository.verificarClienteExiste(nombreCliente) { existe ->
            if (existe) {
                val nuevaCita = Cita(
                    nombreCliente,
                    servicio,
                    fecha,
                    hora
                )

                // Paso 3: Guardar la cita
                mainRepository.crearCita(nuevaCita) { exitoso ->
                    if (exitoso) {
                        // ESTO ES LO QUE HACE QUE LA PANTALLA REACCIONE
                        Toast.makeText(requireContext(), "Cita guardada con éxito", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack() // Vuelve atrás automáticamente
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Si no hace nada y no sale este mensaje, es que el nombre no coincide exactamente
                Toast.makeText(requireContext(), "Error: El cliente '$nombreCliente' no existe", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}