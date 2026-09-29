package com.example.data.rules

enum class RegionalRuleSet(
    val id: String,
    val countryName: String,
    val flagEmoji: String,
    val description: String,
    val openingRuleDescription: String,
    val trancaRuleDescription: String,
    val capicuaBonusPoints: Int,
    val defaultTargetScore: Int,
    val allowsBoneyardDraw: Boolean
) {
    VENEZUELA(
        id = "venezuela",
        countryName = "Venezuela",
        flagEmoji = "🇻🇪",
        description = "Modalidad clásica venezolana: parejas a 100 puntos con salida de la 'Cochina' [6|6] y rotación al ganador.",
        openingRuleDescription = "Primera mano abre obligatoriamente con el Doble Seis (la 'Cochina'). Las siguientes manos las abre quien ganó la mano anterior con cualquier ficha.",
        trancaRuleDescription = "Gana la pareja del jugador que tenga menos puntos en la mano. La pareja ganadora suma todos los puntos restantes de la pareja rival.",
        capicuaBonusPoints = 0,
        defaultTargetScore = 100,
        allowsBoneyardDraw = false
    ),
    DOMINICANA(
        id = "dominicana",
        countryName = "República Dominicana",
        flagEmoji = "🇩🇴",
        description = "Dominó quisqueyano: 'Corre mano' a la derecha, premio por capicúa y juego muy dinámico.",
        openingRuleDescription = "Primera mano abre con Doble Seis. En las siguientes manos el turno de salida corre a la derecha sucesivamente.",
        trancaRuleDescription = "En caso de tranca (cierre), gana el jugador con menos puntos individuales. En caso de empate, gana quien fue el salidor de la mano.",
        capicuaBonusPoints = 25,
        defaultTargetScore = 100,
        allowsBoneyardDraw = false
    ),
    CUBA(
        id = "cuba",
        countryName = "Cuba",
        flagEmoji = "🇨🇺",
        description = "Dominó tradicional cubano: conteo estricto de puntos y máxima agresividad en los pases.",
        openingRuleDescription = "Abre la mano quien posea el mayor doble. En manos consecutivas abre el ganador de la anterior.",
        trancaRuleDescription = "El jugador con menos puntos en la tranca se lleva la suma de las fichas de todos los contrincantes.",
        capicuaBonusPoints = 0,
        defaultTargetScore = 100,
        allowsBoneyardDraw = false
    ),
    PUERTO_RICO(
        id = "puerto_rico",
        countryName = "Puerto Rico",
        flagEmoji = "🇵🇷",
        description = "Reglas boricuas: Apertura con la 'Puerca' [6|6], chuchazo con el doble blanco [0|0] y bonificación especial.",
        openingRuleDescription = "Se inicia la partida con el Doble Seis (la 'Puerca'). Ganar con doble blanco otorga el prestigioso '¡Chuchazo!'.",
        trancaRuleDescription = "En tranca gana quien tenga menor cantidad de puntos en mano; se suman los puntos de los contrarios.",
        capicuaBonusPoints = 30,
        defaultTargetScore = 100,
        allowsBoneyardDraw = false
    ),
    INTERNACIONAL(
        id = "internacional",
        countryName = "Internacional",
        flagEmoji = "🌎",
        description = "Reglamento estándar de la Federación Internacional de Dominó (FID).",
        openingRuleDescription = "Abre con Doble Seis o mayor doble en primera mano; luego rota al ganador de la ronda.",
        trancaRuleDescription = "En tranca gana la pareja de menor puntaje neto; acumula los puntos restantes del contrario.",
        capicuaBonusPoints = 0,
        defaultTargetScore = 100,
        allowsBoneyardDraw = false
    )
}
