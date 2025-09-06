package hn.unah.raindata.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hn.unah.raindata.data.database.dao.VoluntarioDao

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "raindata_database"
        private const val DATABASE_VERSION = 1

        // Tabla voluntarios
        private const val TABLE_VOLUNTARIOS = "voluntarios"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_DIRECCION = "direccion"
        private const val COLUMN_DEPARTAMENTO = "departamento"
        private const val COLUMN_MUNICIPIO = "municipio"
        private const val COLUMN_ALDEA = "aldea"
        private const val COLUMN_CASERIO = "caserio_barrio_colonia"
        private const val COLUMN_TELEFONO = "telefono"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_CEDULA = "cedula"
        private const val COLUMN_FECHA_NACIMIENTO = "fecha_nacimiento"
        private const val COLUMN_GENERO = "genero"
        private const val COLUMN_OCUPACION = "ocupacion"
        private const val COLUMN_EXPERIENCIA = "experiencia_años"
        private const val COLUMN_OBSERVACIONES = "observaciones"
        private const val COLUMN_ACTIVO = "activo"
        private const val COLUMN_FECHA_CREACION = "fecha_creacion"
        private const val COLUMN_FECHA_MODIFICACION = "fecha_modificacion"

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
        val createVoluntariosTable = """
            CREATE TABLE $TABLE_VOLUNTARIOS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_DIRECCION TEXT NOT NULL,
                $COLUMN_DEPARTAMENTO TEXT NOT NULL,
                $COLUMN_MUNICIPIO TEXT NOT NULL,
                $COLUMN_ALDEA TEXT NOT NULL,
                $COLUMN_CASERIO TEXT,
                $COLUMN_TELEFONO TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_CEDULA TEXT,
                $COLUMN_FECHA_NACIMIENTO TEXT,
                $COLUMN_GENERO TEXT,
                $COLUMN_OCUPACION TEXT,
                $COLUMN_EXPERIENCIA INTEGER,
                $COLUMN_OBSERVACIONES TEXT,
                $COLUMN_ACTIVO INTEGER DEFAULT 1,
                $COLUMN_FECHA_CREACION INTEGER,
                $COLUMN_FECHA_MODIFICACION INTEGER
            )
        """.trimIndent()

        db.execSQL(createVoluntariosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VOLUNTARIOS")
        onCreate(db)
    }

    fun getVoluntarioDao(): VoluntarioDao {
        return VoluntarioDao(this)
    }
}