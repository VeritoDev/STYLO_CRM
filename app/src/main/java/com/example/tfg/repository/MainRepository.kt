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

    // -- REFERENCIAS A LA BASE DE DATOS --
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // --- REFERENCIAS DINÁMICAS ---
    private fun getBaseRef(): DatabaseReference {
        val uid = auth.currentUser?.uid ?: "anónimo"
        return database.child("usuario").child(uid)
    }

    private fun getRefClientes() = getBaseRef().child("clientes")
    private fun getRefCitas() = getBaseRef().child("citas")

    // --- LÓGICA DE CLIENTES ---
    fun getClientes(onResult: (List<Cliente>) -> Unit) {
        getRefClientes().addValueEventListener(object : ValueEventListener {
            //DATASNAPSHOT ES UNA "FOTO" DE COMO ESTÁ LA BASE DE DATOS EN ESE MOMENTO
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaClientes = snapshot.children.mapNotNull { datos ->
                    //CONVERTIMOS LA FOTO EN UN OBJETO CLIENTE
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
    fun obtenerDetalleCliente(id: String, callback: (Cliente?) -> Unit) {
        getRefClientes().child(id).get().addOnSuccessListener { snapshot ->
            val cliente = snapshot.getValue(Cliente::class.java)
            callback(cliente)
        }.addOnFailureListener {
            callback(null)
        }
    }
    fun verificarClienteExiste(nombre: String, callback: (String?) -> Unit) {
        //PONERMOS EL NOMBRE EN MINUSCULAS
        val nombreEnMinusculas = nombre.lowercase().trim()
        getRefClientes().orderByChild("nombre").equalTo(nombreEnMinusculas)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    //SI EL IS ES LA KEY DEL NODO (ej: -Ol5dbV9...)
                    val id = snapshot.children.first().key
                    callback(id)
                } else {
                    callback(null)
                }
            }.addOnFailureListener {
                callback(null)
            }
    }

    fun insertarCliente(cliente: Cliente) {
        val key = getRefClientes().push().key
        key?.let { id ->
            //CONVERTIMOS EL NOMBRE EN MINÚSUCULAS ANTES DE GUARDAR
            val clienteNormalizado = cliente.copy(
                id = id,
                nombre = cliente.nombre.lowercase().trim()
            )
            getRefClientes().child(id).setValue(clienteNormalizado)
        }
    }

    fun buscarClientePorEmail(email: String, callback: (Cliente?) -> Unit){
        database.child("clientes").orderByChild("email").equalTo(email.trim()).get().addOnSuccessListener { snapshot ->
            if(snapshot.exists()) {
                val data = snapshot.children.firstOrNull()
                val cliente = data?.getValue(Cliente::class.java)

                if (cliente != null){
                    cliente.id = data.key ?: ""
                }
                callback(cliente)
            } else {
                callback(null)
            }
        }
            .addOnFailureListener {
                callback(null)
            }
    }
    fun eliminarCliente(clienteId: String) {
        getRefClientes().child(clienteId).removeValue()
    }
    fun actualizarCliente(id: String, datos: Map<String, Any>, callback: (Boolean) -> Unit) {
        //CREAMOS UNA COPIA MUTABLE DE LOS DATOS PARA MODIFICAR EL NOMBRE SI EXISTE
        val datosNormalizados = datos.toMutableMap()

        if (datosNormalizados.containsKey("nombre")) {
            val nombre = datosNormalizados["nombre"].toString()
            datosNormalizados["nombre"] = nombre.lowercase().trim()
        }

        getRefClientes().child(id).updateChildren(datosNormalizados)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // --- LÓGICA DE CITAS ---
    //LÓGICA PARA QUE SE VEA EN LA PANTALLA DE CITAS
    fun getTodasLasCitas(onResult: (List<Cita>) -> Unit) {
        getRefCitas().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                onResult(lista)
            }
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }
    //LÓGICA PARA QUE SE VEA EN LA PANTALLA PRINCIPAL
    fun getCitasHoy(onResult: (List<Cita>) -> Unit) {
        getRefCitas().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaCitas = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                onResult(listaCitas)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
    fun crearCita(cita: Cita, callback: (Boolean) -> Unit) {
        val dbRef = getRefCitas()
        val id = dbRef.push().key

        if (id != null) {
            val citaConId = cita.copy(id = id)
            dbRef.child(id).setValue(citaConId)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        } else {
            callback(false)
        }
    }
    //LÓGICA PARA COMPARAR TODAS LAS HORAS DE LAS CITAS EXISTENTES
    fun verificarHorasCitas(fecha: String, horaNueva: String, duracionNueva: Int, onResult: (Boolean) -> Unit) {
        getRefCitas().orderByChild("fecha").equalTo(fecha)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val inicioNueva = horaAMinutos(horaNueva)
                    val finNueva = inicioNueva + duracionNueva

                    var hayChoque = false

                    for (data in snapshot.children) {
                        val citaExistente = data.getValue(Cita::class.java) ?: continue

                        val duracionExistente = extraerDuracion(citaExistente.servicio)
                        val inicioExistente = horaAMinutos(citaExistente.hora)
                        val finExistente = inicioExistente + duracionExistente

                        //LÓGICA PARA COMPARAR LAS HORAS DE LAS CITAS
                        if (inicioNueva < finExistente && finNueva > inicioExistente) {
                            hayChoque = true
                            break
                        }
                    }
                    onResult(hayChoque)
                }
                override fun onCancelled(error: DatabaseError) = onResult(true)
            })
    }
    //LÓGICA PARA PONER EL HISTORIAL DEL CLIENTE
    fun getHistorialCitasCliente(idCliente: String, onResult: (List<Cita>) -> Unit) {
        getRefCitas().orderByChild("idCliente").equalTo(idCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historial = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    //ORDENAMOS POR HORA
                    onResult(historial.sortedByDescending { it.hora })
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }

    //FUNCIONES AUXILIARES
    private fun horaAMinutos(hora: String): Int {
        val partes = hora.split(":")
        return partes[0].toInt() * 60 + partes[1].toInt()
    }

    private fun extraerDuracion(servicio: String): Int {
        return when {
            servicio.contains("60") -> 60
            servicio.contains("45") -> 45
            servicio.contains("20") -> 20
            else -> 30
        }
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