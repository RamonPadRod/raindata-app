package hn.unah.raindata.data.session

import hn.unah.raindata.data.database.entities.Voluntario

/**
 * Singleton para gestionar la sesión del usuario actual
 */
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

    fun getUserRole(): String? {
        return currentUser?.tipo_usuario
    }

    // Permisos basados en roles
    fun canCreateVoluntarios(): Boolean {
        return getUserRole() == "Administrador"
    }

    
    fun canEditVoluntarios(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canDeleteVoluntarios(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canViewVoluntarios(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    fun canCreatePluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canEditPluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canDeletePluviometros(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canViewPluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario", "Observador")
    }

    fun canCreateDatosMeteorologicos(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canEditDatosMeteorologicos(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canDeleteDatosMeteorologicos(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canViewDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    fun canViewReports(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }
}