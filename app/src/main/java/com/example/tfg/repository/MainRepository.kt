package com.example.tfg.repository

import android.util.Log
import com.example.tfg.model.Cita
import com.example.tfg.model.Cliente
import com.example.tfg.model.Estilista
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    // --- LÓGICA DE ESTILISTAS ---

    fun actualizarDatosEstilista(
        id: String,
        emailActual: String,
        passActual: String,
        nuevoNombre: String,
        nuevoEmail: String,
        nuevaPass: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null || id.isEmpty()) {
            onResult(false, "Error: Sesión inválida")
            return
        }

        // 1. REAUTENTICAR (Obligatorio para cambiar email o pass)
        val credential = EmailAuthProvider.getCredential(emailActual, passActual)
        user.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {

                // 2. ¿HAY QUE CAMBIAR EL EMAIL EN AUTH?
                if (emailActual != nuevoEmail) {
                    user.updateEmail(nuevoEmail).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            // Email cambiado en Auth, ahora vamos a la base de datos
                            ejecutarCambiosEnBD(id, user, nuevoNombre, nuevoEmail, nuevaPass, onResult)
                        } else {
                            // Error común: El email ya está en uso por otro usuario
                            val msg = emailTask.exception?.message ?: "Error al cambiar el correo"
                            onResult(false, msg)
                        }
                    }
                } else {
                    // El email es el mismo, solo actualizamos el resto
                    ejecutarCambiosEnBD(id, user, nuevoNombre, nuevoEmail, nuevaPass, onResult)
                }
            } else {
                onResult(false, "La contraseña actual es incorrecta")
            }
        }
    }

    private fun ejecutarCambiosEnBD(
        id: String,
        user: FirebaseUser,
        nuevoNombre: String,
        nuevoEmail: String,
        nuevaPass: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        // 3. Cambiar contraseña si el usuario escribió una nueva
        if (!nuevaPass.isNullOrEmpty()) {
            user.updatePassword(nuevaPass)
        }

        // 4. Recuperamos el nombre viejo para no romper las citas (como hicimos antes)
        getRefEstilistas().child(id).child("nombre").get().addOnSuccessListener { snapshot ->
            val nombreAntiguo = snapshot.value?.toString() ?: ""

            val updates = mapOf(
                "nombre" to nuevoNombre.trim(),
                "email" to nuevoEmail.lowercase().trim()
            )

            // 5. Actualizar los datos en el nodo "estilistas"
            getRefEstilistas().child(id).updateChildren(updates).addOnSuccessListener {

                // 6. Si el nombre cambió, actualizamos las citas para no perderlas
                if (nombreAntiguo.isNotEmpty() && nombreAntiguo != nuevoNombre.trim()) {
                    actualizarNombreEnCitas(nombreAntiguo, nuevoNombre.trim()) {
                        onResult(true, "Perfil y correo actualizados correctamente")
                    }
                } else {
                    onResult(true, "Perfil actualizado correctamente")
                }
            }.addOnFailureListener {
                onResult(false, "Error al guardar en la base de datos")
            }
        }
    }

    private fun actualizarNombreEnCitas(
        nombreViejo: String,
        nombreNuevo: String,
        callback: () -> Unit
    ) {
        getRefCitas().orderByChild("estilista").equalTo(nombreViejo).get()
            .addOnSuccessListener { snapshot ->
                val updatesCitas = mutableMapOf<String, Any?>()
                for (citaSnapshot in snapshot.children) {
                    val citaId = citaSnapshot.key
                    if (citaId != null) {
                        updatesCitas["/$citaId/estilista"] = nombreNuevo
                    }
                }
                if (updatesCitas.isNotEmpty()) {
                    getRefCitas().updateChildren(updatesCitas)
                        .addOnCompleteListener { callback() }
                } else {
                    callback()
                }
            }
            .addOnFailureListener { callback() }
    }

    fun buscarEstilistaPorEmail(email: String?, callback: (Boolean) -> Unit) {
        if (email == null) {
            callback(false)
            return
        }
        val emailLimpio = email.trim().lowercase()

        getRefEstilistas().orderByChild("email").equalTo(emailLimpio).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    callback(true)
                } else {
                    Log.e("DEBUG_ERROR", "No se encontro estilista con ese email: $emailLimpio")
                    callback(false)
                }
            }
            .addOnFailureListener { callback(false) }
    }

    fun obtenerNombresEstilistas(callback: (List<String>) -> Unit) {
        getRefEstilistas().get().addOnSuccessListener { snapshot ->
            val nombres = mutableListOf<String>()
            if (snapshot.exists()) {
                for (data in snapshot.children) {
                    val nombre = data.child("nombre").getValue(String::class.java)
                    if (!nombre.isNullOrEmpty()) {
                        nombres.add(nombre.capitalizarFormato())
                    }
                }
            }
            callback(nombres)
        }.addOnFailureListener { callback(emptyList()) }
    }

    fun obtenerDatosEstilista(email: String, callback: (Estilista?) -> Unit) {
        database.child("estilistas").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val snapshotHijo = snapshot.children.firstOrNull()
                    val estilista = snapshotHijo?.getValue(Estilista::class.java)
                    if (estilista != null && snapshotHijo != null) {
                        estilista.id = snapshotHijo.key ?: ""
                    }
                    callback(estilista)
                }
                override fun onCancelled(error: DatabaseError) { callback(null) }
            })
    }

    fun getCitasPorEstilista(nombreEstilista: String, onResult: (List<Cita>) -> Unit) {
        val nombreLimpio = nombreEstilista.trim()
        getRefCitas().orderByChild("estilista").equalTo(nombreLimpio)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    onResult(lista.sortedWith(compareBy({ it.fecha }, { it.hora })))
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun getNombreEstilistaPorUID(uid: String, callback: (String?) -> Unit) {
        getRefEstilistas().child(uid).child("nombre").get()
            .addOnSuccessListener { snapshot -> callback(snapshot.value?.toString()) }
            .addOnFailureListener { callback(null) }
    }

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
            override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
        })
    }

    fun obtenerDetalleCliente(id: String, callback: (Cliente?) -> Unit) {
        getRefClientes().child(id).get().addOnSuccessListener { snapshot ->
            val cliente = snapshot.getValue(Cliente::class.java)
            callback(cliente)
        }.addOnFailureListener { callback(null) }
    }

    fun insertarCliente(cliente: Cliente) {
        val id = cliente.id
        val clienteNormalizado = cliente.copy(
            id = id,
            nombre = cliente.nombre.lowercase().trim(),
            email = cliente.email.trim().lowercase()
        )
        getRefClientes().child(id).setValue(clienteNormalizado)
    }

    fun buscarClientePorEmail(email: String?, callback: (Cliente?) -> Unit) {
        if (email == null) { callback(null); return }
        val emailLimpio = email.trim().lowercase()
        getRefClientes().orderByChild("email").equalTo(emailLimpio).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val data = snapshot.children.firstOrNull()
                    val cliente = data?.getValue(Cliente::class.java)
                    cliente?.id = data.key ?: ""
                    callback(cliente)
                } else { callback(null) }
            }.addOnFailureListener { callback(null) }
    }

    fun buscarClientePorTelefono(telefono: String, callback: (Cliente?) -> Unit) {
        getRefClientes().orderByChild("telefono").equalTo(telefono.trim())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.children.first()
                        val cliente = data.getValue(Cliente::class.java)
                        cliente?.id = data.key ?: ""
                        callback(cliente)
                    } else { callback(null) }
                }
                override fun onCancelled(error: DatabaseError) { callback(null) }
            })
    }

    fun eliminarClienteYSusCitas(clienteId: String, callback: (Boolean) -> Unit) {
        getRefCitas().orderByChild("idCliente").equalTo(clienteId).get()
            .addOnSuccessListener { snapshot ->
                val rutasABorrar = mutableMapOf<String, Any?>()
                rutasABorrar["/clientes/$clienteId"] = null
                if (snapshot.exists()) {
                    for (citaSnapshot in snapshot.children) {
                        val citaId = citaSnapshot.key
                        if (citaId != null) { rutasABorrar["/citas/$citaId"] = null }
                    }
                }
                database.updateChildren(rutasABorrar)
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }.addOnFailureListener { callback(false) }
    }

    fun actualizarCliente(id: String, datos: Map<String, Any>, callback: (Boolean) -> Unit) {
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
    fun getCitaPorId(id: String, callback: (Cita?) -> Unit) {
        getRefCitas().child(id).get().addOnSuccessListener { snapshot ->
            val cita = snapshot.getValue(Cita::class.java)
            callback(cita)
        }.addOnFailureListener { callback(null) }
    }

    fun actualizarCita(cita: Cita, callback: (Boolean) -> Unit) {
        getRefCitas().child(cita.id).setValue(cita)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun eliminarCita(citaId: String, callback: (Boolean) -> Unit) {
        getRefCitas().child(citaId).removeValue()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

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
        } else { callback(false) }
    }

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

    fun getHistorialCitasCliente(idCliente: String, onResult: (List<Cita>) -> Unit) {
        getRefCitas().orderByChild("idCliente").equalTo(idCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val historial = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    onResult(historial.sortedByDescending { it.hora })
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun finalizarCita(citaId: String, callback: (Boolean) -> Unit) {
        getRefCitas().child(citaId).child("estado").setValue("finalizado")
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getCitasActivasCliente(idCliente: String, onResult: (List<Cita>) -> Unit) {
        getRefCitas().orderByChild("idCliente").equalTo(idCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = snapshot.children.mapNotNull { it.getValue(Cita::class.java) }
                    val pendientes = lista.filter { it.estado != "finalizado" }
                    onResult(pendientes.sortedWith(compareBy({ it.fecha }, { it.hora })))
                }
                override fun onCancelled(error: DatabaseError) { onResult(emptyList()) }
            })
    }

    fun verificarCitaMismoDiaCliente(idCliente: String, fecha: String, citaIdIgnorar: String?, onResult: (Boolean) -> Unit) {
        getRefCitas().orderByChild("idCliente").equalTo(idCliente)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var yaTieneCita = false
                    for (data in snapshot.children) {
                        val cita = data.getValue(Cita::class.java)
                        if (cita != null && cita.fecha == fecha && cita.id != citaIdIgnorar) {
                            if (cita.estado != "finalizado" && cita.estado != "cancelado") {
                                yaTieneCita = true; break
                            }
                        }
                    }
                    onResult(yaTieneCita)
                }
                override fun onCancelled(error: DatabaseError) { onResult(false) }
            })
    }

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

// --- FUNCIONES DE EXTENSIÓN Y AUXILIARES ---
fun String.capitalizarFormato(): String {
    return this.lowercase().trim().split(" ").joinToString(" ") { palabra ->
        palabra.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

fun formatearTelefono(tel: String): String {
    val limpio = tel.replace(" ", "")
    return if (limpio.length >= 12) {
        val prefijo = limpio.substring(0, 3)
        val p1 = limpio.substring(3, 5)
        val p2 = limpio.substring(5, 7)
        val p3 = limpio.substring(7, 9)
        "$prefijo $p1 $p2 $p3"
    } else { limpio }
}