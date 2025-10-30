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

    // ========== PERMISOS DE VOLUNTARIOS ==========
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

    // ========== PERMISOS DE PLUVIÓMETROS ==========
    // SOLO Administradores pueden gestionar pluviómetros
    fun canCreatePluviometros(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canEditPluviometros(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canDeletePluviometros(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canViewPluviometros(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }

    // ========== PERMISOS DE DATOS METEOROLÓGICOS ==========
    // Administradores y Voluntarios pueden crear datos
    fun canCreateDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canEditDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario")
    }

    fun canDeleteDatosMeteorologicos(): Boolean {
        return getUserRole() == "Administrador"
    }

    fun canViewDatosMeteorologicos(): Boolean {
        return getUserRole() in listOf("Administrador", "Voluntario", "Observador")
    }

    // ========== PERMISOS DE REPORTES ==========
    fun canViewReports(): Boolean {
        return getUserRole() in listOf("Administrador", "Observador")
    }
}