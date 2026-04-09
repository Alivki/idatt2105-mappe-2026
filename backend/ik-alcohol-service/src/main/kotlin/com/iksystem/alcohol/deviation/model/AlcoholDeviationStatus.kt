package com.iksystem.alcohol.deviation.model

/**
 * Represents the lifecycle status of an alcohol deviation.
 *
 * - OPEN: Newly reported and not yet handled
 * - UNDER_TREATMENT: Currently being processed or investigated
 * - CLOSED: Fully resolved and finalized
 */
enum class AlcoholDeviationStatus {
    OPEN,
    UNDER_TREATMENT,
    CLOSED,
}