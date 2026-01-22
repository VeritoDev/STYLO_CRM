package com.example.tfg.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tfg.model.Item

class ViewModel {

    //USAMOS MUTABLELIVEDATA PARA QUE EL FRAGMENT PUEDA OBSERVAR LOS CAMBIOS
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    init {
        cargarDatosPrueba()
    }

    private fun cargarDatosPrueba() {
        //AQUÍ SIMULAMOS LOS DATOS QUE LUEGO VENDRAN DE LA BASE DE DATOS
        val lista = listOf(
            Item("PRÓXIMAS CITAS", isCita = true),
            Item("RESUMEN DIARIO", "Has atendido a 12 clientes hoy"),
            Item("ESTADÍSTICAS", "Crecimiento del 5% este mes")
        )
        _items.value = lista
    }


}