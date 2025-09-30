package hn.unah.raindata.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hn.unah.raindata.data.database.dao.PluviometroDao
import hn.unah.raindata.data.database.dao.VoluntarioDao

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "raindata_database"
        private const val DATABASE_VERSION = 3 // Incrementado para incluir pluviómetros

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de voluntarios
        val createVoluntariosTable = """
            CREATE TABLE voluntarios (
                id TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                direccion TEXT NOT NULL,
                departamento TEXT NOT NULL,
                municipio TEXT NOT NULL,
                aldea TEXT NOT NULL,
                caserio_barrio_colonia TEXT,
                telefono TEXT,
                email TEXT,
                cedula TEXT,
                fecha_nacimiento TEXT,
                genero TEXT,
                tipo_usuario TEXT,
                experiencia_años INTEGER,
                observaciones TEXT,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER,
                fecha_modificacion INTEGER
            )
        """.trimIndent()

        // Tabla de pluviómetros
        val createPluviometrosTable = """
            CREATE TABLE pluviometros (
                id TEXT PRIMARY KEY,
                numero_registro TEXT NOT NULL UNIQUE,
                latitud REAL NOT NULL,
                longitud REAL NOT NULL,
                direccion TEXT NOT NULL,
                departamento TEXT NOT NULL,
                municipio TEXT NOT NULL,
                aldea TEXT NOT NULL,
                caserio_barrio_colonia TEXT,
                responsable_id TEXT NOT NULL,
                responsable_nombre TEXT NOT NULL,
                observaciones TEXT,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER,
                fecha_modificacion INTEGER,
                FOREIGN KEY (responsable_id) REFERENCES voluntarios(id)
            )
        """.trimIndent()

        db.execSQL(createVoluntariosTable)
        db.execSQL(createPluviometrosTable)

        // Índices para mejorar rendimiento
        db.execSQL("CREATE INDEX idx_voluntarios_nombre ON voluntarios(nombre)")
        db.execSQL("CREATE INDEX idx_pluviometros_numero ON pluviometros(numero_registro)")
        db.execSQL("CREATE INDEX idx_pluviometros_responsable ON pluviometros(responsable_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE voluntarios ADD COLUMN tipo_usuario TEXT")
                db.execSQL("UPDATE voluntarios SET tipo_usuario = ocupacion WHERE ocupacion IS NOT NULL")
            } catch (e: Exception) {
                db.execSQL("DROP TABLE IF EXISTS voluntarios")
                onCreate(db)
            }
        }

        if (oldVersion < 3) {
            // Crear tabla de pluviómetros en actualización
            val createPluviometrosTable = """
                CREATE TABLE IF NOT EXISTS pluviometros (
                    id TEXT PRIMARY KEY,
                    numero_registro TEXT NOT NULL UNIQUE,
                    latitud REAL NOT NULL,
                    longitud REAL NOT NULL,
                    direccion TEXT NOT NULL,
                    departamento TEXT NOT NULL,
                    municipio TEXT NOT NULL,
                    aldea TEXT NOT NULL,
                    caserio_barrio_colonia TEXT,
                    responsable_id TEXT NOT NULL,
                    responsable_nombre TEXT NOT NULL,
                    observaciones TEXT,
                    activo INTEGER DEFAULT 1,
                    fecha_creacion INTEGER,
                    fecha_modificacion INTEGER,
                    FOREIGN KEY (responsable_id) REFERENCES voluntarios(id)
                )
            """.trimIndent()

            db.execSQL(createPluviometrosTable)
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_pluviometros_numero ON pluviometros(numero_registro)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_pluviometros_responsable ON pluviometros(responsable_id)")
        }
    }

    fun getVoluntarioDao(): VoluntarioDao {
        return VoluntarioDao(this)
    }

    fun getPluviometroDao(): PluviometroDao {
        return PluviometroDao(this)
    }
}