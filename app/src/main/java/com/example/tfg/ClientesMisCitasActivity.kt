package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class ClientesMisCitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteMisCitasBinding
    private val db = FirebaseDatabase.getInstance().reference
    private lateinit var adapter: CitasAdapter
    private val listaCitas = mutableListOf<Cita>()

    private var citasListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClienteMisCitasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        cargarCitasDelCliente()
    }

    private fun setupRecyclerView() {
        binding.rvMisCitas.layoutManager = LinearLayoutManager(this)

        adapter = CitasAdapter(listaCitas, esEstilista = false, onCitaClick = { cita ->
            mostrarOpcionesCita(cita)
        })

        binding.rvMisCitas.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnVolverReservar.setOnClickListener {
            val fragment = ClienteReservasFragment()

            supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
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

                val listaFiltrada = listaTotal.filter {it.estado != "finalizado"}

                if(listaFiltrada.isEmpty()){
                    binding.tvSinCitas.text = getString(R.string.citas_vacio)
                    binding.tvSinCitas.visibility = View.VISIBLE
                    binding.rvMisCitas.visibility = View.GONE
                    adapter.actualizarLista(emptyList())
                } else {
                    binding.tvSinCitas.visibility = View.GONE
                    binding.rvMisCitas.visibility = View.VISIBLE
                    adapter.actualizarLista(listaFiltrada.sortedWith(compareBy ({ it.fecha }, { it.hora })))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isFinishing){
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
        val opciones = arrayOf(getString(R.string.opcion_editar), getString(R.string.opcion_cancelar))

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.titulo_gestion_cita))
        builder.setItems(opciones) { _, which ->
            when (which) {
                0 -> editarCita(cita)
                1 -> confirmarCancelacion(cita)
            }
        }
        builder.show()
    }

    private fun editarCita(cita: Cita) {
        val fragment = ClienteReservasFragment()
        val bundle = Bundle().apply {
            putString("CITA_ID", cita.id)
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmarCancelacion(cita: Cita) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.opcion_cancelar))
            .setMessage(getString(R.string.confirmar_cancelacion))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                cancelarCitaEnFireBase(cita)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun cancelarCitaEnFireBase(cita: Cita){
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