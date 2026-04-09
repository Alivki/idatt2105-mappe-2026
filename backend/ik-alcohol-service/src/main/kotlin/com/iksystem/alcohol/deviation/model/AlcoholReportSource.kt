package com.iksystem.alcohol.deviation.model

/**
 * Represents the source of an alcohol deviation report.
 *
 * Used to distinguish between internally reported incidents
 * and external reports from authorities.
 */
enum class AlcoholReportSource {
    EGENRAPPORT,
    SJENKEKONTROLL,
    POLITIRAPPORT,
}