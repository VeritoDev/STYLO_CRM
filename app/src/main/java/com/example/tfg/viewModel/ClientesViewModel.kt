package com.example.tfg.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tfg.model.Cliente
import com.example.tfg.repository.Repository

class ClientesViewModel {
    private val repository = Repository()

    //LISTA ORIGINAL EN LA FIREBASE
    private var listaCompleta = listOf<Cliente>()

    //LISTA QUE OBSERVA EL FRAGMENT
    private val _clientesMostrados = MutableLiveData<List<Cliente>>()
    val clientesMostrados: LiveData<List<Cliente>> = _clientesMostrados

    init {
        cargarClientes()
    }

    private fun cargarClientes() {
        repository.getClientes { lista ->
            listaCompleta = lista
            _clientesMostrados.value = lista
        }
    }


    // LÓGICA PARA EL BOTÓN ELIMINAR
    fun eliminarCliente(id: String) {
        repository.eliminarCliente(id)
        // Firebase actualizará la lista automáticamente gracias al Listener
    }
}