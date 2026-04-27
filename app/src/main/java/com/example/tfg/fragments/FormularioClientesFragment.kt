package com.example.tfg.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tfg.R
import com.example.tfg.databinding.FragmentFormularioClientesBinding
import com.example.tfg.model.Cliente
import com.example.tfg.repository.MainRepository
import com.google.firebase.auth.FirebaseAuth

class FormularioClientesFragment : Fragment(R.layout.fragment_formulario_clientes) {

    private lateinit var binding: FragmentFormularioClientesBinding
    private val mainRepository = MainRepository()
    private var clienteID: String? = null

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
        binding.etNombreCliente.setText(arguments?.getString("nombre"))
        binding.etTelefonoCliente.setText(arguments?.getString("telefono"))
        binding.etEmail.setText(arguments?.getString("email"))
        binding.etNotas.setText(arguments?.getString("notas"))

        val telCompleto = arguments?.getString("telefono") ?: ""
        if (telCompleto.contains(" ")) {
            val partes = telCompleto.split(" ")
            binding.etPrefijo?.setText(partes[0])
            binding.etTelefonoCliente.setText(partes[1])
        } else {
            binding.etPrefijo?.setText("+34")
            binding.etTelefonoCliente.setText(telCompleto)
        }

    }

    private fun guardarDatos() {
        val nombre = binding.etNombreCliente.text.toString().trim()
        val telefono = binding.etTelefonoCliente.text.toString().trim()
        val prefijo = binding.etPrefijo?.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val notas = binding.etNotas.text.toString().trim()

        val telefonoFinal = "$prefijo $telefono"

        // VALIDACIÓN BÁSICA
        if (nombre.isEmpty() || email.isEmpty() || telefono.length != 9) {
            Toast.makeText(
                requireContext(),
                context?.getString(R.string.errorLoginCamposVacios),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nombreMayuscula = nombre.lowercase().replaceFirstChar { it.uppercase() }

        if (clienteID != null) {
            // LÓGICA DE EDICIÓN
            val datosActualizados = mapOf(
                "nombre" to nombreMayuscula,
                "telefono" to telefonoFinal,
                "email" to email,
                "notas" to notas
            )
            mainRepository.actualizarCliente(clienteID!!, datosActualizados) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), context?.getString(R.string.cliente_actualizado), Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
            }
        } else {
            mainRepository.buscarClientePorEmail(email) { clienteEmail ->
                if (clienteEmail != null){
                    Toast.makeText(requireContext(), context?.getString(R.string.error_sesion_invalida), Toast.LENGTH_SHORT).show()
                } else {
                    mainRepository.buscarClientePorTelefono(telefonoFinal) { clienteTelefono ->
                        if(clienteTelefono != null){
                            Toast.makeText(requireContext(), getString(R.string.error_cliente_telefono), Toast.LENGTH_SHORT).show()
                        } else {
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456")
                                .addOnCompleteListener { task ->
                                    if(isAdded && task.isSuccessful){
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

                                        Toast.makeText(requireContext(), getString(R.string.cliente_creado), Toast.LENGTH_SHORT).show()
                                        findNavController().navigateUp()
                                    } else if (isAdded){
                                        val errorMsg = task.exception?.message ?: getString(R.string.error_crear_cliente)
                                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}