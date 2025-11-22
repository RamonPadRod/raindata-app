//package hn.unah.raindata.data.database
//
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import hn.unah.raindata.data.database.dao.DatoMeteorologicoDao
//import hn.unah.raindata.data.database.dao.PluviometroDao
//import hn.unah.raindata.data.database.dao.VoluntarioDao
//
//class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//    companion object {
//        private const val DATABASE_NAME = "raindata_database"
//        private const val DATABASE_VERSION = 8 // INCREMENTAR A 8 para forzar recreación limpia
//
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = AppDatabase(context.applicationContext)
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//    override fun onCreate(db: SQLiteDatabase) {
//        // Tabla de voluntarios
//        val createVoluntariosTable = """
//            CREATE TABLE voluntarios (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                firebase_uid TEXT NOT NULL UNIQUE,
//                nombre TEXT NOT NULL,
//                direccion TEXT NOT NULL,
//                departamento TEXT NOT NULL,
//                municipio TEXT NOT NULL,
//                aldea TEXT NOT NULL,
//                caserio_barrio_colonia TEXT,
//                telefono TEXT,
//                email TEXT NOT NULL UNIQUE,
//                tipo_documento TEXT NOT NULL DEFAULT 'DNI',
//                cedula TEXT,
//                pasaporte TEXT,
//                fecha_nacimiento TEXT,
//                genero TEXT,
//                tipo_usuario TEXT,
//                estado_aprobacion TEXT DEFAULT 'Aprobado',
//                experiencia_años INTEGER,
//                observaciones TEXT,
//                fotografia TEXT,
//                activo INTEGER DEFAULT 1,
//                fecha_creacion INTEGER,
//                fecha_modificacion INTEGER
//            )
//        """.trimIndent()
//
//        // Tabla de pluviómetros
//        val createPluviometrosTable = """
//            CREATE TABLE pluviometros (
//                id TEXT PRIMARY KEY,
//                numero_registro TEXT NOT NULL UNIQUE,
//                latitud REAL NOT NULL,
//                longitud REAL NOT NULL,
//                direccion TEXT NOT NULL,
//                departamento TEXT NOT NULL,
//                municipio TEXT NOT NULL,
//                aldea TEXT NOT NULL,
//                caserio_barrio_colonia TEXT,
//                responsable_id INTEGER NOT NULL,
//                responsable_nombre TEXT NOT NULL,
//                observaciones TEXT,
//                activo INTEGER DEFAULT 1,
//                fecha_creacion INTEGER,
//                fecha_modificacion INTEGER,
//                FOREIGN KEY (responsable_id) REFERENCES voluntarios(id)
//            )
//        """.trimIndent()
//
//        // Tabla de datos meteorológicos ACTUALIZADA
//        val createDatosMeteorologicosTable = """
//            CREATE TABLE datos_meteorologicos (
//                id TEXT PRIMARY KEY,
//                voluntario_id INTEGER NOT NULL,
//                voluntario_nombre TEXT NOT NULL,
//                pluviometro_id TEXT NOT NULL,
//                pluviometro_registro TEXT NOT NULL,
//                fecha_lectura TEXT NOT NULL,
//                hora_lectura TEXT NOT NULL,
//                fecha_registro TEXT NOT NULL,
//                hora_registro TEXT NOT NULL,
//                precipitacion REAL NOT NULL,
//                temperatura_maxima REAL,
//                temperatura_minima REAL,
//                condiciones_dia TEXT NOT NULL,
//                observaciones TEXT,
//                fecha_creacion INTEGER NOT NULL,
//                fecha_modificacion INTEGER NOT NULL,
//                FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
//                FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
//            )
//        """.trimIndent()
//
//        db.execSQL(createVoluntariosTable)
//        db.execSQL(createPluviometrosTable)
//        db.execSQL(createDatosMeteorologicosTable)
//
//        // Índices para mejorar rendimiento
//        db.execSQL("CREATE INDEX idx_voluntarios_nombre ON voluntarios(nombre)")
//        db.execSQL("CREATE INDEX idx_voluntarios_email ON voluntarios(email)")
//        db.execSQL("CREATE INDEX idx_voluntarios_firebase_uid ON voluntarios(firebase_uid)")
//        db.execSQL("CREATE INDEX idx_pluviometros_numero ON pluviometros(numero_registro)")
//        db.execSQL("CREATE INDEX idx_pluviometros_responsable ON pluviometros(responsable_id)")
//        db.execSQL("CREATE INDEX idx_datos_fecha_lectura ON datos_meteorologicos(fecha_lectura)")
//        db.execSQL("CREATE INDEX idx_datos_fecha_registro ON datos_meteorologicos(fecha_registro)")
//        db.execSQL("CREATE INDEX idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
//        db.execSQL("CREATE INDEX idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        // Migraciones progresivas
//        if (oldVersion < 2) {
//            try {
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN tipo_usuario TEXT")
//                db.execSQL("UPDATE voluntarios SET tipo_usuario = ocupacion WHERE ocupacion IS NOT NULL")
//            } catch (e: Exception) {
//                recreateDatabase(db)
//                return
//            }
//        }
//
//        if (oldVersion < 3) {
//            try {
//                val createPluviometrosTable = """
//                    CREATE TABLE IF NOT EXISTS pluviometros (
//                        id TEXT PRIMARY KEY,
//                        numero_registro TEXT NOT NULL UNIQUE,
//                        latitud REAL NOT NULL,
//                        longitud REAL NOT NULL,
//                        direccion TEXT NOT NULL,
//                        departamento TEXT NOT NULL,
//                        municipio TEXT NOT NULL,
//                        aldea TEXT NOT NULL,
//                        caserio_barrio_colonia TEXT,
//                        responsable_id TEXT NOT NULL,
//                        responsable_nombre TEXT NOT NULL,
//                        observaciones TEXT,
//                        activo INTEGER DEFAULT 1,
//                        fecha_creacion INTEGER,
//                        fecha_modificacion INTEGER,
//                        FOREIGN KEY (responsable_id) REFERENCES voluntarios(id)
//                    )
//                """.trimIndent()
//                db.execSQL(createPluviometrosTable)
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_pluviometros_numero ON pluviometros(numero_registro)")
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_pluviometros_responsable ON pluviometros(responsable_id)")
//            } catch (e: Exception) {
//                recreateDatabase(db)
//                return
//            }
//        }
//
//        if (oldVersion < 4) {
//            try {
//                val createDatosMeteorologicosTable = """
//                    CREATE TABLE IF NOT EXISTS datos_meteorologicos (
//                        id TEXT PRIMARY KEY,
//                        voluntario_id TEXT NOT NULL,
//                        voluntario_nombre TEXT NOT NULL,
//                        pluviometro_id TEXT NOT NULL,
//                        pluviometro_registro TEXT NOT NULL,
//                        fecha TEXT NOT NULL,
//                        hora TEXT NOT NULL,
//                        precipitacion REAL NOT NULL,
//                        temperatura_maxima REAL,
//                        temperatura_minima REAL,
//                        condicion_dia TEXT NOT NULL,
//                        observaciones TEXT,
//                        fecha_creacion INTEGER NOT NULL,
//                        fecha_modificacion INTEGER NOT NULL,
//                        FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
//                        FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
//                    )
//                """.trimIndent()
//                db.execSQL(createDatosMeteorologicosTable)
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_fecha ON datos_meteorologicos(fecha)")
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
//            } catch (e: Exception) {
//                recreateDatabase(db)
//                return
//            }
//        }
//
//        if (oldVersion < 5) {
//            try {
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN firebase_uid TEXT DEFAULT ''")
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN estado_aprobacion TEXT DEFAULT 'Aprobado'")
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_voluntarios_email ON voluntarios(email)")
//                db.execSQL("CREATE INDEX IF NOT EXISTS idx_voluntarios_firebase_uid ON voluntarios(firebase_uid)")
//            } catch (e: Exception) {
//                recreateDatabase(db)
//                return
//            }
//        }
//
//        if (oldVersion < 6) {
//            try {
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN tipo_documento TEXT NOT NULL DEFAULT 'DNI'")
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN pasaporte TEXT")
//                db.execSQL("ALTER TABLE voluntarios ADD COLUMN fotografia TEXT")
//                db.execSQL("UPDATE voluntarios SET tipo_documento = 'DNI' WHERE cedula IS NOT NULL AND cedula != ''")
//            } catch (e: Exception) {
//                recreateDatabase(db)
//                return
//            }
//        }
//
//        // MIGRACIÓN CRÍTICA: Versión 7 y 8 - Actualizar tabla datos_meteorologicos
//        if (oldVersion < 8) {
//            try {
//                // Verificar si la tabla existe y su estructura
//                val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='datos_meteorologicos'", null)
//                val tablaExiste = cursor.count > 0
//                cursor.close()
//
//                if (tablaExiste) {
//                    // Verificar columnas existentes
//                    val columnCursor = db.rawQuery("PRAGMA table_info(datos_meteorologicos)", null)
//                    val existingColumns = mutableSetOf<String>()
//                    while (columnCursor.moveToNext()) {
//                        existingColumns.add(columnCursor.getString(columnCursor.getColumnIndexOrThrow("name")))
//                    }
//                    columnCursor.close()
//
//                    // Si tiene la estructura antigua, migrar datos
//                    if (existingColumns.contains("fecha") && !existingColumns.contains("fecha_lectura")) {
//                        migrarDatosMeteorologicos(db)
//                    } else if (!existingColumns.contains("fecha_lectura")) {
//                        // Estructura corrupta, recrear tabla vacía
//                        db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos")
//                        crearTablaDatosMeteorologicosNueva(db)
//                    }
//                } else {
//                    // La tabla no existe, crearla
//                    crearTablaDatosMeteorologicosNueva(db)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                // Si falla todo, eliminar solo tabla datos_meteorologicos y recrearla
//                db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos")
//                crearTablaDatosMeteorologicosNueva(db)
//            }
//        }
//    }
//
//    private fun migrarDatosMeteorologicos(db: SQLiteDatabase) {
//        // Crear tabla temporal con nueva estructura
//        val createTempTable = """
//            CREATE TABLE datos_meteorologicos_temp (
//                id TEXT PRIMARY KEY,
//                voluntario_id INTEGER NOT NULL,
//                voluntario_nombre TEXT NOT NULL,
//                pluviometro_id TEXT NOT NULL,
//                pluviometro_registro TEXT NOT NULL,
//                fecha_lectura TEXT NOT NULL,
//                hora_lectura TEXT NOT NULL,
//                fecha_registro TEXT NOT NULL,
//                hora_registro TEXT NOT NULL,
//                precipitacion REAL NOT NULL,
//                temperatura_maxima REAL,
//                temperatura_minima REAL,
//                condiciones_dia TEXT NOT NULL,
//                observaciones TEXT,
//                fecha_creacion INTEGER NOT NULL,
//                fecha_modificacion INTEGER NOT NULL,
//                FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
//                FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
//            )
//        """.trimIndent()
//        db.execSQL(createTempTable)
//
//        // Copiar datos de tabla antigua a nueva (CORREGIDO)
//        val copyData = """
//            INSERT INTO datos_meteorologicos_temp (
//                id, voluntario_id, voluntario_nombre, pluviometro_id, pluviometro_registro,
//                fecha_lectura, hora_lectura, fecha_registro, hora_registro,
//                precipitacion, temperatura_maxima, temperatura_minima,
//                condiciones_dia, observaciones, fecha_creacion, fecha_modificacion
//            )
//            SELECT
//                id,
//                CASE
//                    WHEN typeof(voluntario_id) = 'text' THEN CAST(voluntario_id AS INTEGER)
//                    ELSE voluntario_id
//                END as voluntario_id,
//                voluntario_nombre,
//                pluviometro_id,
//                pluviometro_registro,
//                COALESCE(fecha, '') as fecha_lectura,
//                COALESCE(hora, '') as hora_lectura,
//                COALESCE(fecha, '') as fecha_registro,
//                COALESCE(hora, '') as hora_registro,
//                precipitacion,
//                temperatura_maxima,
//                temperatura_minima,
//                COALESCE(condicion_dia, condiciones_dia, '') as condiciones_dia,
//                observaciones,
//                fecha_creacion,
//                fecha_modificacion
//            FROM datos_meteorologicos
//        """.trimIndent()
//
//        try {
//            db.execSQL(copyData)
//
//            // Eliminar tabla antigua
//            db.execSQL("DROP TABLE datos_meteorologicos")
//
//            // Renombrar tabla temporal
//            db.execSQL("ALTER TABLE datos_meteorologicos_temp RENAME TO datos_meteorologicos")
//
//            // Recrear índices
//            db.execSQL("CREATE INDEX idx_datos_fecha_lectura ON datos_meteorologicos(fecha_lectura)")
//            db.execSQL("CREATE INDEX idx_datos_fecha_registro ON datos_meteorologicos(fecha_registro)")
//            db.execSQL("CREATE INDEX idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
//            db.execSQL("CREATE INDEX idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Si falla la migración, eliminar tabla temporal y crear tabla nueva vacía
//            db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos_temp")
//            db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos")
//            crearTablaDatosMeteorologicosNueva(db)
//        }
//    }
//
//    private fun crearTablaDatosMeteorologicosNueva(db: SQLiteDatabase) {
//        val createDatosMeteorologicosTable = """
//            CREATE TABLE datos_meteorologicos (
//                id TEXT PRIMARY KEY,
//                voluntario_id INTEGER NOT NULL,
//                voluntario_nombre TEXT NOT NULL,
//                pluviometro_id TEXT NOT NULL,
//                pluviometro_registro TEXT NOT NULL,
//                fecha_lectura TEXT NOT NULL,
//                hora_lectura TEXT NOT NULL,
//                fecha_registro TEXT NOT NULL,
//                hora_registro TEXT NOT NULL,
//                precipitacion REAL NOT NULL,
//                temperatura_maxima REAL,
//                temperatura_minima REAL,
//                condiciones_dia TEXT NOT NULL,
//                observaciones TEXT,
//                fecha_creacion INTEGER NOT NULL,
//                fecha_modificacion INTEGER NOT NULL,
//                FOREIGN KEY(voluntario_id) REFERENCES voluntarios(id),
//                FOREIGN KEY(pluviometro_id) REFERENCES pluviometros(id)
//            )
//        """.trimIndent()
//        db.execSQL(createDatosMeteorologicosTable)
//
//        // Crear índices
//        db.execSQL("CREATE INDEX idx_datos_fecha_lectura ON datos_meteorologicos(fecha_lectura)")
//        db.execSQL("CREATE INDEX idx_datos_fecha_registro ON datos_meteorologicos(fecha_registro)")
//        db.execSQL("CREATE INDEX idx_datos_pluviometro ON datos_meteorologicos(pluviometro_id)")
//        db.execSQL("CREATE INDEX idx_datos_voluntario ON datos_meteorologicos(voluntario_id)")
//    }
//
//    private fun recreateDatabase(db: SQLiteDatabase) {
//        db.execSQL("DROP TABLE IF EXISTS datos_meteorologicos")
//        db.execSQL("DROP TABLE IF EXISTS pluviometros")
//        db.execSQL("DROP TABLE IF EXISTS voluntarios")
//        onCreate(db)
//    }
//
//    fun getVoluntarioDao(): VoluntarioDao {
//        return VoluntarioDao(this)
//    }
//
//    fun getPluviometroDao(): PluviometroDao {
//        return PluviometroDao(this)
//    }
//
//    fun getDatoMeteorologicoDao(): DatoMeteorologicoDao {
//        return DatoMeteorologicoDao(this)
//    }
//}