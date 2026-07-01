package hn.unah.raindata

import android.app.Application
import hn.unah.raindata.data.utils.NetworkMonitor

/**
 * Clase Application personalizada para inicializar componentes globales al arrancar la app.
 */
class RainDataApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el monitor de red para sincronización automática al recuperar conexión
        NetworkMonitor.init(this)
    }
}
