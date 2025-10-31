package hn.unah.raindata.data.utils

object DepartamentosHonduras {

    // ============================================
    // CÓDIGOS DE DEPARTAMENTOS (01-18)
    // ============================================
    val codigosDepartamentos = mapOf(
        "Atlántida" to "01",
        "Colón" to "02",
        "Comayagua" to "03",
        "Copán" to "04",
        "Cortés" to "05",
        "Choluteca" to "06",
        "El Paraíso" to "07",
        "Francisco Morazán" to "08",
        "Gracias a Dios" to "09",
        "Intibucá" to "10",
        "Islas de la Bahía" to "11",
        "La Paz" to "12",
        "Lempira" to "13",
        "Ocotepeque" to "14",
        "Olancho" to "15",
        "Santa Bárbara" to "16",
        "Valle" to "17",
        "Yoro" to "18"
    )

    val departamentos = listOf(
        "Atlántida",
        "Colón",
        "Comayagua",
        "Copán",
        "Cortés",
        "Choluteca",
        "El Paraíso",
        "Francisco Morazán",
        "Gracias a Dios",
        "Intibucá",
        "Islas de la Bahía",
        "La Paz",
        "Lempira",
        "Ocotepeque",
        "Olancho",
        "Santa Bárbara",
        "Valle",
        "Yoro"
    )

