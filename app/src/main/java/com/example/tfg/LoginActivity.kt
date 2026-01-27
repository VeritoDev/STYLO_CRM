package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        //LÓGICA DE REGISTRO
        val textView = binding.tvIrARegistro
        val textCompleto = "¿No tienes cuenta? Regístrate"
        val spannable = SpannableString(textCompleto)

        val inicio = textCompleto.indexOf("Regístrate")
        val fin = inicio + "Regístrate".length

        val colorResaltado = ContextCompat.getColor(this, R.color.blanco_puro)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navegamos al registro
                val intent = Intent(this@LoginActivity, RegistroActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = colorResaltado
                ds.isFakeBoldText = true
            }
        }

        spannable.setSpan(clickableSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView?.text = spannable
        textView?.movementMethod = LinkMovementMethod.getInstance()


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
