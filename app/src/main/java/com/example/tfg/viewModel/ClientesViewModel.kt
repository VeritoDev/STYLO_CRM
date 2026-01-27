package com.example.tfg.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository

class ClientesViewModel {
    private val mainRepository = MainRepository()

    //LISTA ORIGINAL EN LA FIREBASE
    private var listaCompleta = listOf<Cliente>()

    //LISTA QUE OBSERVA EL FRAGMENT
    private val _clientesMostrados = MutableLiveData<List<Cliente>>()
    val clientesMostrados: LiveData<List<Cliente>> = _clientesMostrados

    init {
        cargarClientes()
    }

    private fun cargarClientes() {
        mainRepository.getClientes { lista ->
            listaCompleta = lista
            _clientesMostrados.value = lista
        }
    }


    // LÓGICA PARA EL BOTÓN ELIMINAR
    fun eliminarCliente(id: String) {
        mainRepository.eliminarCliente(id)
        // Firebase actualizará la lista automáticamente gracias al Listener
    }
}