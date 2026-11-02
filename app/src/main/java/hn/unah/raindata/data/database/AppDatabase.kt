package hn.unah.raindata.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 11, // v11: campo humedad_relativa en datos_meteorologicos
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

        /** Migración 10 → 11: agrega columna humedad_relativa (nullable) */
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE datos_meteorologicos ADD COLUMN humedad_relativa REAL DEFAULT NULL"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raindata_database"
                )
                .addMigrations(MIGRATION_10_11)
                .fallbackToDestructiveMigration() // Seguro de respaldo si falla la migración
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}