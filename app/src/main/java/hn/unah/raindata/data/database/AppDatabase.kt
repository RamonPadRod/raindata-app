package hn.unah.raindata.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
import hn.unah.raindata.data.database.dao.PluviometroDao
import hn.unah.raindata.data.database.dao.VoluntarioDao
import hn.unah.raindata.data.database.entities.DatoMeteorologico
import hn.unah.raindata.data.database.entities.Pluviometro
import hn.unah.raindata.data.database.entities.Voluntario

@Database(
    entities = [
        Voluntario::class,
        Pluviometro::class,
        DatoMeteorologico::class
    ],
    version = 10, // Incrementamos versión para forzar migración limpia tras cambios de offline mode
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voluntarioDao(): VoluntarioDao
    abstract fun pluviometroDao(): PluviometroDao
    abstract fun datoMeteorologicoDao(): DatoMeteorologicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raindata_database"
                )
                .fallbackToDestructiveMigration() // Recrear si hay cambios de esquema en esta fase
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}