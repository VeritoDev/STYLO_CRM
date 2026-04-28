package com.example.tfg.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tfg.R

class NotificacionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val servicio = inputData.getString("SERVICIO") ?: ""
        val hora = inputData.getString("HORA") ?: ""

        val titulo = applicationContext.getString(R.string.notif_recordatorio_titulo)
        val detalle = applicationContext.getString(R.string.notif_recordatorio_detalle, servicio, hora)

        mostrarNotificacion(titulo, detalle)
        return Result.success()
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val channelId = "citas_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Recordatorio de Citas", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_citas) // Asegúrate de que este icono existe
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}