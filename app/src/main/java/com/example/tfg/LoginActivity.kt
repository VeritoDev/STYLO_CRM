package com.example.tfg

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.tfg.fragments.RecuperarPassFragment
import com.example.tfg.databinding.LoginBinding
import com.example.tfg.repository.MainRepository
import com.example.tfg.repository.capitalizarFormato
import com.example.tfg.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private val mainRepository = MainRepository()
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {

        cargarIdiomaPersistente()
        setupBiometria()
        verificarYMostrarHuella()

        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        actualizarIconoTema()

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

            var esValido = true

            if (email.isEmpty()) {
                binding.tilEmail?.error = " "
                esValido = false
            } else {
                binding.tilEmail?.error = null
            }

            if (pass.isEmpty()) {
                binding.tilContraseA?.error = " "
                esValido = false
            } else {
                binding.tilContraseA?.error = null
            }

            if (!esValido) {
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
                val emailEscrito = binding.etEmail.text.toString().trim()
                val dialogo = RecuperarPassFragment().apply {
                    arguments = Bundle().apply {
                        putString("EMAIL_PREVIO", emailEscrito)
                    }
                }
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

        binding.iconIdioma?.setOnClickListener {
            mostrarDialogoIdioma()
        }

        binding.btnThemeLogin?.setOnClickListener {
            binding.btnThemeLogin!!.animate()
                .rotationBy(360f)
                .setDuration(400)
                .withEndAction {
                    val modoActual = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val nuevoModo = if (modoActual == Configuration.UI_MODE_NIGHT_YES) {
                        AppCompatDelegate.MODE_NIGHT_NO
                    } else {
                        AppCompatDelegate.MODE_NIGHT_YES
                    }

                    val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
                    prefs.edit().putBoolean("modo_oscuro", nuevoModo == AppCompatDelegate.MODE_NIGHT_YES).apply()

                    AppCompatDelegate.setDefaultNightMode(nuevoModo)
                    actualizarIconoTema()
                }
                .start()
        }
    }

    private fun actualizarIconoTema() {
        val modoActual = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(modoActual == Configuration.UI_MODE_NIGHT_YES) {
            binding.btnThemeLogin?.setImageResource(R.drawable.icon_dark_mode)
        } else {
            binding.btnThemeLogin?.setImageResource(R.drawable.icon_light_mode)
        }
    }

    private fun cargarIdiomaPersistente() {
        val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
        val lang = prefs.getString("idioma_pref", "es") ?: "es"
        configurarLocale(lang)
    }

    private fun configurarLocale(codigo: String) {
        val locale = Locale(codigo)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun mostrarDialogoIdioma() {
        val dialog = Dialog(this)
        dialog.window?.attributes?.windowAnimations = R.style.MaterialAlertDialog_Material3_Animation_
        dialog.show()

        val view = layoutInflater.inflate(R.layout.fragment_idioma, null)
        dialog.setContentView(view)

        view.findViewById<Button>(R.id.btnEspañol).setOnClickListener {
            aplicarIdioma("es")
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnIngles).setOnClickListener {
            aplicarIdioma("en")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun aplicarIdioma(codigo: String) {
        val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
        prefs.edit().putString("idioma_pref", codigo).apply()

        val locale = Locale(codigo)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                val email = binding.etEmail.text.toString().trim().lowercase()
                dirigirSegunRol(email)
            }
        }

        viewModel.errorMessage.observe(this) { mensajeResId ->
            Toast.makeText(this, getString(mensajeResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun dirigirSegunRol(email: String?) {
        if (email.isNullOrEmpty()) return
        val emailSeguro = email.trim().lowercase()

        mainRepository.obtenerDatosEstilista(emailSeguro) { estilista ->
            if (estilista != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("NOMBRE_USUARIO", estilista.nombre.capitalizarFormato())
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                mainRepository.buscarClientePorEmail(emailSeguro) { cliente ->
                    if (cliente != null) {
                        val intent = Intent(this, ClientesMisCitasActivity::class.java).apply {
                            putExtra("CLIENTE_ID", cliente.id)
                            putExtra("NOMBRE_USUARIO", cliente.nombre.capitalizarFormato())
                            putExtra("BIENVENIDA_MOSTRADA", false)
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

    private fun setupBiometria() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
                val email = prefs.getString("user_email", "") ?: ""
                val pass = prefs.getString("user_pass", "") ?: ""

                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    binding.etEmail.setText(email)
                    binding.etPassword.setText(pass)

                    // LLAMAMOS AL LOGIN
                    viewModel.entrar(email, pass)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_biometrico_titulo))
            .setSubtitle(getString(R.string.login_biometrico_subtitulo))
            .setNegativeButtonText(getString(R.string.cancelar))
            .build()
    }

    private fun verificarYMostrarHuella() {
        val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
        if (prefs.contains("user_email") && prefs.contains("user_pass")) {

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    biometricPrompt.authenticate(promptInfo)
                }
            }, 500)
        }
    }
}