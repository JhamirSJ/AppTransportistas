package com.example.apptransportistas.ubicacion

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.apptransportistas.data.local.DatabaseHelper
import com.example.apptransportistas.data.local.Repository
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val dbHelper = DatabaseHelper(this)
        repository = Repository(dbHelper)

        createLocationRequest()
        createLocationCallback()
        startForegroundServiceNotification()
        startLocationUpdates()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location: Location in result.locations) {
                    val fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )
                    val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                    repository.insertarUbicacion(
                        lat = location.latitude,
                        lon = location.longitude,
                        fechaHora = fechaHora,
                        deviceId = deviceId
                    )

                    Log.d("TrackingService", "Ubicación guardada: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "ubicacion_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ubicación en segundo plano",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rastreando ubicación")
            .setContentText("La app está registrando tu recorrido.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopSelf()
        Log.d("TrackingService", "App cerrada: servicio detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
