package com.example.tfg

class InicioFragment : Fragment(R.layout.fragment_inicio) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí configurarás el RecyclerView más adelante
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDashboard)
    }
}