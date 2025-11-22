package hn.unah.raindata.data.session

import hn.unah.raindata.data.database.entities.Voluntario

object UserSession {

    private var currentUser: Voluntario? = null

    fun login(user: Voluntario) {
        currentUser = user
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): Voluntario? {
        return currentUser
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getUserRole(): String {
        return currentUser?.tipo_usuario ?: "Sin rol"
    }

    fun getCurrentUserUid(): String? {
        return currentUser?.firebase_uid
    }

    fun getCurrentUserName(): String {
        return currentUser?.nombre ?: "Usuario"
    }

    fun isAdmin(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun isVoluntario(): Boolean {
        return getUserRole() == "Voluntario"
    }

    fun isObservador(): Boolean {
        return getUserRole() == "Observador"
    }

    fun canCreateVoluntarios(): Boolean {
        return isAdmin()
    }

    fun canEditVoluntarios(): Boolean {
        return isAdmin()
    }

    fun canDeleteVoluntarios(): Boolean {
        return isAdmin()
    }

    fun canViewVoluntarios(): Boolean {
        return isAdmin()
    }

    fun canCreatePluviometros(): Boolean {
        return isAdmin()
    }

    fun canEditPluviometros(): Boolean {
        return isAdmin()
    }

    fun canDeletePluviometros(): Boolean {
        return isAdmin()
    }

    fun canViewPluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador", "Voluntario")
    }

    fun canViewAllPluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    fun shouldFilterPluviometrosByUser(): Boolean {
        return isVoluntario()
    }

    fun canCreateDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canEditDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canDeleteDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canViewDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario", "Observador")
    }

    fun canViewAllDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    fun shouldFilterDatosMeteorologicosByUser(): Boolean {
        return isVoluntario()
    }

    fun canViewReports(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    fun canGenerateReports(): Boolean {
        return isAdmin()
    }

    fun canExportReports(): Boolean {
        return isAdmin()
    }

    fun canApproveAdminRequests(): Boolean {
        return isAdmin()
    }

    fun canViewAdminNotifications(): Boolean {
        return isAdmin()
    }

    fun canModifySystemSettings(): Boolean {
        return isAdmin()
    }

    fun getUserUidForFiltering(): String? {
        return if (shouldFilterPluviometrosByUser() || shouldFilterDatosMeteorologicosByUser()) {
            getCurrentUserUid()
        } else {
            null
        }
    }

    fun ownsPluviometro(pluviometroResponsableUid: String): Boolean {
        return if (isAdmin()) {
            true
        } else {
            pluviometroResponsableUid == getCurrentUserUid()
        }
    }

    fun canEditDatoMeteorologico(pluviometroResponsableUid: String): Boolean {
        return if (isAdmin()) {
            true
        } else if (isVoluntario()) {
            pluviometroResponsableUid == getCurrentUserUid()
        } else {
            false
        }
    }

    fun getPermissionsSummary(): String {
        val role = getUserRole()
        val name = getCurrentUserName()
        val uid = getCurrentUserUid()

        return """
            Usuario: $name
            Rol: $role
            UID: $uid
            
            Permisos:
            - Crear Pluviómetros: ${canCreatePluviometros()}
            - Ver Todos los Pluviómetros: ${canViewAllPluviometros()}
            - Crear Datos Meteorológicos: ${canCreateDatosMeteorologicos()}
            - Ver Todos los Datos: ${canViewAllDatosMeteorologicos()}
            - Gestionar Voluntarios: ${canCreateVoluntarios()}
            - Ver Reportes: ${canViewReports()}
        """.trimIndent()
    }
}