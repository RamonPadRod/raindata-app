package hn.unah.raindata.data.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DatabaseBackupManager {

    private const val DB_NAME = "raindata_database"
    private const val BACKUP_NAME = "raindata_backup.db"

    suspend fun realizarBackup(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, BACKUP_NAME)
            dbFile.copyTo(backupFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restaurarBackup(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(context.filesDir, "backups/$BACKUP_NAME")
            if (!backupFile.exists()) return@withContext false

            val dbFile = context.getDatabasePath(DB_NAME)
            backupFile.copyTo(dbFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun existeBackup(context: Context): Boolean {
        return File(context.filesDir, "backups/$BACKUP_NAME").exists()
    }
}