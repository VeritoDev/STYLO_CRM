package com.example.tfg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.example.tfg.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.example.tfg.fragments.RecuperarPassFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("config_app", Context.MODE_PRIVATE)
        val lang = prefs.getString("idioma_key", "es") ?: "es"

        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //CONFIGURAMOS EL NAVCONTROLLER
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        actualizarIconoTema()

        binding.btnThemeToolbar.setOnClickListener {
            binding.btnThemeToolbar.animate()
                .rotationBy(360f)
                .setDuration(400)
                .withEndAction {
                    val modoActual = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

                    if (modoActual == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
                .start()
        }
        //CONECTAMOS EL BOTTOMNAVIGATION CON EL NAVCONTROLLER
        binding.bottomNavigation.setupWithNavController(navController)

        binding.btnLoginToolbar.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.menu_usuario, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_cambiar_pass -> {
                        abrirDialogoRecuperar()
                        true
                    }
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
    private fun abrirDialogoRecuperar() {
        val dialogo = RecuperarPassFragment()
        dialogo.show(supportFragmentManager, "Recuperar")
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, getString(R.string.cerrar_sesion), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun actualizarIconoTema(){
        val modoActual = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        if(modoActual == android.content.res.Configuration.UI_MODE_NIGHT_YES){
            binding.btnThemeToolbar.setImageResource(R.drawable.icon_light_mode)
        } else {
            binding.btnThemeToolbar.setImageResource(R.drawable.icon_dark_mode)
        }
    }

    private fun mostrarDialogoIdiomas(){
        val idiomas = arrayOf("Español", "English")
        val codigos = arrayOf("es", "en")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.selecciona_idioma))
        builder.setItems(idiomas) { _, which ->
            val selectedLang = codigos[which]

            val prefs = getSharedPreferences("config_app", Context.MODE_PRIVATE)
            prefs.edit { putString("idioma_key", selectedLang) }

            aplicarIdioma(selectedLang)
        }
        builder.show()
    }

    private fun aplicarIdioma(codigo: String){
        val locale = java.util.Locale(codigo)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

