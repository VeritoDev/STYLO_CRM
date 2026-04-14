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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val mainRepository = MainRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        // LÓGICA DE REGISTRO
        val textCompleto = getString(R.string.login_no_cuenta)
        val palabraResaltada = getString(R.string.link_registrate)
        val spannable = SpannableString(textCompleto)

        val inicio = textCompleto.indexOf(palabraResaltada)
        val fin = inicio + palabraResaltada.length

        if (inicio != -1) {
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

        // LÓGICA DE LOGIN
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim().lowercase()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()){
                Toast.makeText(this, getString(R.string.errorLoginCamposVacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signOut()

            binding.btnLogin.postDelayed({
                viewModel.entrar(email, pass)
            }, 250)
        }

        // LÓGICA DE RECUPERAR CONTRASEÑA
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
                val emailIngresado = binding.etEmail.text.toString().trim().lowercase()
                dirigirSegunRol(emailIngresado)
            } else {
                Toast.makeText(this, getString(R.string.error_sesion_invalida), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dirigirSegunRol(email: String?) {
        if (email.isNullOrEmpty()) return
        val emailSeguro = email.trim().lowercase()

        mainRepository.buscarEstilistaPorEmail(emailSeguro) { esEstilista ->
            if (esEstilista) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                mainRepository.buscarClientePorEmail(emailSeguro) { cliente ->
                    if (cliente != null) {
                        val intent = Intent(this, ClientesMisCitasActivity::class.java).apply {
                            putExtra("CLIENTE_ID", cliente.id)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(this, getString(R.string.error_sesion_invalida), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}