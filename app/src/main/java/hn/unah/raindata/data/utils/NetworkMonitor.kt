package hn.unah.raindata.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitor de conectividad de red. Expone un [StateFlow] reactivo que
 * emite `true` cuando hay internet disponible y `false` cuando no.
 *
 * Uso: inicializar una vez con el Application context y luego observar
 * [isOnline] desde los ViewModels para disparar sincronizaciones automáticas.
 */
object NetworkMonitor {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        // Guardia contra doble inicialización: evita registrar dos NetworkCallbacks
        // si init() se llama tanto desde RainDataApp como desde MainActivity.
        if (initialized) {
            android.util.Log.w("NetworkMonitor", "⚠️ init() llamado más de una vez – ignorando")
            return
        }
        initialized = true

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Inicializar con el estado actual antes de registrar el callback
        _isOnline.value = isCurrentlyConnected(cm)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                android.util.Log.d("NetworkMonitor", "🌐 Red disponible – ONLINE")
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                android.util.Log.d("NetworkMonitor", "📵 Red perdida – OFFLINE")
                // Verificar si aún hay otra red activa antes de marcar offline
                _isOnline.value = isCurrentlyConnected(cm)
            }

            override fun onUnavailable() {
                android.util.Log.d("NetworkMonitor", "📵 Red no disponible – OFFLINE")
                _isOnline.value = false
            }
        })
    }

    private fun isCurrentlyConnected(cm: ConnectivityManager): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
