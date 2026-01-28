package com.example.tfg.repository

import com.example.tfg.model.Cita
import com.example.tfg.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // --- REFERENCIAS DINÁMICAS ---
    private fun getBaseRef(): DatabaseReference {
        val uid = auth.currentUser?.uid ?: "anónimo"
        return database.child("usuarios").child(uid)
    }

    private fun getRefClientes() = getBaseRef().child("clientes")
    private fun getRefCitas() = getBaseRef().child("citas")

    // --- LÓGICA DE CLIENTES ---
    fun getClientes(onResult: (List<Cliente>) -> Unit) {
        getRefClientes().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaClientes = snapshot.children.mapNotNull { datos ->
                    val cliente = datos.getValue(Cliente::class.java)
                    cliente?.copy(id = datos.key ?: "")
                }
                onResult(listaClientes)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun getDetalleCliente(clienteId: String, onResult: (Cliente?) -> Unit) {
        getRefClientes().child(clienteId).get().addOnSuccessListener { snapshot ->
            onResult(snapshot.getValue(Cliente::class.java))
        }
    }

    fun insertarCliente(cliente: Cliente) {
        val key = getRefClientes().push().key
        key?.let { id ->
            getRefClientes().child(id).setValue(cliente.copy(id = id))
        }
    }

    fun actualizarCliente(cliente: Cliente) {
        if (cliente.id.isNotEmpty()) {
            getRefClientes().child(cliente.id).setValue(cliente)
        }
    }

    fun eliminarCliente(clienteId: String) {
        getRefClientes().child(clienteId).removeValue()
    }

    // --- LÓGICA DE CITAS ---
    fun getCitasHoy(onResult: (List<Cita>) -> Unit) {
        getRefCitas().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaCitas = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                onResult(listaCitas)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun insertarCita(cita: Cita) {
        val key = getRefCitas().push().key
        key?.let { getRefCitas().child(it).setValue(cita) }
    }

    // --- LÓGICA DE USUARIOS ---
    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful, task.exception?.message)
            }
    }

    fun isUsuarioLogueado(): Boolean = auth.currentUser != null

    fun getUsuarioActual() = auth.currentUser
}