package hn.unah.raindata.data.email

object EmailTemplates {

    // ===== COLORES CORPORATIVOS =====
    private const val COLOR_VERDE_PRINCIPAL = "#2E7D32"
    private const val COLOR_VERDE_CLARO = "#66BB6A"
    private const val COLOR_AZUL_CIELO = "#4FC3F7"
    private const val COLOR_AMARILLO = "#FDD835"
    private const val COLOR_BLANCO = "#FFFFFF"
    private const val COLOR_GRIS_TEXTO = "#424242"

    // ===== TEMPLATE BASE HTML =====
    private fun baseTemplate(
        titulo: String,
        contenido: String,
        textoBoton: String = "Abrir REVOCLIMAP",
        mostrarBoton: Boolean = true
    ): String {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$titulo</title>
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td align="center" style="padding: 40px 20px;">
                <!-- CONTENEDOR PRINCIPAL -->
                <table role="presentation" style="width: 100%; max-width: 600px; background-color: $COLOR_BLANCO; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                    
                    <!-- HEADER CON LOGO -->
                    <tr>
                        <td style="background: linear-gradient(135deg, $COLOR_VERDE_PRINCIPAL 0%, $COLOR_VERDE_CLARO 100%); padding: 40px 30px; text-align: center; border-radius: 16px 16px 0 0;">
                            <!-- LOGO CIRCULAR -->
                            <div style="background-color: $COLOR_AMARILLO; width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 8px rgba(0,0,0,0.2);">
                                <span style="font-size: 40px; color: $COLOR_VERDE_PRINCIPAL;">☁️</span>
                            </div>
                            
                            <h1 style="margin: 0; color: $COLOR_BLANCO; font-size: 28px; font-weight: bold; text-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                                $titulo
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- CONTENIDO -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            $contenido
                        </td>
                    </tr>
                    
                    ${if (mostrarBoton) """
                    <!-- BOTÓN -->
                    <tr>
                        <td style="padding: 0 30px 40px;">
                            <table role="presentation" style="width: 100%;">
                                <tr>
                                    <td align="center">
                                        <a href="${EmailConfig.APP_LINK}" style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, $COLOR_VERDE_PRINCIPAL 0%, $COLOR_VERDE_CLARO 100%); color: $COLOR_BLANCO; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3); transition: transform 0.2s;">
                                            🚀 $textoBoton
                                        </a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    """ else ""}
                    
                    <!-- FOOTER -->
                    <tr>
                        <td style="background-color: #f9f9f9; padding: 30px; text-align: center; border-radius: 0 0 16px 16px; border-top: 3px solid $COLOR_VERDE_CLARO;">
                            <p style="margin: 0 0 10px; color: $COLOR_VERDE_PRINCIPAL; font-weight: bold; font-size: 16px;">
                                Mesa Agroclimática del Paraíso
                            </p>
                            <p style="margin: 0 0 5px; color: $COLOR_GRIS_TEXTO; font-size: 14px;">
                                Red Comunitaria de Observadores del Clima
                            </p>
                            <p style="margin: 0; color: #757575; font-size: 12px; font-style: italic;">
                                CIENCIA CLIMÁTICA AL SERVICIO DEL CAMPO Y LA COMUNIDAD
                            </p>
                            
                            <!-- Logo pequeño al pie -->
                            <div style="margin-top: 20px;">
                                <span style="font-size: 24px;">🌧️💧🌿</span>
                            </div>
                        </td>
                    </tr>
                    
                </table>
                
                <!-- TEXTO LEGAL PEQUEÑO -->
                <p style="margin-top: 20px; color: #9e9e9e; font-size: 12px; text-align: center; max-width: 600px;">
                    Este es un correo automático de ${EmailConfig.APP_NAME}. Por favor no respondas a este mensaje.
                </p>
            </td>
        </tr>
    </table>
</body>
</html>
        """.trimIndent()
    }

    // ===== EMAIL 1: BIENVENIDA VOLUNTARIO =====
    fun emailBienvenidaVoluntario(nombreUsuario: String): String {
        val contenido = """
            <p style="margin: 0 0 20px; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                ¡Hola! 👋
            </p>
            
            <p style="margin: 0 0 20px; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                Nos alegra que formes parte de la <strong>Red Comunitaria de Observadores del Clima</strong> 
                de la Mesa Agroclimática del Paraíso.
            </p>
            
            <div style="background-color: #E8F5E9; padding: 20px; border-radius: 12px; border-left: 4px solid $COLOR_VERDE_PRINCIPAL; margin: 20px 0;">
                <p style="margin: 0 0 10px; color: $COLOR_VERDE_PRINCIPAL; font-weight: bold; font-size: 18px;">
                    ✅ Tu cuenta de VOLUNTARIO ha sido activada
                </p>
                <p style="margin: 0; color: $COLOR_GRIS_TEXTO; font-size: 14px;">
                    Ya puedes iniciar sesión en la aplicación y comenzar a registrar datos meteorológicos.
                </p>
            </div>
            
            <p style="margin: 20px 0 0; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                Gracias por tu compromiso con el monitoreo climático de nuestra comunidad. 🌧️
            </p>
        """

        return baseTemplate(
            titulo = "¡Bienvenido a REVOCLIMAP!",
            contenido = contenido,
            textoBoton = "Abrir REVOCLIMAP"
        )
    }

    // ===== EMAIL 2: BIENVENIDA OBSERVADOR =====
    fun emailBienvenidaObservador(nombreUsuario: String): String {
        val contenido = """
            <p style="margin: 0 0 20px; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                ¡Hola! 👋
            </p>
            
            <p style="margin: 0 0 20px; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                Nos alegra que formes parte de la <strong>Red Comunitaria de Observadores del Clima</strong> 
                de la Mesa Agroclimática del Paraíso.
            </p>
            
            <div style="background-color: #E3F2FD; padding: 20px; border-radius: 12px; border-left: 4px solid $COLOR_AZUL_CIELO; margin: 20px 0;">
                <p style="margin: 0 0 10px; color: #0277BD; font-weight: bold; font-size: 18px;">
                    ✅ Tu cuenta de OBSERVADOR ha sido activada
                </p>
                <p style="margin: 0; color: $COLOR_GRIS_TEXTO; font-size: 14px;">
                    Ya puedes iniciar sesión en la aplicación para consultar los datos meteorológicos registrados.
                </p>
            </div>
            
            <p style="margin: 20px 0 0; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                Gracias por tu interés en el monitoreo climático de nuestra comunidad. 📊
            </p>
        """

        return baseTemplate(
            titulo = "¡Bienvenido a REVOCLIMAP!",
            contenido = contenido,
            textoBoton = "Abrir REVOCLIMAP"
        )
    }

    // ===== EMAIL 3: APROBACIÓN ADMINISTRADOR =====
    fun emailAprobacionAdmin(nombreUsuario: String): String {
        val contenido = """
            <p style="margin: 0 0 20px; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                ¡Felicidades! 🎉
            </p>
            
            <div style="background-color: #FFF3E0; padding: 20px; border-radius: 12px; border-left: 4px solid $COLOR_AMARILLO; margin: 20px 0;">
                <p style="margin: 0 0 10px; color: #F57C00; font-weight: bold; font-size: 18px;">
                    ✅ Tu solicitud de ADMINISTRADOR ha sido aprobada
                </p>
                <p style="margin: 0; color: $COLOR_GRIS_TEXTO; font-size: 14px;">
                    Ya puedes iniciar sesión con permisos completos de administrador.
                </p>
            </div>
            
            <p style="margin: 20px 0 0; color: $COLOR_GRIS_TEXTO; font-size: 16px; line-height: 1.6;">
                Gracias por tu compromiso con la Mesa Agroclimática del Paraíso. 🌟
            </p>
        """

        return baseTemplate(
            titulo = "¡Solicitud Aprobada!",
            contenido = contenido,
            textoBoton = "Iniciar Sesión"
        )
    }
}