    // ============================================
    // CÓDIGOS DE MUNICIPIOS POR DEPARTAMENTO
    // ============================================
    val codigosMunicipios = mapOf(
        // ATLÁNTIDA (01)
        "Atlántida" to mapOf(
            "La Ceiba" to "01",
            "El Porvenir" to "02",
            "Esparta" to "03",
            "Jutiapa" to "04",
            "La Masica" to "05",
            "San Francisco" to "06",
            "Tela" to "07",
            "Arizona" to "08"
        ),

        // COLÓN (02)
        "Colón" to mapOf(
            "Trujillo" to "01",
            "Balfate" to "02",
            "Iriona" to "03",
            "Limón" to "04",
            "Sabá" to "05",
            "Santa Fe" to "06",
            "Santa Rosa de Aguán" to "07",
            "Sonaguera" to "08",
            "Tocoa" to "09",
            "Bonito Oriental" to "10"
        ),

        // COMAYAGUA (03)
        "Comayagua" to mapOf(
            "Comayagua" to "01",
            "Ajuterique" to "02",
            "El Rosario" to "03",
            "Esquías" to "04",
            "Humuya" to "05",
            "La Libertad" to "06",
            "Lamaní" to "07",
            "La Trinidad" to "08",
            "Lejamaní" to "09",
            "Meámbar" to "10",
            "Minas de Oro" to "11",
            "Ojos de Agua" to "12",
            "San Jerónimo" to "13",
            "San José de Comayagua" to "14",
            "San José del Potrero" to "15",
            "San Luis" to "16",
            "San Sebastián" to "17",
            "Siguatepeque" to "18",
            "Villa de San Antonio" to "19",
            "Las Lajas" to "20",
            "Taulabé" to "21"
        ),

        // COPÁN (04)
        "Copán" to mapOf(
            "Santa Rosa de Copán" to "01",
            "Cabañas" to "02",
            "Concepción" to "03",
            "Copán Ruinas" to "04",
            "Corquín" to "05",
            "Cucuyagua" to "06",
            "Dolores" to "07",
            "Dulce Nombre" to "08",
            "El Paraíso" to "09",
            "Florida" to "10",
            "La Jigua" to "11",
            "La Unión" to "12",
            "Nueva Arcadia" to "13",
            "San Agustín" to "14",
            "San Antonio" to "15",
            "San Jerónimo" to "16",
            "San José" to "17",
            "San Juan de Opoa" to "18",
            "San Nicolás" to "19",
            "San Pedro" to "20",
            "Santa Rita" to "21",
            "Trinidad de Copán" to "22",
            "Veracruz" to "23"
        ),

        // CORTÉS (05)
        "Cortés" to mapOf(
            "San Pedro Sula" to "01",
            "Choloma" to "02",
            "Omoa" to "03",
            "Pimienta" to "04",
            "Potrerillos" to "05",
            "Puerto Cortés" to "06",
            "San Antonio de Cortés" to "07",
            "San Francisco de Yojoa" to "08",
            "San Manuel" to "09",
            "Santa Cruz de Yojoa" to "10",
            "Villanueva" to "11",
            "La Lima" to "12"
        ),

        // CHOLUTECA (06)
        "Choluteca" to mapOf(
            "Choluteca" to "01",
            "Apacilagua" to "02",
            "Concepción de María" to "03",
            "Duyure" to "04",
            "El Corpus" to "05",
            "El Triunfo" to "06",
            "Marcovia" to "07",
            "Morolica" to "08",
            "Namasigüe" to "09",
            "Orocuina" to "10",
            "Pespire" to "11",
            "San Antonio de Flores" to "12",
            "San Isidro" to "13",
            "San José" to "14",
            "San Marcos de Colón" to "15",
            "Santa Ana de Yusguare" to "16"
        ),

        // EL PARAÍSO (07)
        "El Paraíso" to mapOf(
            "Yuscarán" to "01",
            "Alauca" to "02",
            "Danlí" to "03",
            "El Paraíso" to "04",
            "Güinope" to "05",
            "Jacaleapa" to "06",
            "Liure" to "07",
            "Morocelí" to "08",
            "Oropolí" to "09",
            "Potrerillos" to "10",
            "San Antonio de Flores" to "11",
            "San Lucas" to "12",
            "San Matías" to "13",
            "Soledad" to "14",
            "Teupasenti" to "15",
            "Texiguat" to "16",
            "Trojes" to "17",
            "Vado Ancho" to "18",
            "Yauyupe" to "19"
        ),

        // FRANCISCO MORAZÁN (08)
        "Francisco Morazán" to mapOf(
            "Distrito Central" to "01",
            "Alubarén" to "02",
            "Cedros" to "03",
            "Curarén" to "04",
            "El Porvenir" to "05",
            "Guaimaca" to "06",
            "La Libertad" to "07",
            "La Venta" to "08",
            "Lepaterique" to "09",
            "Maraita" to "10",
            "Marale" to "11",
            "Nueva Armenia" to "12",
            "Ojojona" to "13",
            "Orica" to "14",
            "Reitoca" to "15",
            "Sabanagrande" to "16",
            "San Antonio de Oriente" to "17",
            "San Buenaventura" to "18",
            "San Ignacio" to "19",
            "San Juan de Flores" to "20",
            "San Miguelito" to "21",
            "Santa Ana" to "22",
            "Santa Lucía" to "23",
            "Talanga" to "24",
            "Tatumbla" to "25",
            "Valle de Ángeles" to "26",
            "Villa de San Francisco" to "27",
            "Vallecillo" to "28"
        ),

        // GRACIAS A DIOS (09)
        "Gracias a Dios" to mapOf(
            "Puerto Lempira" to "01",
            "Brus Laguna" to "02",
            "Ahuas" to "03",
            "Juan Francisco Bulnes" to "04",
            "Ramón Villeda Morales" to "05",
            "Wampusirpi" to "06"
        ),

        // INTIBUCÁ (10)
        "Intibucá" to mapOf(
            "La Esperanza" to "01",
            "Camasca" to "02",
            "Colomoncagua" to "03",
            "Concepción" to "04",
            "Dolores" to "05",
            "Intibucá" to "06",
            "Jesús de Otoro" to "07",
            "Magdalena" to "08",
            "Masaguara" to "09",
            "San Antonio" to "10",
            "San Isidro" to "11",
            "San Juan" to "12",
            "San Marcos de la Sierra" to "13",
            "San Miguelito" to "14",
            "Santa Lucía" to "15",
            "Yamaranguila" to "16",
            "San Francisco de Opalaca" to "17"
        ),

        // ISLAS DE LA BAHÍA (11)
        "Islas de la Bahía" to mapOf(
            "Roatán" to "01",
            "José Santos Guardiola" to "02",
            "Utila" to "03",
            "Guanaja" to "04"
        ),

        // LA PAZ (12)
        "La Paz" to mapOf(
            "La Paz" to "01",
            "Aguanqueterique" to "02",
            "Cabañas" to "03",
            "Cane" to "04",
            "Chinacla" to "05",
            "Guajiquiro" to "06",
            "Lauterique" to "07",
            "Marcala" to "08",
            "Mercedes de Oriente" to "09",
            "Opatoro" to "10",
            "San Antonio del Norte" to "11",
            "San José" to "12",
            "San Juan" to "13",
            "San Pedro de Tutule" to "14",
            "Santa Ana" to "15",
            "Santa Elena" to "16",
            "Santa María" to "17",
            "Santiago de Puringla" to "18",
            "Yarula" to "19"
        ),

        // LEMPIRA (13)
        "Lempira" to mapOf(
            "Gracias" to "01",
            "Belén" to "02",
            "Candelaria" to "03",
            "Cololaca" to "04",
            "Erandique" to "05",
            "Gualcince" to "06",
            "Guarita" to "07",
            "La Campa" to "08",
            "La Iguala" to "09",
            "Las Flores" to "10",
            "La Unión" to "11",
            "La Virtud" to "12",
            "Lepaera" to "13",
            "Mapulaca" to "14",
            "Piraera" to "15",
            "San Andrés" to "16",
            "San Francisco" to "17",
            "San Juan Guarita" to "18",
            "San Manuel Colohete" to "19",
            "San Rafael" to "20",
            "San Sebastián" to "21",
            "Santa Cruz" to "22",
            "Talgua" to "23",
            "Tambla" to "24",
            "Tomalá" to "25",
            "Valladolid" to "26",
            "Virginia" to "27",
            "San Marcos de Caiquín" to "28"
        ),

        // OCOTEPEQUE (14)
        "Ocotepeque" to mapOf(
            "Ocotepeque" to "01",
            "Belén Gualcho" to "02",
            "Concepción" to "03",
            "Dolores Merendón" to "04",
            "Fraternidad" to "05",
            "La Encarnación" to "06",
            "La Labor" to "07",
            "Lucerna" to "08",
            "Mercedes" to "09",
            "San Fernando" to "10",
            "San Francisco del Valle" to "11",
            "San Jorge" to "12",
            "San Marcos" to "13",
            "Santa Fe" to "14",
            "Sensenti" to "15",
            "Sinuapa" to "16"
        ),

        // OLANCHO (15)
        "Olancho" to mapOf(
            "Juticalpa" to "01",
            "Campamento" to "02",
            "Catacamas" to "03",
            "Concordia" to "04",
            "Dulce Nombre de Culmí" to "05",
            "El Rosario" to "06",
            "Esquipulas del Norte" to "07",
            "Gualaco" to "08",
            "Guarizama" to "09",
            "Guata" to "10",
            "Guayape" to "11",
            "Jano" to "12",
            "La Unión" to "13",
            "Mangulile" to "14",
            "Manto" to "15",
            "Patuca" to "16",
            "Salamá" to "17",
            "San Esteban" to "18",
            "San Francisco de Becerra" to "19",
            "San Francisco de la Paz" to "20",
            "Santa María del Real" to "21",
            "Silca" to "22",
            "Yocón" to "23"
        ),

        // SANTA BÁRBARA (16)
        "Santa Bárbara" to mapOf(
            "Santa Bárbara" to "01",
            "Arada" to "02",
            "Atima" to "03",
            "Azacualpa" to "04",
            "Ceguaca" to "05",
            "Concepción del Norte" to "06",
            "Concepción del Sur" to "07",
            "Chinda" to "08",
            "El Níspero" to "09",
            "Gualala" to "10",
            "Ilama" to "11",
            "Las Vegas" to "12",
            "Macuelizo" to "13",
            "Naranjito" to "14",
            "Nuevo Celilac" to "15",
            "Petoa" to "16",
            "Protección" to "17",
            "Quimistán" to "18",
            "San Francisco de Ojuera" to "19",
            "San José de las Colinas" to "20",
            "San Luis" to "21",
            "San Marcos" to "22",
            "San Nicolás" to "23",
            "San Pedro Zacapa" to "24",
            "Santa Rita" to "25",
            "Trinidad" to "26",
            "Las Flores" to "27",
            "Araminda" to "28"
        ),

        // VALLE (17)
        "Valle" to mapOf(
            "Nacaome" to "01",
            "Alianza" to "02",
            "Amapala" to "03",
            "Aramecina" to "04",
            "Caridad" to "05",
            "Goascorán" to "06",
            "Langue" to "07",
            "San Francisco de Coray" to "08",
            "San Lorenzo" to "09"
        ),

        // YORO (18)
        "Yoro" to mapOf(
            "Yoro" to "01",
            "Arenal" to "02",
            "El Negrito" to "03",
            "El Progreso" to "04",
            "Jocón" to "05",
            "Morazán" to "06",
            "Olanchito" to "07",
            "Santa Rita" to "08",
            "Sulaco" to "09",
            "Victoria" to "10",
            "Yorito" to "11"
        )
    )

