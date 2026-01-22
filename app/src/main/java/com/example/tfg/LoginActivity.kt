package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.databinding.LoginBinding
import com.example.tfg.viewModel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //CONFIGURAMOS LOS OBSERVERS
        setupObservers()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            //LLAMAMOS A LA LÓGICA DEL VIEWMODEL
            viewModel.entrar(email, pass)
        }

    }

    private fun setupObservers(){
        //OBSERVAMOS SI EL LOGIN ES CORRECTO
        viewModel.loginResult.observe(this){ success ->
            if(success){
                //SI ES CORRECTO NOS LLEVARÁ AL HOME
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()    //CERRAMOS LA ACTIVITY PARA QUE NO VUELVA ATRÁS
            }
        }
        //OBSERVAMOS SI HAY ERRORES
        viewModel.errorMessage.observe(this) { error ->
            if (error != null){
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart(){
        super.onStart()

        viewModel.sesionActiva.observe(this){ estaLogueado ->
            if(estaLogueado){
                irAMainActivity()
            }
        }

        viewModel.comprobarSesion()
    }

    private fun irAMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()    //CERRAMOS EL LOGIN PARA QUE NO SE PUEDA VOLVER ATRÁS
    }

}
