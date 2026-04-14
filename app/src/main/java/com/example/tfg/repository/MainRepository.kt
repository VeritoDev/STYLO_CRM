package com.example.tfg.repository

import android.util.Log
import androidx.compose.animation.core.snap
import com.example.tfg.model.Cita
import com.example.tfg.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {

    // -- REFERENCIAS A LA BASE DE DATOS --
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // --- REFERENCIAS DINÁMICAS ---
    private fun getRefClientes() = database.child("clientes")
    private fun getRefCitas() = database.child("citas")
    private fun getRefEstilistas() = database.child("estilistas")

    // --- LÓGICA DE ESTILISTAS ---

    //BÚSCAR ESTILISTA POR EMAIL
    fun buscarEstilistaPorEmail(email: String?, callback: (Boolean) -> Unit) {
        if (email == null){
            callback(false)
            return
        }
        val emailLimpio = email.trim().lowercase()

        getRefEstilistas().orderByChild("email").equalTo(emailLimpio).get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()){
                    callback(true)
                } else {
                    Log.e("DEBUG_ERROR", "No se encontro estilista con ese email: $emailLimpio")
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun obtenerNombresEstilistas(callback: (List<String>) -> Unit){
        getRefEstilistas().get().addOnSuccessListener { snapshot  ->
            val nombres = mutableListOf<String>()
            if(snapshot.exists()){
                for(data in snapshot.children){
                    val nombre = data.child("nombre").value.toString()
                    if(nombre != "null") nombres.add(nombre)
                }
            }
            callback(nombres)
        }.addOnFailureListener {
            callback(emptyList())
        }
    }
    fun getCitasPorEstilista(nombreEstilista: String, onResult: (List<Cita>) -> Unit) {
        val nombreLimpio = nombreEstilista.trim()

        getRefCitas()
            .orderByChild("estilista")
            .equalTo(nombreLimpio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    onResult(lista.sortedWith(compareBy({ it.fecha }, { it.hora })))
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }
    fun getNombreEstilistaPorUID(uid: String, callback: (String?) -> Unit) {
        // IMPORTANTE: Asegúrate de que en Firebase la carpeta se llame "estilistas"
        // y el campo dentro se llame "nombre"
        FirebaseDatabase.getInstance().getReference("estilistas").child(uid).child("nombre").get()
            .addOnSuccessListener { snapshot ->
                callback(snapshot.value?.toString())
            }
            .addOnFailureListener {
                callback(null)
            }
    }
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

    fun insertarCliente(cliente: Cliente) {
        val id = cliente.id
        val clienteNormalizado = cliente.copy(
            id = id,
            nombre = cliente.nombre.lowercase().trim(),
            email = cliente.email.trim().lowercase()
        )
        getRefClientes().child(id).setValue(clienteNormalizado)
            .addOnSuccessListener {
            }
    }

    fun buscarClientePorEmail(email: String?, callback: (Cliente?) -> Unit) {
        if (email == null) {
            callback(null)
            return
        }
        val emailLimpio = email.trim().lowercase()
        getRefClientes().orderByChild("email").equalTo(emailLimpio).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val data = snapshot.children.firstOrNull()
                    val cliente = data?.getValue(Cliente::class.java)
                    cliente?.id = data.key ?: ""
                    callback(cliente)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun buscarClientePorTelefono(telefono: String, callback: (Cliente?) -> Unit) {
        // Buscamos en la carpeta 'clientes' filtrando por el campo 'telefono'
        getRefClientes()
            .orderByChild("telefono")
            .equalTo(telefono.trim()) // Usamos trim() para evitar errores de espacios
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Cogemos el primer resultado que coincida
                        val data = snapshot.children.first()
                        val cliente = data.getValue(Cliente::class.java)
                        cliente?.id = data.key ?: ""
                        callback(cliente)
                    } else {
                        // Si no hay ningún cliente con ese teléfono
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun eliminarClienteYSusCitas(clienteId: String, callback: (Boolean) -> Unit) {
        getRefClientes().child(clienteId).equalTo(clienteId).get()
            .addOnSuccessListener { snapshot ->
                val rutasABorrar = mutableMapOf<String, Any?>()
                //Ruta del cliente en el mapa de borrado
                rutasABorrar["/clientes/$clienteId"] = null

                if (snapshot.exists()) {
                    for (citaSnapshot in snapshot.children){
                        val citaId = citaSnapshot.key
                        if (citaId != null){
                            rutasABorrar["/citas/$citaId"] = null
                        }
                    }
                }

                //Ejecutamos todas las eliminaciones en una sola operación de red
                database.updateChildren(rutasABorrar)
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
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
    // BUSCAR UNA CITA ESPECÍFICA POR SU ID (Para cargar los datos al editar)
    fun getCitaPorId(id: String, callback: (Cita?) -> Unit) {
        getRefCitas().child(id).get().addOnSuccessListener { snapshot ->
            val cita = snapshot.getValue(Cita::class.java)
            callback(cita)
        }.addOnFailureListener {
            callback(null)
        }
    }

    // ACTUALIZAR UNA CITA EXISTENTE
    fun actualizarCita(cita: Cita, callback: (Boolean) -> Unit) {
        getRefCitas().child(cita.id).setValue(cita)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
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
    //LÓGICA PARA TERMINAR LA CITA
    fun finalizarCita(citaId: String, callback: (Boolean) -> Unit){
        val actualizaciones = mapOf("estado" to "finalizado")

        getRefCitas().child(citaId).updateChildren(actualizaciones)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
    //LÓGICA PARA OBTENER SOLO CITAS PENDIENTES PARA EL CLIENTE
    fun getCitasActivasCliente(idCliente: String, onResult: (List<Cita>) -> Unit){
        getRefCitas().orderByChild("idCliente").equalTo(idCliente)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    val pendientes = lista.filter { it.estado != "finalizado" }
                    onResult(pendientes.sortedWith (compareBy({it.fecha}, {it.hora})))
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