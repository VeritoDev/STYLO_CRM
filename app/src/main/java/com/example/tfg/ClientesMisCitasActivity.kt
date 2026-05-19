package com.example.tfg

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tfg.adapter.CitasAdapter
import com.example.tfg.databinding.ActivityClienteMisCitasBinding
import com.example.tfg.fragments.ClienteReservasFragment
import com.example.tfg.model.Cita
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.tfg.fragments.GestionarReservaDialog
import com.example.tfg.fragments.CancelarCitaDialog

class ClientesMisCitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteMisCitasBinding
    private val db = FirebaseDatabase.getInstance().reference
    
    private lateinit var adapterPendientes: CitasAdapter
    private lateinit var adapterFinalizadas: CitasAdapter

    private var citasListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteMisCitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                aplicarEfectoBlur(false)
            }
        }

        setupRecyclerViews()
        setupListeners()
        cargarCitasDelCliente()
    }

    private fun aplicarEfectoBlur(activar: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(activar) {
                val blur = RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP)
                binding.root.setRenderEffect(blur)
            } else {
                binding.root.setRenderEffect(null)
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rvCitasPendientes.layoutManager = LinearLayoutManager(this)
        binding.rvCitasFinalizadas.layoutManager = LinearLayoutManager(this)

        adapterPendientes = CitasAdapter(emptyList(), onCitaClick = { cita ->
            mostrarOpcionesCita(cita)
        }, esEstilista = false)

        adapterFinalizadas = CitasAdapter(emptyList(), onCitaClick = {
            Toast.makeText(this, getString(R.string.citaFinalizada), Toast.LENGTH_SHORT).show()
        }, esEstilista = false)

        binding.rvCitasPendientes.adapter = adapterPendientes
        binding.rvCitasFinalizadas.adapter = adapterFinalizadas
    }

    private fun setupListeners() {
        binding.btnVolverReservar.setOnClickListener {
            aplicarEfectoBlur(true)
            val fragment = ClienteReservasFragment()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.deslizar_derecha_dentro,
                    R.anim.deslizar_izquierda_fuera,
                    R.anim.deslizar_izquierda_dentro,
                    R.anim.deslizar_derecha_fuera
                )
                .add(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnCerrarSesion.setOnClickListener {
            citasListener?.let {
                db.child("citas").removeEventListener(it)
                citasListener = null
            }
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarCitasDelCliente() {
        val emailActual = FirebaseAuth.getInstance().currentUser?.email ?: return

        citasListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaTotal = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }

                val pendientes = listaTotal.filter { it.estado != "finalizado" }
                    .sortedWith(compareBy({ it.fecha }, { it.hora }))
                
                val finalizadas = listaTotal.filter { it.estado == "finalizado" }
                    .sortedWith(compareByDescending<Cita> { it.fecha }.thenByDescending { it.hora })

                if (pendientes.isEmpty() && finalizadas.isEmpty()) {
                    binding.tvSinCitas.visibility = View.VISIBLE
                    binding.tvTituloPendientes.visibility = View.GONE
                    binding.tvTituloFinalizadas.visibility = View.GONE
                    binding.rvCitasPendientes.visibility = View.GONE
                    binding.rvCitasFinalizadas.visibility = View.GONE
                } else {
                    binding.tvSinCitas.visibility = View.GONE
                    
                    // Manejo de Pendientes
                    binding.tvTituloPendientes.visibility = if (pendientes.isNotEmpty()) View.VISIBLE else View.GONE
                    binding.rvCitasPendientes.visibility = if (pendientes.isNotEmpty()) View.VISIBLE else View.GONE
                    adapterPendientes.actualizarLista(pendientes)

                    // Manejo de Finalizadas
                    binding.tvTituloFinalizadas.visibility = if (finalizadas.isNotEmpty()) View.VISIBLE else View.GONE
                    binding.rvCitasFinalizadas.visibility = if (finalizadas.isNotEmpty()) View.VISIBLE else View.GONE
                    adapterFinalizadas.actualizarLista(finalizadas)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isFinishing) {
                    Toast.makeText(this@ClientesMisCitasActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        db.child("citas")
            .orderByChild("emailCliente")
            .equalTo(emailActual)
            .addValueEventListener(citasListener!!)
    }

    private fun mostrarOpcionesCita(cita: Cita) {
        val dialog = GestionarReservaDialog(
            cita = cita,
            onEditar = { citaAEditar -> editarCita(citaAEditar) },
            onEliminar = { citaAEliminar -> confirmarCancelacion(citaAEliminar) }
        )
        dialog.show(supportFragmentManager, "GestionarReserva")
    }

    private fun editarCita(cita: Cita) {
        aplicarEfectoBlur(true)
        val fragment = ClienteReservasFragment()
        val bundle = Bundle().apply {
            putString("CITA_ID", cita.id)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.deslizar_derecha_dentro,
                R.anim.deslizar_izquierda_fuera,
                R.anim.deslizar_izquierda_dentro,
                R.anim.deslizar_derecha_fuera
            )
            .add(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmarCancelacion(cita: Cita) {
        val dialog = CancelarCitaDialog {
            cancelarCitaEnFireBase(cita)
        }
        dialog.show(supportFragmentManager, "CancelarCita")
    }

    private fun cancelarCitaEnFireBase(cita: Cita) {
        db.child("citas").child(cita.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, getString(R.string.cita_cancelada), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        citasListener?.let { db.child("citas").removeEventListener(it) }
    }

    override fun onStop() {
        super.onStop()
        citasListener?.let { db.child("citas").removeEventListener(it) }
    }
}
