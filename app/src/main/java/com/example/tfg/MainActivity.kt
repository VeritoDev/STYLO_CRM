package com.example.tfg

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.graphics.RenderEffect
import androidx.navigation.fragment.NavHostFragment
import com.example.tfg.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // APLICACIÓN DEL IDIOMA
        val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
        val lang = prefs.getString("idioma_pref", "es") ?: "es"

        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // GESTIÓN DEL NOMBRE DE USUARIO
        val nombreDesdeRegistro = intent.getStringExtra("NOMBRE_USUARIO")

        val nombreFinal = if (!nombreDesdeRegistro.isNullOrEmpty()) {
            prefs.edit {
                putString("user_name_key", nombreDesdeRegistro)
            }
            nombreDesdeRegistro
        } else {
            prefs.getString("user_name_key", "User") ?: "User"
        }

        if (savedInstanceState == null) {
            Toast.makeText(this, getString(R.string.bienvenida_nombre, nombreFinal), Toast.LENGTH_SHORT).show()
        }

        // NAVEGACIÓN
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            aplicarEfectoBlur()
        }

        actualizarIconoTema()

        // CAMBIO DE TEMA (MODO OSCURO/CLARO)
        binding.btnThemeToolbar.setOnClickListener {
            binding.btnThemeToolbar.animate()
                .rotationBy(360f)
                .setDuration(400)
                .withEndAction {
                    val modoActual = resources.configuration.uiMode and UI_MODE_NIGHT_MASK
                    if (modoActual == UI_MODE_NIGHT_YES) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                .start()
        }

        // MENÚ DE USUARIO
        binding.btnLoginToolbar.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_usuario, popup.menu)

            val logoutItem = popup.menu.findItem(R.id.menu_logout)
            val spannable = SpannableString(logoutItem.title)
            spannable.setSpan(
                ForegroundColorSpan(Color.RED), 0, spannable.length, 0
            )
            logoutItem.title = spannable

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_logout -> {
                        cerrarSesion()
                        true
                    }
                    R.id.menu_cambiar_idioma -> {
                        mostrarDialogoIdiomas()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun aplicarEfectoBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP)

            binding.navHostFragment.setRenderEffect(blurEffect)

            binding.navHostFragment.postDelayed({
                binding.navHostFragment.setRenderEffect(null)
            }, 400)
        }
    }

    private fun cerrarSesion() {
       FirebaseAuth.getInstance().signOut()

        getSharedPreferences("config_app", MODE_PRIVATE).edit { clear() }

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun actualizarIconoTema() {
        val modoActual = resources.configuration.uiMode and UI_MODE_NIGHT_MASK

        if (modoActual == UI_MODE_NIGHT_YES) {
            binding.btnThemeToolbar.setImageResource(R.drawable.icon_light_mode)
        } else {
            binding.btnThemeToolbar.setImageResource(R.drawable.icon_dark_mode)
        }
    }

    private fun mostrarDialogoIdiomas() {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.fragment_idioma, null)
        dialog.setContentView(view)

        view.findViewById<Button>(R.id.btnEspañol).setOnClickListener {
            guardarYAplicarIdioma("es")
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnIngles).setOnClickListener {
            guardarYAplicarIdioma("en")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarYAplicarIdioma(codigo: String) {
        val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
        prefs.edit { putString("idioma_pref", codigo) }
        aplicarIdioma(codigo)
    }

    private fun aplicarIdioma(codigo: String){
        val locale = Locale(codigo)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}