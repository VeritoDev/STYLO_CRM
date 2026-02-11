package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.text.LinkAnnotation
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.tfg.databinding.ActivityMainBinding
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
                        abrirDialogoCambiarPassword()
                        true
                    }
                    R.id.menu_logout -> {
                        cerrarSesion()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
    private fun abrirDialogoCambiarPassword() {
        Toast.makeText(this, "Función para cambiar contraseña", Toast.LENGTH_SHORT).show()
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
}

