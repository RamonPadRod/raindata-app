package hn.unah.raindata.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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

        // Si estamos offline, no intentamos subir a Firestore porque .await() se quedaría bloqueado
        if (!hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value) {
            android.util.Log.d("DatoMeteorologicoRepo", "Offline - Dato guardado localmente como PENDIENTE: ${localItem.id}")
            return@withContext
        }

        try {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            android.util.Log.d("DatoMeteorologicoRepo", "🔒 [AUTH STATE] ANTES de set() en guardarDato: currentUser = ${currentUser?.uid} (${currentUser?.email})")
            collection.document(localItem.id).set(localItem).await()
            dao.actualizarSyncStatus(localItem.id, SyncStatus.ENVIADO)
            android.util.Log.d("DatoMeteorologicoRepo", "Dato guardado en Firestore: ${localItem.id}")
        } catch (e: Exception) {
            android.util.Log.e("DatoMeteorologicoRepo", "Error al guardar en Firestore: ${localItem.id}", e)
            if (hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                // Mantener pendiente para reintentar después
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
        if (!hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value) return@withContext
        sincronizarPendientesLocal()
        try {
            val snapshot = collection.get().await()
            val remotos = snapshot.toObjects(DatoMeteorologico::class.java)

            remotos.forEach { remoto ->
                android.util.Log.d("DEBUG_SYNC",
                    "id=${remoto.id} activo=${remoto.activo} fecha=${remoto.fecha_lectura}")
                // Fix: preservar fechaRegistroLocal del registro local si ya existe.
                // OnConflictStrategy.REPLACE haría DELETE+INSERT con un timestamp nuevo
                // (System.currentTimeMillis() del default de la entidad), borrando el original.
                val existing = dao.obtenerPorId(remoto.id)
                val item = remoto.copy(
                    syncStatus = SyncStatus.ENVIADO,
                    fechaRegistroLocal = existing?.fechaRegistroLocal ?: System.currentTimeMillis()
                )
                dao.insertar(item)
            }
        } catch (e: Exception) {
            // Ignorar errores de red
        }
    }

    suspend fun sincronizarPendientesLocal() {
        val isNetworkOnline = hn.unah.raindata.data.utils.NetworkMonitor.isOnline.value
        android.util.Log.d("DatoMeteorologicoRepo", "🔍 sincronizarPendientesLocal() llamada. NetworkMonitor.isOnline = $isNetworkOnline")
        
        // DUMP COMPLETO OBLIGATORIO DE LA TABLA datos_meteorologicos
        try {
            val todosLosDatos = dao.obtenerTodosDirecto()
            android.util.Log.d("DatoMeteorologicoRepo", "📋 [DUMP COMPLETO ROOM] Total de filas en la tabla datos_meteorologicos: ${todosLosDatos.size}")
            todosLosDatos.forEachIndexed { index, item ->
                android.util.Log.d("DatoMeteorologicoRepo", "   -> Fila #$index: $item")
            }
        } catch (dbEx: Exception) {
            android.util.Log.e("DatoMeteorologicoRepo", "   ❌ Falló el dump completo de la tabla Room", dbEx)
        }

        if (!isNetworkOnline) {
            android.util.Log.d("DatoMeteorologicoRepo", "🚫 Cancelando sincronización: NetworkMonitor reporta OFFLINE.")
            return
        }
        
        val pendientes = dao.obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        android.util.Log.d("DatoMeteorologicoRepo", "📊 Se encontraron ${pendientes.size} registros PENDIENTES en la base de datos Room.")
        
        for (item in pendientes) {
            android.util.Log.d("DatoMeteorologicoRepo", "⏳ Intentando subir registro ID: ${item.id} a la colección 'datos_meteorologicos'...")
            try {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                android.util.Log.d("DatoMeteorologicoRepo", "🔒 [AUTH STATE] ANTES de set() en sincronizarPendientesLocal: currentUser = ${currentUser?.uid} (${currentUser?.email})")
                collection.document(item.id).set(item).await()
                dao.actualizarSyncStatus(item.id, SyncStatus.ENVIADO)
                android.util.Log.d("DatoMeteorologicoRepo", "✅ Pendiente sincronizado exitosamente: ${item.id}")
            } catch (e: Exception) {
                val errorClass = e.javaClass.simpleName
                val errorMsg = e.message ?: "Sin mensaje"
                
                android.util.Log.e("DatoMeteorologicoRepo", "❌ ERROR al sincronizar pendiente (ID: ${item.id})")
                android.util.Log.e("DatoMeteorologicoRepo", "   Clase de excepción: $errorClass")
                android.util.Log.e("DatoMeteorologicoRepo", "   Mensaje de error: $errorMsg")
                
                if (e is FirebaseFirestoreException) {
                    android.util.Log.e("DatoMeteorologicoRepo", "   Código de FirestoreException: ${e.code} (${e.code.name})")
                }
                
                android.util.Log.e("DatoMeteorologicoRepo", "   Stacktrace completo:", e)
                
                if (hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                    android.util.Log.w("DatoMeteorologicoRepo", "⚠️ Se determinó que es un error de conexión para ${item.id}. Permanece PENDIENTE.")
                } else {
                    android.util.Log.e("DatoMeteorologicoRepo", "🛑 Se determinó que es un error definitivo para ${item.id}. Marcando como ERROR en Room.")
                    dao.actualizarSyncStatus(item.id, SyncStatus.ERROR)
                }
            }
        }
    }
}
