package hn.unah.raindata.data.database.entities

/**
 * Representa el estado de sincronización de un registro local con Firebase.
 */
enum class SyncStatus {
    /**
     * El registro se guardó localmente pero aún no se ha enviado a Firebase.
     */
    PENDIENTE,

    /**
     * El registro se envió exitosamente a Firebase.
     */
    ENVIADO,

    /**
     * Hubo un error al intentar enviar el registro a Firebase (no relacionado con la conexión).
     */
    ERROR
}
