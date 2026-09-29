package com.example.data.domino

import kotlin.random.Random

/**
 * Capacidades y personalidades tácticas de los jugadores bots de Dominó.
 */
enum class BotCapability(
    val displayName: String,
    val shortName: String,
    val description: String,
    val badgeEmoji: String
) {
    AGRESIVO_PUNTOS(
        displayName = "Descargador Agresivo",
        shortName = "Agresivo",
        description = "Prioriza soltar fichas altas y dobles rápidamente para no cargar puntos.",
        badgeEmoji = "⚡"
    ),
    TRANQUE_ESTRATEGA(
        displayName = "Estratega de Tranca",
        shortName = "Trancador",
        description = "Monitorea la mesa y busca cerrar el juego cuando tiene pocos puntos en mano.",
        badgeEmoji = "🔒"
    ),
    COOPERATIVO_PAREJA(
        displayName = "Compañero Táctico",
        shortName = "Solidario",
        description = "Juega en equipo: apoya las salidas de su pareja y evita ahorcar sus números.",
        badgeEmoji = "🤝"
    ),
    MAESTRO_CALCULADOR(
        displayName = "Maestro Analítico",
        shortName = "Maestro",
        description = "Lleva la cuenta de fichas jugadas, calcula probabilidades y maximiza opciones futuras.",
        badgeEmoji = "🧠"
    ),
    EQUILIBRADO_CLASICO(
        displayName = "Clásico Tradicional",
        shortName = "Clásico",
        description = "Estilo clásico y consistente: asegura dobles temprano y mantiene balance.",
        badgeEmoji = "⚖️"
    ),
    CONSERVADOR_DEFENSIVO(
        displayName = "Defensivo Prudente",
        shortName = "Defensivo",
        description = "Conserva versatilidad de palos y cuida las espaldas para no dar puntos al rival.",
        badgeEmoji = "🛡️"
    ),
    IMPREDECIBLE_AUDAZ(
        displayName = "Jugador Audaz",
        shortName = "Audaz",
        description = "Arriesga con jugadas sorpresivas, retiene dobles para remate y busca la capicúa.",
        badgeEmoji = "🔥"
    ),
    REMATE_VELOZ(
        displayName = "Rematador Veloz",
        shortName = "Rematador",
        description = "Especialista en cierres rápidos y mantener la mano corta.",
        badgeEmoji = "🎯"
    ),
    CAUTELOSO_PACIENTE(
        displayName = "Defensor Paciente",
        shortName = "Paciente",
        description = "Espera el momento exacto y fuerza el paso y castigo de los rivales.",
        badgeEmoji = "🧘"
    ),
    CAZADOR_DOBLES(
        displayName = "Cazador de Dobles",
        shortName = "Cazadobles",
        description = "Presiona ahorcando números para neutralizar las fichas dobles del rival.",
        badgeEmoji = "⚡"
    )
}

data class BotProfile(
    val id: String,
    val name: String,
    val capability: BotCapability,
    val originCity: String,
    val avatarEmoji: String,
    val avatarColorIndex: Int
)

