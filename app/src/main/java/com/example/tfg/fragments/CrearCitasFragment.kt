package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.R
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

        //BOTÓN VOLVER HACIA ATRÁS
        binding.btnBackCita.setOnClickListener {
            findNavController().popBackStack()
        }

        //BOTÓN GUARDAR CITA
        binding.btnGuardarCita.setOnClickListener {
            validarYGuardarCita()
        }

        //BOTÓN CON CALENDARIO PARA ELEGIR EL DÍA
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

        //BOTÓN CON RELOJ PARA ELEGIR LA HORA
        binding.etCitaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->

                //COMPROBAMOS SI LA HORA ESTÁ ENTRE LAS 9 Y LAS 20 (SE PUEDE MODIFICAR A LA HORA DE APERTURA Y CIERRE DEL ESTABLECIMIENTO)
                if (selectedHour in 9..20) {
                    //SI ES LA HORA 20, NOS ASEGURAMOS DE QUE SEAN EXACTAMENTE LAS 20:00
                    if (selectedHour == 20 && selectedMinute > 0) {
                        Toast.makeText(requireContext(), "El horario es hasta las 20:00", Toast.LENGTH_SHORT).show()
                    } else {
                        val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                        binding.etCitaHora.setText(time)
                    }
                } else {
                    //SI ESTÁ FUERA DE RANGO, AVISAMOS AL USUARIO
                    Toast.makeText(requireContext(), "Por favor, elige una hora entre las 09:00 y las 20:00", Toast.LENGTH_LONG).show()
                }

            }, currentHour, currentMinute, true)

            timePicker.show()
        }
    }

    private fun validarYGuardarCita() {
        val nombreCliente = binding.etCitaCliente.text.toString().trim()
        val servicio = binding.spinnerServicios?.selectedItem.toString().trim()
        val fecha = binding.etCitaFecha.text.toString().trim()
        val hora = binding.etCitaHora.text.toString().trim()

        //VALIDACIÓN DE DATOS

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

            //VALIDACIÓN DE TIEMPO
            mainRepository.verificarHorasCitas(fecha, hora, duracion) { choque ->
                if (choque) {
                    //SI EL PELUQUERO ESTÁ OCUPADO ENTRE LA FRANJA PROPUESTA, SE NOTIFICA AL USUARIO
                    Toast.makeText(requireContext(), "El peluquero está ocupado", Toast.LENGTH_LONG).show()
                } else {
                    //SI NO, SE CREA UNA NUEVA CITA
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

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            serviciosConHint
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView

                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.item_spinner_desplegable)

        binding.spinnerServicios?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}