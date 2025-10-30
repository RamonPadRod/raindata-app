package hn.unah.raindata.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
import hn.unah.raindata.data.database.dao.PluviometroDao
import hn.unah.raindata.data.database.dao.VoluntarioDao

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "raindata_database"
        private const val DATABASE_VERSION = 5 // ← CAMBIADO de 4 a 5

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
        // Tabla de voluntarios con campos de Firebase
        val createVoluntariosTable = """
            CREATE TABLE voluntarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firebase_uid TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                direccion TEXT NOT NULL,
                departamento TEXT NOT NULL,
                municipio TEXT NOT NULL,
                aldea TEXT NOT NULL,
                caserio_barrio_colonia TEXT,
                telefono TEXT,
                email TEXT NOT NULL UNIQUE,
                cedula TEXT,
                fecha_nacimiento TEXT,
                genero TEXT,
                tipo_usuario TEXT,
                estado_aprobacion TEXT DEFAULT 'Aprobado',
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
                responsable_id INTEGER NOT NULL,
                responsable_nombre TEXT NOT NULL,
                observaciones TEXT,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER,
                fecha_modificacion INTEGER,
                FOREIGN KEY (responsable_id) REFERENCES voluntarios(id)
            )
        """.trimIndent()

        // Tabla de datos meteorológicos
        val createDatosMeteorologicosTable = """
            CREATE TABLE datos_meteorologicos (
                id TEXT PRIMARY KEY,
                voluntario_id INTEGER NOT NULL,
                voluntario_nombre TEXT NOT NULL,
                pluviometro_id TEXT NOT NULL,
                pluviometro_registro TEXT NOT NULL,
                fecha TEXT NOT NULL,
                hora TEXT NOT NULL,
                precipitacion REAL NOT NULL,
                temperatura_maxima REAL,
                temperatura_minima REAL,
                condicion_dia TEXT NOT NULL,
                observaciones TEXT,
                fecha_creacion INTEGER NOT NULL,
                fecha_modificacion INTEGER NOT NULL,
                FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
                FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
            )
        """.trimIndent()

        db.execSQL(createVoluntariosTable)
        db.execSQL(createPluviometrosTable)
        db.execSQL(createDatosMeteorologicosTable)

        // Índices para mejorar rendimiento
        db.execSQL("CREATE INDEX idx_voluntarios_nombre ON voluntarios(nombre)")
        db.execSQL("CREATE INDEX idx_voluntarios_email ON voluntarios(email)")
        db.execSQL("CREATE INDEX idx_voluntarios_firebase_uid ON voluntarios(firebase_uid)")
        db.execSQL("CREATE INDEX idx_pluviometros_numero ON pluviometros(numero_registro)")
        db.execSQL("CREATE INDEX idx_pluviometros_responsable ON pluviometros(responsable_id)")
        db.execSQL("CREATE INDEX idx_datos_fecha ON datos_meteorologicos(fecha)")
        db.execSQL("CREATE INDEX idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
        db.execSQL("CREATE INDEX idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
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

        if (oldVersion < 4) {
            val createDatosMeteorologicosTable = """
                CREATE TABLE IF NOT EXISTS datos_meteorologicos (
                    id TEXT PRIMARY KEY,
                    voluntario_id TEXT NOT NULL,
                    voluntario_nombre TEXT NOT NULL,
                    pluviometro_id TEXT NOT NULL,
                    pluviometro_registro TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    hora TEXT NOT NULL,
                    precipitacion REAL NOT NULL,
                    temperatura_maxima REAL,
                    temperatura_minima REAL,
                    condicion_dia TEXT NOT NULL,
                    observaciones TEXT,
                    fecha_creacion INTEGER NOT NULL,
                    fecha_modificacion INTEGER NOT NULL,
                    FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
                    FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
                )
            """.trimIndent()

            db.execSQL(createDatosMeteorologicosTable)
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_fecha ON datos_meteorologicos(fecha)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
        }

        // ← NUEVA MIGRACIÓN PARA FIREBASE
        if (oldVersion < 5) {
            // Agregar columnas nuevas a voluntarios
            try {
                db.execSQL("ALTER TABLE voluntarios ADD COLUMN firebase_uid TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE voluntarios ADD COLUMN estado_aprobacion TEXT DEFAULT 'Aprobado'")

                // Crear índices
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_voluntarios_email ON voluntarios(email)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_voluntarios_firebase_uid ON voluntarios(firebase_uid)")

                // Actualizar pluviómetros para cambiar responsable_id de TEXT a INTEGER
                // Nota: SQLite no permite cambiar tipo de columna directamente,
                // así que si tienes datos existentes, se mantendrán como TEXT

            } catch (e: Exception) {
                // Si hay error, recrear toda la BD
                db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos")
                db.execSQL("DROP TABLE IF EXISTS pluviometros")
                db.execSQL("DROP TABLE IF EXISTS voluntarios")
                onCreate(db)
            }
        }
    }

    fun getVoluntarioDao(): VoluntarioDao {
        return VoluntarioDao(this)
    }

    fun getPluviometroDao(): PluviometroDao {
        return PluviometroDao(this)
    }

    fun getDatoMeteorologicoDao(): DatoMeteorologicoDao {
        return DatoMeteorologicoDao(this)
    }
}