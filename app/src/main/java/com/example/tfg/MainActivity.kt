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

        val textView = findViewById<TextView>(R.id.tvIrARegistro)
        val textCompleto = "¿No tienes cuenta? Login"

        //LÓGICA DE FORMULARIO PARA REGISTRARSE
        //CREAMOS EL OBJETO SPANNABLE
        val sp = SpannableString(textCompleto)

        //ENCONTRAMOS LOS ÍNDICES DE LA PALABRA REGISTRATE
        val inicio = textCompleto.indexOf("Login")
        val fin = inicio + "Login".length

        //EXTRAEMOS LOS COLORES DEL TEMA ACTUAL
        val colorTextoBase = obtenerColorDeAttr(com.google.android.material.R.attr.colorOnSurface)
        val colorResaltado = obtenerColorDeAttr(com.google.android.material.R.attr.colorSurface)

        val textoCompleto = "¿No tienes cuenta? Regístrate"
        val spannable = SpannableString(textoCompleto)

        //APLICAMOS EL COLOR QUE CAMBIA CON EL TEMA AL TEXTO
        spannable.setSpan(
            ForegroundColorSpan(colorTextoBase),
            0,
            inicio,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        //APLICAMOS EL COLOR DE MARCA DE REGISTRATE
        spannable.setSpan(
            ForegroundColorSpan(colorResaltado),
            inicio,
            fin,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@MainActivity, RegistroActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = colorResaltado
            }
        }

        spannable.setSpan(clickableSpan, inicio, fin, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()


        //CONECTAMOS EL BOTTOMNAVIGATION CON EL NAVCONTROLLER
        binding.bottomNavigation.setupWithNavController(navController)

        binding.btnLoginToolbar.setOnClickListener {
            // 1. Cerramos sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Avisamos al usuario
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            // 3. Volvemos al Login y cerramos esta pantalla
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun obtenerColorDeAttr(attr: Int): Int{
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}

