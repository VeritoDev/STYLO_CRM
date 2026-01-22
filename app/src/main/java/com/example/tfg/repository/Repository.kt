package com.example.tfg.repository

import com.example.tfg.model.Cita
import com.google.firebase.Firebase
import com.google.firebase.database.database

class Repository() {
    //REFERENCIA A LA BASE DE DATOS
    private val database = Firebase.database.getReference("citas")

    //FUNCIÓN PARA OBTENER CITAS QUE LUEGO USARÁ EL VIEWMODEL
    fun getCitasHoy(): List<Cita>{
        //LÓGICA PARA BUSCAR LOS DATOS
        return database.getCitas()
    }

    fun insertarCita(cita: Cita){
        //push() CREA UN ID ÚNICO PARA QUE NO SE SOBREESCRIBAN
        val key = database.push().key
        if(key != null){
            database.child(key).setValue(cita)
        }

    }
}