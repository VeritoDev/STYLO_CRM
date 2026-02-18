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
import com.example.tfg.fragments.RecuperarPassFragment
import com.example.tfg.databinding.LoginBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.viewModel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val mainRepository = MainRepository()


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

        val colorResaltado = ContextCompat.getColor(this, R.color.marron_oscuro_fondo)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
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
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()

        //LÓGICA DE LOGIN
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            //LLAMAMOS A LA LÓGICA DEL VIEWMODEL
            viewModel.entrar(email, pass)
        }

        // LÓGICA DE RECUPERAR CONTRASEÑA
        val tvOlvide = binding.tvOlvidePass
        val textoOlvide = "¿Has olvidado tu contraseña?"
        val spannableOlvide = SpannableString(textoOlvide)

        val clickableOlvide = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val dialogo = RecuperarPassFragment()
                dialogo.show(supportFragmentManager, "Recuperar")
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.marron_oscuro_fondo)
                ds.isFakeBoldText = true
            }
        }

        spannableOlvide.setSpan(clickableOlvide, 0, textoOlvide.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvOlvide.text = spannableOlvide
        tvOlvide.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun setupObservers(){
        //OBSERVAMOS SI EL LOGIN ES CORRECTO
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                val emailLogueado = binding.etEmail.text.toString().trim()

                mainRepository.buscarClientePorEmail(emailLogueado) { cliente ->
                    if (cliente != null) {
                        val intent = Intent(this, ClienteReservasActivity::class.java)
                        intent.putExtra("CLIENTE_ID", cliente.id)
                        startActivity(intent)
                    } else {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
            }
        }
        viewModel.errorMessage.observe(this){ error ->
            if(error!= null){
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart(){
        super.onStart()

        viewModel.sesionActiva.observe(this){ estaLogueado ->
            if(estaLogueado){
                val emailActual = viewModel.obtenerEmailUsuarioActual()
                dirigirSegunRol(emailActual)
            }
        }
        viewModel.comprobarSesion()
    }

    private fun dirigirSegunRol(email: String?){
        if (email == null) return

        mainRepository.buscarClientePorEmail(email) { cliente ->
            if(cliente != null){
                val intent = Intent(this, ClienteReservasActivity::class.java)
                intent.putExtra("CLIENTE_ID", cliente.id)
                startActivity(intent)
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }

    }

}
