package com.example.tfg.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfg.R
import com.example.tfg.repository.MainRepository
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel: ViewModel() {
    private val mainRepository = MainRepository()
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult
    private val _errorMessage = MutableLiveData<Int>()
    val errorMessage: LiveData<Int> = _errorMessage
    private val _sesionActiva = MutableLiveData<Boolean>()
    val sesionActiva: LiveData<Boolean> = _sesionActiva


    fun entrar(email: String, pass: String) {
        if(email.isEmpty() || pass.isEmpty()){
            _errorMessage.value = R.string.errorLoginCamposVacios
            return
        }

        mainRepository.login(email, pass) { success, _ ->
            if(success){
                _loginResult.value = true
            }else{
                _errorMessage.value = R.string.error_sesion_invalida
            }
        }
    }
}