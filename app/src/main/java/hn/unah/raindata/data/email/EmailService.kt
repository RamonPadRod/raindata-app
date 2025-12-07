package hn.unah.raindata.data.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailService {

    private const val TAG = "EmailService"

    /**
     * Configuración de la sesión SMTP de Gmail
     */
    private fun createEmailSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.host", EmailConfig.SMTP_HOST)
            put("mail.smtp.port", EmailConfig.SMTP_PORT)
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(
                    EmailConfig.EMAIL_FROM,
                    EmailConfig.EMAIL_PASSWORD
                )
            }
        })
    }

    /**
     * Función genérica para enviar emails
     */
    private suspend fun enviarEmail(
        destinatario: String,
        asunto: String,
        contenidoHTML: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando envío de email a: $destinatario")

            val session = createEmailSession()
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(EmailConfig.EMAIL_FROM, EmailConfig.EMAIL_FROM_NAME))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
                subject = asunto
                setContent(contenidoHTML, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d(TAG, "✅ Email enviado exitosamente a: $destinatario")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al enviar email: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ===== EMAILS ESPECÍFICOS =====

    /**
     * EMAIL 1: Bienvenida para Voluntario
     */
    suspend fun enviarEmailBienvenidaVoluntario(
        destinatario: String,
        nombreUsuario: String
    ): Result<Unit> {
        return enviarEmail(
            destinatario = destinatario,
            asunto = "¡Bienvenido a REVOCLIMAP!",
            contenidoHTML = EmailTemplates.emailBienvenidaVoluntario(nombreUsuario)
        )
    }

    /**
     * EMAIL 2: Bienvenida para Observador
     */
    suspend fun enviarEmailBienvenidaObservador(
        destinatario: String,
        nombreUsuario: String
    ): Result<Unit> {
        return enviarEmail(
            destinatario = destinatario,
            asunto = "¡Bienvenido a REVOCLIMAP!",
            contenidoHTML = EmailTemplates.emailBienvenidaObservador(nombreUsuario)
        )
    }

    /**
     * EMAIL 3: Aprobación de Administrador
     */
    suspend fun enviarEmailAprobacionAdmin(
        destinatario: String,
        nombreUsuario: String
    ): Result<Unit> {
        return enviarEmail(
            destinatario = destinatario,
            asunto = "✅ Solicitud de Administrador APROBADA",
            contenidoHTML = EmailTemplates.emailAprobacionAdmin(nombreUsuario)
        )
    }

    /**
     * Función helper para usar desde ViewModels
     * Maneja el resultado y ejecuta callbacks
     */
    suspend fun enviarEmailConCallback(
        tipo: TipoEmail,
        destinatario: String,
        nombreUsuario: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val resultado = when (tipo) {
            TipoEmail.BIENVENIDA_VOLUNTARIO -> enviarEmailBienvenidaVoluntario(destinatario, nombreUsuario)
            TipoEmail.BIENVENIDA_OBSERVADOR -> enviarEmailBienvenidaObservador(destinatario, nombreUsuario)
            TipoEmail.APROBACION_ADMIN -> enviarEmailAprobacionAdmin(destinatario, nombreUsuario)
        }

        resultado.fold(
            onSuccess = {
                Log.d(TAG, "✅ Email enviado correctamente")
                onSuccess()
            },
            onFailure = { error ->
                Log.e(TAG, "❌ Error al enviar email: ${error.message}")
                onError(error.message ?: "Error desconocido al enviar email")
            }
        )
    }

    /**
     * Enum para tipos de email
     */
    enum class TipoEmail {
        BIENVENIDA_VOLUNTARIO,
        BIENVENIDA_OBSERVADOR,
        APROBACION_ADMIN
    }
}