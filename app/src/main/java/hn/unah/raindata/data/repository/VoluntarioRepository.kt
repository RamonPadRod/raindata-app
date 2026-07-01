package hn.unah.raindata.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.Voluntario
import hn.unah.raindata.data.database.entities.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class VoluntarioRepository(private val dao: VoluntarioDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("voluntarios")

    fun obtenerVoluntarios(): Flow<List<Voluntario>> {
        return dao.obtenerActivos()
    }

    suspend fun obtenerPorUid(uid: String): Voluntario? {
        return dao.obtenerPorUid(uid)
    }

    suspend fun obtenerPorEmail(email: String): Voluntario? {
        return dao.obtenerPorEmail(email)
    }

    suspend fun guardarVoluntario(voluntario: Voluntario) = withContext(Dispatchers.IO) {
        val localItem = voluntario.copy(
            syncStatus = SyncStatus.PENDIENTE,
            fechaRegistroLocal = System.currentTimeMillis()
        )
        dao.insertar(localItem)

        // Si estamos offline, no intentamos subir a Firestore porque .await() se quedaría bloqueado
        if (!hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value) {
            android.util.Log.d("VoluntarioRepository", "Offline - Voluntario guardado localmente como PENDIENTE: ${localItem.firebase_uid}")
            return@withContext
        }

        try {
            collection.document(localItem.firebase_uid).set(localItem).await()
            dao.actualizarSyncStatus(localItem.firebase_uid, SyncStatus.ENVIADO)
            android.util.Log.d("VoluntarioRepository", "Voluntario guardado en Firestore: ${localItem.firebase_uid}")
        } catch (e: Exception) {
            android.util.Log.e("VoluntarioRepository", "Error al guardar en Firestore: ${localItem.firebase_uid}", e)
            if (hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                // Mantener pendiente
            } else {
                dao.actualizarSyncStatus(localItem.firebase_uid, SyncStatus.ERROR)
            }
        }
    }

    suspend fun eliminarVoluntario(uid: String) = withContext(Dispatchers.IO) {
        try {
            collection.document(uid).delete().await()
            dao.eliminarPorUid(uid)
        } catch (e: Exception) {
            dao.eliminarPorUid(uid)
        }
    }

    suspend fun buscarEnNube(uid: String): Voluntario? = withContext(Dispatchers.IO) {
        try {
            val doc = collection.document(uid).get().await()
            if (doc.exists()) {
                val voluntario = doc.toObject(Voluntario::class.java)
                voluntario?.let {
                    // Guardar localmente para futuras sesiones offline
                    val local = it.copy(syncStatus = SyncStatus.ENVIADO)
                    dao.insertar(local)
                }
                voluntario
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun esPrimerUsuarioEnNube(): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = collection.limit(1).get().await()
            snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sincronizarDesdeNube() = withContext(Dispatchers.IO) {
        if (!hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value) return@withContext
        sincronizarPendientesLocal()
        try {
            val snapshot = collection.get().await()
            val remotos = snapshot.toObjects(Voluntario::class.java)
            
            remotos.forEach { remoto ->
                val item = remoto.copy(syncStatus = SyncStatus.ENVIADO)
                dao.insertar(item)
            }
        } catch (e: Exception) {
            // Ignorar errores de red
        }
    }

    suspend fun sincronizarPendientesLocal() {
        if (!hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value) return
        val pendientes = dao.obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        for (item in pendientes) {
            try {
                collection.document(item.firebase_uid).set(item).await()
                dao.actualizarSyncStatus(item.firebase_uid, SyncStatus.ENVIADO)
                android.util.Log.d("VoluntarioRepo", "✅ Pendiente sincronizado: ${item.firebase_uid}")
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Desconocido"
                android.util.Log.e("VoluntarioRepo", "❌ Error al sincronizar pendiente (UID: ${item.firebase_uid}): $errorMsg", e)
                
                if (hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                    android.util.Log.w("VoluntarioRepo", "⚠️ Error de conexión para ${item.firebase_uid}. Se mantiene PENDIENTE.")
                } else {
                    android.util.Log.e("VoluntarioRepo", "🛑 Error definitivo para ${item.firebase_uid}. Marcando como ERROR.")
                    dao.actualizarSyncStatus(item.firebase_uid, SyncStatus.ERROR)
                }
            }
        }
    }
}