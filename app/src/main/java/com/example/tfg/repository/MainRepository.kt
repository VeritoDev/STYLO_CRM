package com.example.tfg.repository

import com.example.tfg.model.Cita
import com.example.tfg.model.Cliente
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class MainRepository() {
    //HACEMOS REFERENCIA A LA BASE DE DATOS
    private val database = Firebase.database
    private val database_cliente: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()


    // HACEMOS REDERENCIA A CADA TABLA DE FIREBASE
    private val refCitas = database.getReference("citas")
    private val refClientes = database.getReference("clientes")
    private val refUsuarios = database.getReference("usuarios")

    // --- LÓGICA DE CITAS ---
    fun getCitasHoy(onResult: (List<Cita>) -> Unit) {
        refCitas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaCitas = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                onResult(listaCitas)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun insertarCita(cita: Cita) {
        val key = refCitas.push().key
        key?.let { refCitas.child(it).setValue(cita) }
    }

    // --- LÓGICA DE CLIENTES ---

    fun getClientes(onResult: (List<Cliente>) -> Unit) {
        database_cliente.child("clientes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaClientes = mutableListOf<Cliente>()
                for (datos in snapshot.children) {
                    val cliente = datos.getValue(Cliente::class.java)
                    cliente?.let { listaClientes.add(it.copy(id = datos.key ?: "")) }
                }
                onResult(listaClientes)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun getDetalleCliente(clienteId: String, onResult: (Cliente?) -> Unit) {
        refClientes.child(clienteId).get().addOnSuccessListener { snapshot ->
            onResult(snapshot.getValue(Cliente::class.java))
        }
    }

    fun insertarCliente(cliente: Cliente) {
        val key = refClientes.push().key
        // Al guardar, le metemos el ID generado dentro del objeto para poder editarlo luego
        key?.let { refClientes.child(it).setValue(cliente.copy(id = it)) }
    }

    fun actualizarCliente(cliente: Cliente) {
        refClientes.child(cliente.id).setValue(cliente)
    }

    fun eliminarCliente(clienteId: String) {
        refClientes.child(clienteId).removeValue()
    }

    // --- LÓGICA DE USUARIOS ---
    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun isUsuarioLogueado(): Boolean{
        FirebaseAuth.getInstance().currentUser
        return auth.currentUser != null
    }

    //VERIFICAMOS SI YA HAY UN USUARIO LOGUEADO AL ABRIR LA APP
    fun getUsuarioActual() = auth.currentUser
}