    val municipiosPorDepartamento = mapOf(
        "Atlántida" to listOf(
            "La Ceiba", "El Porvenir", "Esparta", "Jutiapa",
            "La Masica", "San Francisco", "Tela", "Arizona"
        ),
        "Colón" to listOf(
            "Trujillo", "Balfate", "Iriona", "Limón", "Sabá",
            "Santa Fe", "Santa Rosa de Aguán", "Sonaguera", "Tocoa", "Bonito Oriental"
        ),
        "Comayagua" to listOf(
            "Comayagua", "Ajuterique", "El Rosario", "Esquías", "Humuya",
            "La Libertad", "Lamaní", "La Trinidad", "Lejamaní", "Meámbar",
            "Minas de Oro", "Ojos de Agua", "San Jerónimo", "San José de Comayagua",
            "San José del Potrero", "San Luis", "San Sebastián", "Siguatepeque",
            "Villa de San Antonio", "Las Lajas", "Taulabé"
        ),
        "Copán" to listOf(
            "Santa Rosa de Copán", "Cabañas", "Concepción", "Copán Ruinas", "Corquín",
            "Cucuyagua", "Dolores", "Dulce Nombre", "El Paraíso", "Florida",
            "La Jigua", "La Unión", "Nueva Arcadia", "San Agustín", "San Antonio",
            "San Jerónimo", "San José", "San Juan de Opoa", "San Nicolás", "San Pedro",
            "Santa Rita", "Trinidad de Copán", "Veracruz"
        ),
        "Cortés" to listOf(
            "San Pedro Sula", "Choloma", "Omoa", "Pimienta", "Potrerillos",
            "Puerto Cortés", "San Antonio de Cortés", "San Francisco de Yojoa",
            "San Manuel", "Santa Cruz de Yojoa", "Villanueva", "La Lima"
        ),
        "Choluteca" to listOf(
            "Choluteca", "Apacilagua", "Concepción de María", "Duyure", "El Corpus",
            "El Triunfo", "Marcovia", "Morolica", "Namasigüe", "Orocuina",
            "Pespire", "San Antonio de Flores", "San Isidro", "San José",
            "San Marcos de Colón", "Santa Ana de Yusguare"
        ),
        "El Paraíso" to listOf(
            "Yuscarán", "Alauca", "Danlí", "El Paraíso", "Güinope",
            "Jacaleapa", "Liure", "Morocelí", "Oropolí", "Potrerillos",
            "San Antonio de Flores", "San Lucas", "San Matías", "Soledad",
            "Teupasenti", "Texiguat", "Trojes", "Vado Ancho", "Yauyupe"
        ),
        "Francisco Morazán" to listOf(
            "Distrito Central", "Alubarén", "Cedros", "Curarén", "El Porvenir",
            "Guaimaca", "La Libertad", "La Venta", "Lepaterique", "Maraita",
            "Marale", "Nueva Armenia", "Ojojona", "Orica", "Reitoca",
            "Sabanagrande", "San Antonio de Oriente", "San Buenaventura", "San Ignacio",
            "San Juan de Flores", "San Miguelito", "Santa Ana", "Santa Lucía",
            "Talanga", "Tatumbla", "Valle de Ángeles", "Villa de San Francisco", "Vallecillo"
        ),
        "Gracias a Dios" to listOf(
            "Puerto Lempira", "Brus Laguna", "Ahuas", "Juan Francisco Bulnes",
            "Ramón Villeda Morales", "Wampusirpi"
        ),
        "Intibucá" to listOf(
            "La Esperanza", "Camasca", "Colomoncagua", "Concepción", "Dolores",
            "Intibucá", "Jesús de Otoro", "Magdalena", "Masaguara", "San Antonio",
            "San Isidro", "San Juan", "San Marcos de la Sierra", "San Miguelito",
            "Santa Lucía", "Yamaranguila", "San Francisco de Opalaca"
        ),
        "Islas de la Bahía" to listOf(
            "Roatán", "José Santos Guardiola", "Utila", "Guanaja"
        ),
        "La Paz" to listOf(
            "La Paz", "Aguanqueterique", "Cabañas", "Cane", "Chinacla",
            "Guajiquiro", "Lauterique", "Marcala", "Mercedes de Oriente", "Opatoro",
            "San Antonio del Norte", "San José", "San Juan", "San Pedro de Tutule",
            "Santa Ana", "Santa Elena", "Santa María", "Santiago de Puringla", "Yarula"
        ),
        "Lempira" to listOf(
            "Gracias", "Belén", "Candelaria", "Cololaca", "Erandique",
            "Gualcince", "Guarita", "La Campa", "La Iguala", "Las Flores",
            "La Unión", "La Virtud", "Lepaera", "Mapulaca", "Piraera",
            "San Andrés", "San Francisco", "San Juan Guarita", "San Manuel Colohete",
            "San Rafael", "San Sebastián", "Santa Cruz", "Talgua", "Tambla",
            "Tomalá", "Valladolid", "Virginia", "San Marcos de Caiquín"
        ),
        "Ocotepeque" to listOf(
            "Ocotepeque", "Belén Gualcho", "Concepción", "Dolores Merendón", "Fraternidad",
            "La Encarnación", "La Labor", "Lucerna", "Mercedes", "San Fernando",
            "San Francisco del Valle", "San Jorge", "San Marcos", "Santa Fe",
            "Sensenti", "Sinuapa"
        ),
        "Olancho" to listOf(
            "Juticalpa", "Campamento", "Catacamas", "Concordia", "Dulce Nombre de Culmí",
            "El Rosario", "Esquipulas del Norte", "Gualaco", "Guarizama", "Guata",
            "Guayape", "Jano", "La Unión", "Mangulile", "Manto",
            "Patuca", "Salamá", "San Esteban", "San Francisco de Becerra",
            "San Francisco de la Paz", "Santa María del Real", "Silca", "Yocón"
        ),
        "Santa Bárbara" to listOf(
            "Santa Bárbara", "Arada", "Atima", "Azacualpa", "Ceguaca",
            "Concepción del Norte", "Concepción del Sur", "Chinda", "El Níspero",
            "Gualala", "Ilama", "Las Vegas", "Macuelizo", "Naranjito",
            "Nuevo Celilac", "Petoa", "Protección", "Quimistán", "San Francisco de Ojuera",
            "San José de las Colinas", "San Luis", "San Marcos", "San Nicolás",
            "San Pedro Zacapa", "Santa Rita", "Trinidad", "Las Flores", "Araminda"
        ),
        "Valle" to listOf(
            "Nacaome", "Alianza", "Amapala", "Aramecina", "Caridad",
            "Goascorán", "Langue", "San Francisco de Coray", "San Lorenzo"
        ),
        "Yoro" to listOf(
            "Yoro", "Arenal", "El Negrito", "El Progreso", "Jocón",
            "Morazán", "Olanchito", "Santa Rita", "Sulaco", "Victoria", "Yorito"
        )
    )

    // ============================================
    // FUNCIONES DE UTILIDAD
    // ============================================

    fun obtenerMunicipios(departamento: String): List<String> {
        return municipiosPorDepartamento[departamento] ?: emptyList()
    }

    fun obtenerCodigoDepartamento(departamento: String): String? {
        return codigosDepartamentos[departamento]
    }

    fun obtenerCodigoMunicipio(departamento: String, municipio: String): String? {
        return codigosMunicipios[departamento]?.get(municipio)
    }

    fun generarCodigoPluviometro(departamento: String, municipio: String, secuencial: Int): String {
        val codigoDepto = obtenerCodigoDepartamento(departamento) ?: "00"
        val codigoMuni = obtenerCodigoMunicipio(departamento, municipio) ?: "00"
        val secuencialFormateado = String.format("%03d", secuencial)

        return "$codigoDepto-$codigoMuni-$secuencialFormateado"
    }
}