object BotRoster {
    // Catálogo extenso de más de 270 nombres y apodos típicos del dominó caribeño y latinoamericano
    private val rawBotNames = listOf(
        "Don Cheo", "Doña Carmen", "Tito El Gallo", "El Pana Javi", "La Guajira",
        "El Samurai", "Maestro Chente", "El Zurdo", "Abuelo Ramón", "Yulimar",
        "El Tigre", "Beto", "Camila", "Mateo", "Valentina",
        "Panchito", "Nico", "Sofía", "El Barbero", "Yadira",
        "El Capitán", "Carlitos", "Chucho", "Daniela", "Gabo",
        "Rafa", "Kenia", "El Pollo", "Sra. Mercedes", "Moisés",
        "Dayana", "Facundo", "Thiago", "Isabella", "Cheché",
        "Manolo", "Renata", "Lucho", "Cachito", "Tía Olga",
        "Pablito", "Caridad", "Felo", "Toñito", "La Catira",
        "Moncho", "Yosneidy", "El Chamo", "Guillermo", "Milagros",
        "El Mocho", "Nelson", "Luzmila", "Papi Chulo", "Enrique",
        "Marisela", "César", "Soraya", "Alfonso", "Xiomara",
        "Leandro", "Nayarith", "Gerson", "Beatriz", "Kike",
        "Estela", "Romer", "Génesis", "Darwin", "Morella",
        "Elio", "Flor", "Wilmer", "Yaritza", "Argenis",
        "Coromoto", "Brayan", "Suleidy", "Kelvin", "Mireya",
        "Oswaldo", "Gladys", "Jhonny", "Norkys", "Alexis",
        "Zulima", "Ender", "Belkis", "Franklin", "Chiquinquirá",
        "Reinaldo", "Lourdes", "Gustavo", "Esperanza", "Ignacio",
        "Amparo", "Silvio", "Consuelo", "Régulo", "Herminia",
        "Wladimir", "Haydée", "Braulio", "Dora", "Orlando",
        "Trina", "Héctor", "Rosalía", "Vicente", "Elba",
        "Saúl", "Norma", "Alí", "Cándida", "Román",
        "Ligia", "Eulogio", "Deyanira", "Homero", "Lérida",
        "Félix", "Iris", "Nestor", "Magaly", "Benito",
        "Aura", "Florángel", "Aquiles", "Nelly", "Lisandro",
        "Solángel", "Juvenal", "Omayra", "Severo", "Yuraima",
        "Crispín", "Hildamar", "Evaristo", "Gisela", "Toribio",
        "Marisol", "Faustino", "Yasmín", "Gumersindo", "Tibisay",
        "Plácido", "Yamilet", "Aniceto", "Ingrid", "Ceferino",
        "Yomaira", "Telesforo", "Astrid", "Primitivo", "Maritza",
        "Hermógenes", "Aída", "Leocadio", "Vilma", "Apolinar",
        "Zoraida", "Salomón", "Yolanda", "Tiburcio", "Marina",
        "Robustiano", "Griselda", "Eustaquio", "Minerva", "Teófilo",
        "Eneida", "Pánfilo", "Livia", "Bonifacio", "Maribel",
        "Venancio", "Marlene", "Policarpo", "Glenda", "Epifanio",
        "Norelys", "Silverio", "Mayra", "Eleuterio", "Mildred",
        "Celestino", "Yelitza", "Macario", "Omaira", "Demetrio",
        "Cora", "Dionisio", "Pastora", "Heriberto", "Clemencia",
        "Elio José", "Zenaida", "Nabor", "Teodolinda", "Urbano",
        "Bertila", "Wenceslao", "Petronila", "Rufino", "Emerita",
        "Cipriano", "Nieves", "Bernardo", "Dulce", "Aureliano",
        "El Mago del Tranque", "La Fiera de Maracaibo", "Don Pascual", "Doña Rosaura", "El Mariscal",
        "Chicho Terremoto", "El Brujo", "La Catedrática", "Compadre Pancho", "Tato",
        "El Chato", "Mingo", "El Relámpago", "Cheo Mandarria", "La Güera",
        "Don Efraín", "El Chino", "Pepe El Duro", "La Comadre Juana", "El Profe Martínez",
        "El Galeno", "Nando", "Toño Candela", "El Coronel", "Doña Florinda",
        "El Capi", "La Tormenta", "Don Vicente", "Guille", "El Bachiller",
        "La Sultana", "Pellín", "El Compá", "Macho Prieto", "La Reina de Copas",
        "El Zorro", "Don Melquiades", "Chiche", "Monchito", "La Consentida",
        "El Gavilán", "Pachín", "El Duende", "Doña Tomasa", "El Bate",
        "Tatico", "La Madrina", "Don Silvio", "El Teniente", "La Flaca",
        "El Negrito", "Don Arturo", "Papo El Bravo", "La Patrona", "El Guajiro",
        "Doña Clotilde", "El Rumbero", "Chano", "Don Leopoldo", "La Doña",
        "El Flaco", "El Maza", "Don Bartolo", "La Comadrita", "El Campeón",
        "Pedrito La Roca", "Doña Leonor", "El León del Zulia", "El Lobo", "Don Casimiro",
        "La Maestra Elena", "El Huracán", "Don Valerio", "Tito Candela", "La Nena",
        "Don Justo", "El Centauro", "El Indio", "Doña Ramona", "El Chacal",
        "El Gavilán Pollero", "La Maracucha", "El Poeta", "Don Baldomero", "El Ciclón",
        "Doña Berta", "El Rayo", "Don Cosme", "La Criolla", "El Halcón",
        "Don Cecilio", "La Pinta", "El Cacique", "Don Sixto", "La Guara",
        "El General", "Don Modesto", "La Criollita", "El Trueno", "Don Higinio",
        "El Diamante", "Don Nicanor", "El As del Dominó", "Doña Virginia", "El Gran Maestro",
        "Don Timoteo", "La Elegante", "El Padrino", "Don Eleazar", "La Dama del Tranque",
        "El Invencible", "Don Filemón", "La Leona", "El Titán", "Don Fortunato",
        "La Pantera", "El Sabio", "Don Gualberto", "La Fénix", "El Guerrero",
        "Don Heliodoro", "La Estrella", "El Guardián", "Don Ismael", "La Campeona",
        "El Conquistador", "Don Jacinto", "La Diosa", "El Protector", "Don Laureano",
        "La Furia", "El Estratega", "Don Maximiliano", "La Saeta", "El Mágico",
        "Don Nazario", "La Dinamita", "Don Odilón", "La Centella", "El Destructor",
        "Don Pantaleón", "La Vorágine", "El Centinela", "Don Quintín", "La Maravilla",
        "El Rayo Veloz", "Don Romualdo", "La Maquinaria", "El Cañón", "Don Sabas",
        "La Muralla", "El Fénix Dorado", "Don Telmo", "La Cuchilla", "Don Ubaldo",
        "La Candela", "El Vencedor", "Don Valeriano", "La Chispa", "El Zafiro",
        "Don Wilfredo", "La Perla", "El Gladiador", "Don Zacarias", "La Joya",
        "El Cóndor", "Don Ananías", "La Joyita", "El Halcón Peregrino", "Don Balbino",
        "La Princesa", "El Gavilán Dorado", "Don Crisóstomo", "La Soberana", "El Dragón",
        "Don Dalmacio", "La Mariposa", "El Águila Real", "Don Erasmo", "La Sirena",
        "El Cazador", "Don Froilán", "La Flecha", "El Halcón Azul", "Don Gaudencio",
        "La Diosa del Dominó", "El Pájaro Cantor", "Don Heraclio", "La Rosa Blanca", "El Rayo Plateado",
        "Don Indalecio", "La Azucena", "El Viento del Sur", "Don Jovito", "La Palmera",
        "El Rey de la Mesa", "Don Ladislao", "La Orquídea", "El Soberano", "Don Maclovio",
        "La Ceiba", "El Patriarca", "Don Nicasio", "La Araguaney", "El Cacique Mayor",
        "Don Onofre", "La Turpial", "El Tigre de Oriente", "Don Próspero", "La Llovizna",
        "El Relámpago del Catatumbo", "Don Quirino", "La Neblina", "El Centinela del Llano", "Don Remigio",
        "La Brisa", "El Samán", "Don Saturnino", "La Tormenta Tropical", "El Cardenal",
        "Don Torcuato", "La Llanera", "El Guácharo", "Don Ulpiano", "La Caraqueña",
        "El Cóndor Andino", "Don Venancio", "La Zuliana", "El Chigüire", "Don Wenceslao",
        "La Oriental", "El Cunaguaro", "Don Xenón", "La Guayanesa", "El Araguato",
        "Don Yahel", "La Andina", "El Picaflor", "Don Zabulón", "La Costeña", "El Colibrí"
    )

