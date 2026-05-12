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
                // Si es un error definitivo, marcar como ERROR
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
            }
        }
    }

    private suspend fun sincronizarDatosMeteorologicos() {
        val pendientes = db.datoMeteorologicoDao().obtenerPorSyncStatus(SyncStatus.PENDIENTE)
        val collection = firestore.collection("datos_meteorologicos")
        
        for (item in pendientes) {
            try {
                collection.document(item.id).set(item).await()
                db.datoMeteorologicoDao().actualizarSyncStatus(item.id, SyncStatus.ENVIADO)
            } catch (e: Exception) {
            }
        }
    }
}
