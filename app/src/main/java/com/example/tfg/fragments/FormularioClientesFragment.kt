package com.example.tfg.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.R
import com.example.tfg.databinding.FragmentFormularioClientesBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class FormularioClientesFragment : Fragment(R.layout.fragment_formulario_clientes) {

    private lateinit var binding: FragmentFormularioClientesBinding
    private val mainRepository = MainRepository()
    private var clienteID: String? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFormularioClientesBinding.bind(view)

        clienteID = arguments?.getString("clienteId")

        //SI EXISTE EL ID, SE MUESTRA UN TITULO
        if (clienteID != null) {
            binding.tvTituloFormulario.text = getString(R.string.editarCliente)
            rellenarDatosParaEditar()
            //SI NO EXISTE EL ID, SE ENSEÑA OTRO TÍTULO
        } else {
            binding.tvTituloFormulario.text = getString(R.string.nuevoCliente)
        }

        //BOTÓN PAR VOLVER HACIA ATRÁS
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        //BOTÓN PARA GUARDAR EL CLIENTE EN LA BASE DE DATOS
        binding.btnGuardarCliente.setOnClickListener {
            guardarDatos()
        }
    }

    private fun rellenarDatosParaEditar() {
        //RELLENAMOS LOS DATOS EXISTENTES DE LA BASE DE DATOS PARA EDITARLOS
        binding.etNuevoNombre.setText(arguments?.getString("nombre"))
        binding.etNuevoTelefono.setText(arguments?.getString("telefono"))
        binding.etNuevoEmail.setText(arguments?.getString("email"))
        binding.etNuevoNotas.setText(arguments?.getString("notas"))

    }

    private fun guardarDatos() {
        val nombre = binding.etNuevoNombre.text.toString().trim()
        val telefono = binding.etNuevoTelefono.text.toString().trim()
        val email = binding.etNuevoEmail.text.toString().trim()
        val notas = binding.etNuevoNotas.text.toString().trim()

        // VALIDACIÓN BÁSICA
        if (nombre.isEmpty() || email.isEmpty() || telefono.length != 9) {
            Toast.makeText(
                requireContext(),
                "Revisa los campos (Nombre, Email y Teléfono)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nombreMayuscula = nombre.lowercase().replaceFirstChar { it.uppercase() }

        if (clienteID != null) {
            // LÓGICA DE EDICIÓN
            val datosActualizados = mapOf(
                "nombre" to nombreMayuscula,
                "telefono" to telefono,
                "email" to email,
                "notas" to notas
            )
            mainRepository.actualizarCliente(clienteID!!, datosActualizados) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Cliente actualizado", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
            }
        } else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456")
                .addOnCompleteListener { task ->
                    if (isAdded) {
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid ?: ""

                            val nuevoCliente = Cliente(
                                id = uid,
                                nombre = nombreMayuscula,
                                telefono = telefono,
                                email = email,
                                notas = notas
                            )

                            mainRepository.insertarCliente(nuevoCliente)
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)

                            Toast.makeText(
                                requireContext(),
                                "Cliente creado y aviso enviado",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigateUp()
                        } else {
                            val error = task.exception?.message ?: "Error al crear cuenta"
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        }
                    }
                }

            mainRepository.buscarClientePorTelefono(telefono) { clienteExistente ->
                if (clienteExistente != null){
                    Toast.makeText(requireContext(), "Error: Ya hay un cliente registrado con este teléfono", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456")
                        .addOnCompleteListener { task ->
                            if (isAdded) {
                                if(task.isSuccessful){
                                    val uid = task.result?.user?.uid ?: ""
                                    val nuevocliente = Cliente (
                                        id = uid,
                                        nombre = nombreMayuscula,
                                        telefono = telefono,
                                        email = email,
                                        notas = notas
                                    )
                                    mainRepository.insertarCliente(nuevocliente)
                                    //ESTO HACE QUE SE ENVIE UN CORREO PARA QUE CAMBIE LA CONTRASEÑA POR DEFECTO A LA CONTRASEÑA QUE QUIERA EL CLIENTE
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    Toast.makeText(requireContext(), "Cliente Creado", Toast.LENGTH_SHORT).show()
                                    findNavController().navigateUp()
                                } else {
                                    val error = task.exception?.message ?: "Error al crear la cuenta"
                                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
        }
    }
}