    private val cities = listOf(
        "Caracas", "Maracaibo", "San Juan", "La Habana", "Santo Domingo",
        "Cali", "Medellín", "Valencia", "Bogotá", "Barquisimeto",
        "Ponce", "Santiago de Cuba", "Cartagena", "Maracay", "Mayagüez",
        "Barranquilla", "San Cristóbal", "Caguas", "Mérida", "Puerto La Cruz",
        "Holguín", "Cienfuegos", "La Guaira", "Camagüey", "Bayamo",
        "Margarita", "Guatire", "Bayamón", "Ciudad Guayana", "Arecibo",
        "Coro", "Cumaná", "Trujillo", "Cabimas", "Barinas",
        "Santa Marta", "Bucaramanga", "Matanzas", "Santa Clara", "Pinar del Río",
        "Guayama", "Carolina", "Fajardo", "Aguadilla", "San Pedro de Macorís",
        "Santiago de los Caballeros", "La Romana", "Puerto Plata", "San Francisco de Macorís"
    )

    private val botEmojis = listOf(
        "🤠", "😎", "🎩", "🧔", "👵", "👴", "🦊", "🐯", "🦁", "🦅",
        "👑", "🎲", "🎯", "🤹", "🧑‍✈️", "👩‍🌾", "🕵️", "🧑‍🍳", "👨‍🏫", "👩‍🎨",
        "⚡", "🔥", "🧠", "⚔️", "🏆", "🌟", "🦜", "🦈", "🦚", "🐆"
    )

    /**
     * Genera bots aleatorios garantizando nombres y personalidades únicas.
     */
    fun getRandomBots(
        count: Int,
        isTeams: Boolean = false,
        excludeNames: Set<String> = emptySet()
    ): List<BotProfile> {
        val availableNames = rawBotNames.filterNot { excludeNames.contains(it) }.shuffled()
        val allCapabilities = BotCapability.entries.toTypedArray()
        val shuffledCities = cities.shuffled()
        val shuffledEmojis = botEmojis.shuffled()

        val results = mutableListOf<BotProfile>()
        for (i in 0 until count) {
            val name = availableNames.getOrElse(i) { "Bot ${Random.nextInt(100, 999)}" }
            
            // Asignar capacidad con lógica balanceada para equipos:
            // En modo parejas (2 vs 2), el índice 1 (compañero, index 2 en jugadores) se beneficia de ser Cooperativo o Maestro
            val capability = if (isTeams && i == 1) {
                listOf(
                    BotCapability.COOPERATIVO_PAREJA,
                    BotCapability.MAESTRO_CALCULADOR,
                    BotCapability.EQUILIBRADO_CLASICO,
                    BotCapability.CONSERVADOR_DEFENSIVO
                ).random()
            } else {
                allCapabilities.random()
            }

            val city = shuffledCities.getOrElse(i % shuffledCities.size) { "Caribe" }
            val emoji = shuffledEmojis.getOrElse(i % shuffledEmojis.size) { "🎲" }

            results.add(
                BotProfile(
                    id = "bot_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}_$i",
                    name = "$name (Bot)",
                    capability = capability,
                    originCity = city,
                    avatarEmoji = emoji,
                    avatarColorIndex = (i + 1) % 4
                )
            )
        }
        return results
    }
}
