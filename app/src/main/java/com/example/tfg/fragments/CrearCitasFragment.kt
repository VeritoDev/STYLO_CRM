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
    private val duracionServicios = mutableMapOf<String, Int>()
    private var citaIdParaEditar: String? = null

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

        citaIdParaEditar = arguments?.getString("CITA_ID")

        val telefonoRecibido = arguments?.getString("TELEFONO_CLIENTE")

        inicializarServicios()
        setupListeners()
        configurarSpinner()

        if (!telefonoRecibido.isNullOrEmpty()) {
            binding.etCitaCliente.setText(telefonoRecibido)
            binding.tilCitaCliente.isEnabled = false //DESHABILITAMOS PARA QUE NO SE CAMBIE EL NUMERO DE TELÉFONO
        }

        if (citaIdParaEditar != null) {
            configurarModoEdicion(citaIdParaEditar!!)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupListeners() {

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

            val picker =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val date =
                        String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
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
                        Toast.makeText(
                            requireContext(),
                            context?.getString(R.string.horario),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                        binding.etCitaHora.setText(time)
                    }
                } else {
                    //SI ESTÁ FUERA DE RANGO, AVISAMOS AL USUARIO
                    Toast.makeText(
                        requireContext(),
                        context?.getString(R.string.hora_elegida),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }, currentHour, currentMinute, true)

            timePicker.show()
        }
    }

    private fun validarYGuardarCita() {
        val telefonoCliente = binding.etCitaCliente.text.toString().trim()
        val servicio = binding.spinnerServicios?.selectedItem?.toString()?.trim() ?: ""
        val prefs = requireContext().getSharedPreferences("config_app", android.content.Context.MODE_PRIVATE)
        val estilista = prefs.getString("user_name_key", "Profesional") ?: "Profesional"
        val fecha = binding.etCitaFecha.text.toString().trim()
        val hora = binding.etCitaHora.text.toString().trim()

        //RESETEAMOS LOS ERRORES
        binding.tilCitaCliente.error = null
        binding.tilCitaServicio?.error = null
        binding.tilCitaFecha.error = null
        binding.etCitaHora.error = null

        var esValido = true

        if(binding.spinnerServicios?.selectedItemPosition == 0) {
            binding.tilCitaServicio?.error = " "
            esValido = false
        }

        if (telefonoCliente.isEmpty()) {
            binding.tilCitaCliente.error = " "
            esValido = false
        }

        if (fecha.isEmpty()) {
            binding.tilCitaFecha.error = " "
            esValido = false
        }

        if (hora.isEmpty()) {
            binding.tilCitaHora.error = " "
            esValido = false
        }

        if (!esValido) {
            Toast.makeText(requireContext(), getString(R.string.errorLoginCamposVacios), Toast.LENGTH_SHORT).show()
            return
        }

        val duracion = duracionServicios[servicio] ?: 30

        // BUSCAMOS AL CLIENTE POR NÚMERO DE TELÉFONO
        mainRepository.buscarClientePorTelefono(telefonoCliente) { cliente ->
            if (cliente == null) {
                Toast.makeText(
                    requireContext(),
                    context?.getString(R.string.noExisteCliente),
                    Toast.LENGTH_SHORT
                ).show()
                return@buscarClientePorTelefono
            }

            mainRepository.verificarHorasCitas(fecha, hora, duracion) { choque ->
                if (choque && citaIdParaEditar == null) {
                    Toast.makeText(
                        requireContext(),
                        context?.getString(R.string.estilistaOcupado),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val nuevaCita = Cita(
                        id = citaIdParaEditar ?: "",
                        idCliente = cliente.id,
                        nombreCliente = cliente.nombre,
                        telefonoCliente = cliente.telefono,
                        emailCliente = cliente.email,
                        estilista = estilista,
                        servicio = servicio,
                        fecha = fecha,
                        hora = hora,
                        estado = "pendiente"
                    )

                    if (citaIdParaEditar != null) {
                        mainRepository.actualizarCita(nuevaCita) { exitoso ->
                            if (exitoso) {
                                Toast.makeText(
                                    requireContext(), context?.getString(R.string.citaActualizado),
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                    } else {
                        mainRepository.crearCita(nuevaCita) { exitoso ->
                            if (exitoso) {
                                Toast.makeText(
                                    requireContext(),
                                    context?.getString(R.string.citaConfirmada),
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configurarSpinner() {
        val placeholder = getString(R.string.servicio)
        val serviciosConHint = mutableListOf(placeholder)
        serviciosConHint.addAll(duracionServicios.keys as Collection<String>)

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            serviciosConHint
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView

                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.item_spinner_desplegable)
        binding.spinnerServicios?.adapter = adapter
    }

    private fun inicializarServicios() {
        duracionServicios[getString(R.string.servicio_corte)] = 30
        duracionServicios[getString(R.string.servicio_tinte)] = 60
        duracionServicios[getString(R.string.servicio_barba)] = 20
        duracionServicios[getString(R.string.servicio_peinado)] = 45
    }

    private fun configurarModoEdicion(id: String) {
        binding.btnGuardarCita.text = context?.getString(R.string.editar)

        mainRepository.getCitaPorId(id) { cita ->
            if (cita != null) {
                binding.etCitaCliente.setText(cita.telefonoCliente)
                binding.etCitaFecha.setText(cita.fecha)
                binding.etCitaHora.setText(cita.hora)

                val adapter = binding.spinnerServicios?.adapter
                val totalElementos = adapter?.count ?: 0

                val posicion = (0 until totalElementos).indexOfFirst { i ->
                    adapter?.getItem(i).toString() == cita.servicio
                }

                if (posicion != -1) {
                    binding.spinnerServicios?.setSelection(posicion)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}