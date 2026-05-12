package hn.unah.raindata.data.database

import androidx.room.TypeConverter
import hn.unah.raindata.data.database.entities.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return try {
            SyncStatus.valueOf(value)
        } catch (e: Exception) {
            SyncStatus.PENDIENTE
        }
    }
}
