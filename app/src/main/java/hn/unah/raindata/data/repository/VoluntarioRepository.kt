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

        try {
            collection.document(localItem.firebase_uid).set(localItem).await()
            dao.actualizarSyncStatus(localItem.firebase_uid, SyncStatus.ENVIADO)
        } catch (e: Exception) {
            if (e is com.google.firebase.firestore.FirebaseFirestoreException && 
                e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE) {
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

    suspend fun sincronizarDesdeNube() = withContext(Dispatchers.IO) {
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
}