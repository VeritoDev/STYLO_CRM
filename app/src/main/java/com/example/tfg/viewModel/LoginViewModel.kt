package com.example.tfg.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfg.repository.MainRepository

class LoginViewModel: ViewModel() {
    private val mainRepository = MainRepository()
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    private val _sesionActiva = MutableLiveData<Boolean>()
    val sesionActiva: LiveData<Boolean> = _sesionActiva


    fun entrar(email: String, pass: String) {
        if(email.isEmpty() || pass.isEmpty()){
            _errorMessage.value = "@string/errorLoginCamposVacios"
            return
        }

        mainRepository.login(email, pass) { success, error ->
            if(success){
                _loginResult.value = true
            }else{
                _errorMessage.value = error ?: "@string/errorLogin"
            }
        }
    }
    fun comprobarSesion(){
        if(mainRepository.isUsuarioLogueado()){
            _sesionActiva.value = true
        }
    }
}