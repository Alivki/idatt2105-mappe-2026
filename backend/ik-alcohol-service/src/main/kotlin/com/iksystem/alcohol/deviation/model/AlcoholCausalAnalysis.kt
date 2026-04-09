package com.iksystem.alcohol.deviation.model

/**
 * Represents the root cause classification for an alcohol-related deviation.
 *
 * Used to categorize why a deviation occurred,
 * supporting analysis and preventive improvements.
 */
enum class AlcoholCausalAnalysis {
    MANGLENDE_OPPLAERING,
    RUTINE_IKKE_FULGT,
    RUTINE_MANGLER,
    HOYT_TRYKK_STRESS,
    UNDERBEMANNING,
    KOMMUNIKASJON,
    ANNET,
}