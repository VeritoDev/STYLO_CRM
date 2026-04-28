package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tfg.R
import com.example.tfg.databinding.FragmentCrearCitasBinding
import com.example.tfg.model.Cita
import com.example.tfg.repository.MainRepository
import com.example.tfg.repository.capitalizarFormato
import com.example.tfg.worker.NotificacionWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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
            binding.root.post {
                if (telefonoRecibido.contains(" ")) {
                    val partes = telefonoRecibido.split(" ")
                    binding.etPrefijo?.setText(partes[0])
                    binding.etCitaCliente.setText(partes[1])
                } else {
                    binding.etPrefijo?.setText("+34")
                    binding.etCitaCliente.setText(telefonoRecibido)
                }
            }
        }

        if (citaIdParaEditar != null) {
            configurarModoEdicion(citaIdParaEditar!!)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupListeners() {

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

            picker.datePicker.minDate = System.currentTimeMillis()
            picker.show()
        }

        binding.etCitaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->

                if (selectedHour in 9..20) {
                    if (selectedHour == 20 && selectedMinute > 0) {
                        Toast.makeText(requireContext(), context?.getString(R.string.horario), Toast.LENGTH_SHORT).show()
                    } else {
                        val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                        binding.etCitaHora.setText(time)
                    }
                } else {
                    Toast.makeText(requireContext(), context?.getString(R.string.hora_elegida), Toast.LENGTH_LONG).show()
                }

            }, currentHour, currentMinute, true)

            timePicker.show()
        }
    }

    private fun validarYGuardarCita() {
        val numeroBase = binding.etCitaCliente.text.toString().trim()
        val prefijo = binding.etPrefijo?.text.toString().trim()

        // UNIMOS EL TELÉFONO ANTES DE BUSCAR Y GUARDAR
        val telefonoFinal = "$prefijo $numeroBase"

        val servicio = binding.spinnerServicios?.text.toString().trim()
        val prefs = requireContext().getSharedPreferences("config_app", android.content.Context.MODE_PRIVATE)
        val estilista = prefs.getString("user_name_key", "Profesional") ?: "Profesional"
        val fecha = binding.etCitaFecha.text.toString().trim()
        val hora = binding.etCitaHora.text.toString().trim()

        binding.tilCitaCliente.error = null
        binding.tilCitaFecha.error = null
        binding.tilCitaHora.error = null

        var esValido = true

        if (numeroBase.isEmpty()) {
            binding.tilCitaCliente.error = " "
            esValido = false
        }

        if (servicio.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, elige un servicio", Toast.LENGTH_SHORT).show()
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

        // BUSCAMOS CON EL TELÉFONO COMPLETO
        mainRepository.buscarClientePorTelefono(telefonoFinal) { cliente ->
            if (cliente == null) {
                Toast.makeText(requireContext(), context?.getString(R.string.noExisteCliente), Toast.LENGTH_SHORT).show()
                return@buscarClientePorTelefono
            }

            mainRepository.verificarCitaMismoDiaCliente(cliente.id, fecha, citaIdParaEditar) { yaTieneCita ->
                if(yaTieneCita) {
                    Toast.makeText(requireContext(), getString(R.string.error_cita_mismo_dia), Toast.LENGTH_SHORT).show()
                    return@verificarCitaMismoDiaCliente
                }
            }

            mainRepository.verificarHorasCitas(fecha, hora, duracion) { choque ->
                if (choque && citaIdParaEditar == null) {
                    Toast.makeText(requireContext(), context?.getString(R.string.estilistaOcupado), Toast.LENGTH_LONG).show()
                } else {
                    val nuevaCita = Cita(
                        id = citaIdParaEditar ?: "",
                        idCliente = cliente.id,
                        nombreCliente = cliente.nombre.capitalizarFormato(),
                        telefonoCliente = telefonoFinal, // GUARDAMOS EL TELÉFONO COMPLETO
                        emailCliente = cliente.email,
                        estilista = estilista.capitalizarFormato(),
                        servicio = servicio,
                        fecha = fecha,
                        hora = hora,
                        estado = "pendiente"
                    )

                    if (citaIdParaEditar != null) {
                        mainRepository.actualizarCita(nuevaCita) { exitoso ->
                            if (exitoso) {
                                Toast.makeText(requireContext(), context?.getString(R.string.citaActualizado), Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                        }
                    } else {
                        mainRepository.crearCita(nuevaCita) { exitoso ->
                            if (exitoso) {
                                Toast.makeText(requireContext(), context?.getString(R.string.citaConfirmada), Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configurarSpinner() {
        val listaServicios = duracionServicios.keys.toList()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            listaServicios
        )
        binding.spinnerServicios?.setAdapter(adapter)
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
                // SEPARAR EL PREFIJO AL EDITAR LA CITA
                val telCompleto = cita.telefonoCliente
                if (telCompleto.contains(" ")) {
                    val partes = telCompleto.split(" ")
                    binding.etPrefijo?.setText(partes[0])
                    binding.etCitaCliente.setText(partes[1])
                } else {
                    binding.etPrefijo?.setText("+34")
                    binding.etCitaCliente.setText(telCompleto)
                }

                binding.etCitaFecha.setText(cita.fecha)
                binding.etCitaHora.setText(cita.hora)

                binding.spinnerServicios?.setText(cita.servicio, false)
            }
        }
    }

    private fun programarNotificacion(cita: Cita) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaCita = formato.parse("${cita.fecha} ${cita.hora}") ?: return

        // Calculamos cuánto tiempo falta para la cita
        val ahora = System.currentTimeMillis()
        val delay = (fechaCita.time - 3600000) - ahora
        val delayFinal = if (delay > 0) delay else 10000L

        val data = workDataOf("SERVICIO" to cita.servicio, "HORA" to cita.hora)

        val request = OneTimeWorkRequestBuilder<NotificacionWorker>()
            .setInitialDelay(delayFinal, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(cita.id)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}