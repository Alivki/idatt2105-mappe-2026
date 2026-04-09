package com.iksystem.alcohol.deviation.model

/**
 * Represents types of alcohol-related deviations.
 *
 * Each type is associated with a penalty point value,
 * which is applied when the report source is SJENKEKONTROLL or POLITIRAPPORT.
 *
 * Deviations with 0 penalty points are typically internal incidents
 * related to age verification and are not part of official penalty scoring.
 */
enum class AlcoholDeviationType(val penaltyPoints: Int) {
    SKJENKING_MINDREAARIGE(8),
    BRUDD_BISTANDSPLIKT(8),
    UFORSVARLIG_DRIFT(8),
    HINDRING_KONTROLL(8),

    SKJENKING_APENBART_BERUSET(4),
    BRUDD_SJENKETIDER(4),
    BRENNEVIN_18_19(4),

    BERUSET_PERSON_I_LOKALET(2),
    MANGLER_IK_SYSTEM(2),
    MANGLER_STYRER_STEDFORTREDER(2),
    NARKOTIKA(2),

    ALKOHOLFRI_ALTERNATIV_MANGLER(1),
    MEDBRAKT_ALKOHOL(1),
    REKLAMEBRUDD(1),
    VILKAARSBRUDD(1),

    NEKTET_VISE_LEGITIMASJON(0),
    GLEMTE_SJEKKE_LEGITIMASJON(0),
    MINDREAARIG_FORSOK(0),
    FALSK_LEGITIMASJON(0),
    UTGAATT_LEGITIMASJON(0),
    LEGITIMASJON_ANNET(0),
}