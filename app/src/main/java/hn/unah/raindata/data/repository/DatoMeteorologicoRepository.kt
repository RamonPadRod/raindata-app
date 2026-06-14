package hn.unah.raindata.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.database.entities.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DatoMeteorologicoRepository(private val dao: DatoMeteorologicoDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("datos_meteorologicos")

    fun obtenerTodos(): Flow<List<DatoMeteorologico>> {
        return dao.obtenerTodos()
    }

    suspend fun obtenerPorId(id: String): DatoMeteorologico? {
        return dao.obtenerPorId(id)
    }

    suspend fun guardarDato(dato: DatoMeteorologico, context: android.content.Context) = withContext(Dispatchers.IO) {
        val localItem = dato.copy(
            syncStatus = SyncStatus.PENDIENTE,
            fechaRegistroLocal = System.currentTimeMillis()
        )
        dao.insertar(localItem)

        // Backup local inmediato
        hn.unah.raindata.data.database.DatabaseBackupManager.realizarBackup(context)

        try {
            collection.document(localItem.id).set(localItem).await()
            dao.actualizarSyncStatus(localItem.id, SyncStatus.ENVIADO)
        } catch (e: Exception) {
            if (e is com.google.firebase.firestore.FirebaseFirestoreException &&
                e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE) {
                // Mantener pendiente
            } else {
                dao.actualizarSyncStatus(localItem.id, SyncStatus.ERROR)
            }
        }
    }

    suspend fun eliminarDato(id: String) = withContext(Dispatchers.IO) {
        try {
            collection.document(id).delete().await()
            dao.eliminarPorId(id)
        } catch (e: Exception) {
            dao.eliminarPorId(id)
        }
    }

    suspend fun sincronizarDesdeNube() = withContext(Dispatchers.IO) {
        try {
            val snapshot = collection.get().await()
            val remotos = snapshot.toObjects(DatoMeteorologico::class.java)
            
            remotos.forEach { remoto ->
                val item = remoto.copy(syncStatus = SyncStatus.ENVIADO)
                dao.insertar(item)
            }
        } catch (e: Exception) {
            // Ignorar errores de red
        }
    }
}
