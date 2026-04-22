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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tfg.R
import com.example.tfg.databinding.FragmentClienteReservasBinding
import com.example.tfg.model.Cita
import com.example.tfg.repository.MainRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class ClienteReservasFragment : Fragment() {

    private var _binding: FragmentClienteReservasBinding? = null
    private val binding get() = _binding!!
    private val mainRepository = MainRepository()
    private var duracionServicios: Map<String, Int> = emptyMap()

    private var citaIdParaEditar: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClienteReservasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaIdParaEditar = arguments?.getString("CITA_ID")

        duracionServicios = mapOf(
            getString(R.string.servicio_corte) to 30,
            getString(R.string.servicio_tinte) to 60,
            getString(R.string.servicio_barba) to 20,
            getString(R.string.servicio_peinado) to 45
        )

        setupListeners()
        configurarSpinners()

        if(citaIdParaEditar != null) {
            cargarDatosCita(citaIdParaEditar!!)
        }
    }

    private fun cargarDatosCita(id: String) {
        mainRepository.getCitaPorId(id) { cita ->
            if (cita != null) {
                binding.etFechaReserva.setText(cita.fecha)
                binding.etHoraReserva.setText(cita.hora)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupListeners() {
        // VOLVER ATRÁS
        binding.btnBackCita.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // SELECCIONAR FECHA
        binding.etFechaReserva.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                val fecha = String.format("%02d/%02d/%d", d, m + 1, y)
                binding.etFechaReserva.setText(fecha)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = System.currentTimeMillis()
                show()
            }
        }

        // SELECCIONAR HORA
        binding.etHoraReserva.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, m ->
                if (h in 9..19) {
                    // Horario comercial
                    val hora = String.format("%02d:%02d", h, m)
                    binding.etHoraReserva.setText(hora)
                } else {
                    Toast.makeText(requireContext(), context?.getString(R.string.horarioHasta), Toast.LENGTH_SHORT).show()
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // BOTÓN CONFIRMAR RESERVA
        binding.btnConfirmarReserva.setOnClickListener {
            validarYReservar()
        }
    }

    private fun validarYReservar() {
        val servicio = binding.spinnerServicios.selectedItem?.toString() ?: ""
        val estilista = binding.spinnerEstilistas.selectedItem?.toString() ?: ""
        val fecha = binding.etFechaReserva.text.toString()
        val hora = binding.etHoraReserva.text.toString()
        val emailActual = FirebaseAuth.getInstance().currentUser?.email

        var esValido = true

        //Validaciones Spinners
        if (binding.spinnerServicios.selectedItemPosition <= 0) {
            Toast.makeText(requireContext(), getString(R.string.seleccionar_servicio), Toast.LENGTH_SHORT).show()
            esValido = false
        }

        if (binding.spinnerEstilistas.selectedItemPosition <= 0){
            Toast.makeText(requireContext(), getString(R.string.seleccionar_estilista), Toast.LENGTH_SHORT).show()
            esValido = false
        }

        //Validaciones para Botones
        if (fecha == getString(R.string.fecha)) {
            binding.etFechaReserva.setTextColor(Color.RED)
            esValido = false
        }

        if (hora == getString(R.string.hora)) {
            binding.etHoraReserva.setTextColor(Color.RED)
            esValido = false
        }

        if (!esValido) {
            Toast.makeText(requireContext(), getString(R.string.errorLoginCamposVacios), Toast.LENGTH_SHORT).show()
            return
        }

        val duracion = duracionServicios[servicio] ?: 30

        //Buscamos los datos del cliente por su email
        mainRepository.buscarClientePorEmail(emailActual) { cliente ->
            if (cliente != null) {
                //Verificamos que el peluquero no esté ocupado
                mainRepository.verificarHorasCitas(fecha, hora, duracion) { ocupado ->
                    if (ocupado && citaIdParaEditar == null) {
                        Toast.makeText(requireContext(), context?.getString(R.string.hora_reservada), Toast.LENGTH_LONG).show()
                    } else {
                        //Creamos la cita
                        val nuevaCita = Cita(
                            id = citaIdParaEditar ?: "",
                            idCliente = cliente.id,
                            nombreCliente = cliente.nombre,
                            telefonoCliente = cliente.telefono,
                            emailCliente = emailActual ?: "",
                            estilista = estilista,
                            servicio = servicio,
                            fecha = fecha,
                            hora = hora,
                            estado = "pendiente"
                        )

                        if (citaIdParaEditar != null) {
                            mainRepository.actualizarCita(nuevaCita) { exitoso ->
                                if (exitoso) {
                                    Toast.makeText(requireContext(), getString(R.string.citaActualizado), Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                            }
                        } else {
                            mainRepository.crearCita(nuevaCita) { exitoso ->
                                if (exitoso) {
                                    Toast.makeText(requireContext(), context?.getString(R.string.cita_reservada), Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configurarSpinners() {
        // Estilistas desde el Repositorio
        mainRepository.obtenerNombresEstilistas { lista ->
            setCustomAdapter(binding.spinnerEstilistas, context?.getString(R.string.seleccionar_estilista), lista)
        }
        // Servicios desde el Mapa
        setCustomAdapter(binding.spinnerServicios, context?.getString(R.string.seleccionar_servicio),
            duracionServicios.keys.toList()
        )
    }

    private fun setCustomAdapter(spinner: Spinner, hint: String?, items: List<String>) {
        val listaConHint = mutableListOf(hint).apply { addAll(items) }
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, listaConHint) {
            override fun isEnabled(position: Int): Boolean = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return v
            }
        }
        adapter.setDropDownViewResource(R.layout.item_spinner_desplegable)
        spinner.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}