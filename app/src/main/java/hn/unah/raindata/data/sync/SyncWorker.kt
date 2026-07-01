package hn.unah.raindata.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import hn.unah.raindata.data.database.AppDatabase
import hn.unah.raindata.data.database.entities.SyncStatus
import kotlinx.coroutines.tasks.await

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val db = AppDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        return try {
            sincronizarVoluntarios()
            sincronizarPluviometros()
            sincronizarDatosMeteorologicos()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun sincronizarVoluntarios() {
        val pendientes = db.voluntarioDao().obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        val collection = firestore.collection("voluntarios")
        
        for (item in pendientes) {
            try {
                collection.document(item.firebase_uid).set(item).await()
                db.voluntarioDao().actualizarSyncStatus(item.firebase_uid, SyncStatus.ENVIADO)
            } catch (e: Exception) {
                if (!hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                    db.voluntarioDao().actualizarSyncStatus(item.firebase_uid, SyncStatus.ERROR)
                }
            }
        }
    }

    private suspend fun sincronizarPluviometros() {
        val pendientes = db.pluviometroDao().obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        val collection = firestore.collection("pluviometros")
        
        for (item in pendientes) {
            try {
                collection.document(item.id).set(item).await()
                db.pluviometroDao().actualizarSyncStatus(item.id, SyncStatus.ENVIADO)
            } catch (e: Exception) {
                if (!hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                    db.pluviometroDao().actualizarSyncStatus(item.id, SyncStatus.ERROR)
                }
            }
        }
    }

    private suspend fun sincronizarDatosMeteorologicos() {
        val pendientes = db.datoMeteorologicoDao().obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        val collection = firestore.collection("datos_meteorologicos")
        
        for (item in pendientes) {
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            android.util.Log.d("SyncWorker", "🔒 [AUTH STATE] ANTES de set() en SyncWorker: currentUser = ${currentUser?.uid} (${currentUser?.email})")
            try {
                collection.document(item.id).set(item).await()
                db.datoMeteorologicoDao().actualizarSyncStatus(item.id, SyncStatus.ENVIADO)
            } catch (e: Exception) {
                android.util.Log.e("SyncWorker", "❌ Error al sincronizar en SyncWorker para ID: ${item.id}", e)
                if (!hn.unah.raindata.data.utils.NetworkUtils.isConnectionError(e)) {
                    db.datoMeteorologicoDao().actualizarSyncStatus(item.id, SyncStatus.ERROR)
                }
            }
        }
    }
}
