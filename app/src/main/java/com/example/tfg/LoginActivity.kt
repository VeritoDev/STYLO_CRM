package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
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

        setupObservers()

        // --- LÓGICA DE REGISTRO ---
        val textCompleto = getString(R.string.login_no_cuenta)
        val palabraResaltada = getString(R.string.link_registrate) // "Regístrate" o "Sign up"
        val spannable = SpannableString(textCompleto)

        // Buscamos dinámicamente dónde empieza y termina la palabra en el idioma actual
        val inicio = textCompleto.indexOf(palabraResaltada)
        val fin = inicio + palabraResaltada.length

        if (inicio != -1) { // Solo si encuentra la palabra (evita errores)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@LoginActivity, RegistroActivity::class.java)
                    startActivity(intent)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.marron_oscuro_fondo)
                    ds.isFakeBoldText = true
                }
            }
            spannable.setSpan(clickableSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvIrARegistro.text = spannable
        binding.tvIrARegistro.movementMethod = LinkMovementMethod.getInstance()

        // --- LÓGICA DE LOGIN ---
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            viewModel.entrar(email, pass)
        }

        // --- LÓGICA DE RECUPERAR CONTRASEÑA ---
        val textoOlvide = getString(R.string.recuperarCuenta)
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
        binding.tvOlvidePass.text = spannableOlvide
        binding.tvOlvidePass.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                val emailLogueado = viewModel.obtenerEmailUsuarioActual()
                dirigirSegunRol(emailLogueado ?: binding.etEmail.text.toString().trim())
            }
        }
    }

    private fun dirigirSegunRol(email: String?) {
        mainRepository.buscarClientePorEmail(email) { cliente ->
            val intent = if (cliente != null) {
                Intent(this, ClientesMisCitasActivity::class.java).apply {
                    putExtra("CLIENTE_ID", cliente.id)
                }
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}