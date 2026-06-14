package hn.unah.raindata.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hn.unah.raindata.data.database.dao.PluviometroDao
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.content.Context
import hn.unah.raindata.data.database.DatabaseBackupManager

class PluviometroRepository(private val pluviometroDao: PluviometroDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("pluviometros")

    fun obtenerPluviometros(): Flow<List<Pluviometro>> {
        return pluviometroDao.obtenerActivos()
    }

    suspend fun obtenerPorId(id: String): Pluviometro? {
        return pluviometroDao.obtenerPorId(id)
    }

    suspend fun guardarPluviometro(pluviometro: Pluviometro, context: Context) = withContext(Dispatchers.IO) {
        val localItem = pluviometro.copy(
            syncStatus = SyncStatus.PENDIENTE,
            fechaRegistroLocal = System.currentTimeMillis()
        )
        pluviometroDao.insertar(localItem)

        // Backup local inmediato tras guardar
        DatabaseBackupManager.realizarBackup(context)

        try {
            collection.document(localItem.id).set(localItem).await()
            pluviometroDao.actualizarSyncStatus(localItem.id, SyncStatus.ENVIADO)
        } catch (e: Exception) {
            if (e is com.google.firebase.firestore.FirebaseFirestoreException &&
                e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE) {
                // Mantener pendiente
            } else {
                pluviometroDao.actualizarSyncStatus(localItem.id, SyncStatus.ERROR)
            }
        }
    }

    suspend fun eliminarPluviometro(id: String) = withContext(Dispatchers.IO) {
        try {
            collection.document(id).delete().await()
            pluviometroDao.eliminarPorId(id)
        } catch (e: Exception) {
            // Si falla la eliminación en la nube, marcamos como inactivo localmente 
            // para una futura sincronización de eliminación si fuera necesario.
            // Por simplicidad en este MVP, eliminamos local.
            pluviometroDao.eliminarPorId(id)
        }
    }

    suspend fun sincronizarDesdeNube() = withContext(Dispatchers.IO) {
        try {
            val snapshot = collection.get().await()
            val remotos = snapshot.toObjects(Pluviometro::class.java)
            
            remotos.forEach { remoto ->
                // Marcar como enviado para evitar re-sincronización inmediata
                val item = remoto.copy(syncStatus = SyncStatus.ENVIADO)
                pluviometroDao.insertar(item)
            }
        } catch (e: Exception) {
            // Error de red o permisos, ignorar para modo offline
        }
    }
}