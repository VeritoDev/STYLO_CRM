package com.example.tfg

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.snap
import androidx.compose.material3.TimePickerDialog
import androidx.compose.runtime.Composable
import com.example.tfg.databinding.ActivityClienteReservasBinding
import com.google.firebase.database.FirebaseDatabase
import androidx.core.net.toUri
import com.example.tfg.repository.MainRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import javax.security.auth.callback.Callback

class ClienteReservasActivity: AppCompatActivity() {

    private lateinit var binding: ActivityClienteReservasBinding
    private val db = FirebaseDatabase.getInstance().reference
    private var fechaSeleccionada = ""
    private var horaSeleccionada = ""
    private var clienteId = ""
    private var mainRepository = MainRepository()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityClienteReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clienteId = intent.getStringExtra("CLIENTE_ID") ?: ""

        setupListeners()
        cargarEstilistas()
    }

    private fun setupListeners(){
        binding.btnFechaCard.setOnClickListener {
            mostrarDatePicker { fecha ->
                fechaSeleccionada = fecha
                binding.btnFechaCard.text = fecha
                binding.btnFechaCard.setTypeface(null, Typeface.BOLD)
            }
        }

        binding.btnHoraCard.setOnClickListener {
            mostrarTimePicker { hora ->
                horaSeleccionada = hora
                binding.btnHoraCard.text = hora
                binding.btnHoraCard.setTypeface(null, Typeface.BOLD)
            }
        }

        binding.btnEnviarMensaje.setOnClickListener {
            val estilista = binding.spinnerEstilistas.selectedItem.toString()
            val mensaje = "@string/mensajeCliente"
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = "https://api.whatsapp.com/send?phone=34600000000&text=${Uri.encode(mensaje)}"
            intent.data = uri.toUri()
            startActivity(intent)
        }

        binding.btnConfirmarReserva.setOnClickListener {
            if(validarCampos()) {
                enviarCitaAFirebase()
            }
        }
        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        if(fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()){
            Toast.makeText(this, "@string/seleccionarFechaHora", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun enviarCitaAFirebase(){
        val cita = mapOf(
            "clienteId" to clienteId,
            "estilista" to binding.spinnerEstilistas.selectedItem.toString(),
            "fecha" to fechaSeleccionada,
            "hora" to horaSeleccionada
        )

        db.child("citas").push().setValue(cita).addOnSuccessListener {
            Toast.makeText(this, "@string/reserva", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun mostrarDatePicker(callback: (String) -> Unit){
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, {_, selectedYear, selectedMonth, selecredDay ->
            val fechaFormateada = String.format("%02d/%02d/%04d", selecredDay, selectedMonth + 1, selectedYear)
            callback(fechaFormateada)
        }, year, month, day)

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    @SuppressLint("DefaultLocale")
    private fun mostrarTimePicker(callback: (String) -> Unit) {
        val calendario = Calendar.getInstance()
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)

        val timePicker = android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->

            if (selectedHour in 9..20) {
                if (selectedHour == 20 && selectedMinute > 0) {
                    Toast.makeText(this, "El salón cierra a las 20:00", Toast.LENGTH_SHORT).show()
                } else {
                    val horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute)
                    callback(horaFormateada)
                }
            } else {
                Toast.makeText(this, "Horario comercial: 09:00 a 20:00", Toast.LENGTH_LONG).show()
            }

        }, horaActual, minutoActual, true)

        timePicker.show()
    }

    private fun cargarEstilistas() {
            val placeholder = "Selecciona un estilista"

            mainRepository.obtenerNombresEstilistas { listaEstilistas ->
                val estilistasConHint = mutableListOf<String>(placeholder)
                estilistasConHint.addAll(listaEstilistas)

                val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estilistasConHint) {
                    override fun isEnabled(position: Int): Boolean = position != 0

                    override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
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

                adapter.setDropDownViewResource(R.layout.item_spinner_desplegable)

                binding.spinnerEstilistas.adapter = adapter
            }
        }
}