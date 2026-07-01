package hn.unah.raindata.data.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {
    /**
     * Determina si una excepción es un error de conectividad/red.
     * Cubre errores de Firebase, SSL, I/O, timeout, y otros fallos de conexión.
     */
    fun isConnectionError(e: Exception): Boolean {
        if (e is FirebaseNetworkException) return true
        if (e is ConnectException || e is SocketTimeoutException || e is UnknownHostException || e is IOException) return true
        
        if (e is FirebaseFirestoreException) {
            val code = e.code
            if (code == FirebaseFirestoreException.Code.UNAVAILABLE || 
                code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED) {
                return true
            }
        }

        val msg = e.message?.lowercase() ?: ""
        val cause = e.cause?.message?.lowercase() ?: ""
        val combined = "$msg $cause"

        val networkKeywords = listOf(
            "network", "ssl", "handshake", "connection reset", "connection refused",
            "unable to resolve host", "timeout", "timed out", "i/o error", "eof",
            "socket", "unreachable", "no route to host", "failed to connect",
            "client is offline", "offline"
        )

        return networkKeywords.any { combined.contains(it) }
    }
}
