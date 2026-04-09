package com.iksystem.food.haccpsetup.templates

import com.iksystem.common.checklist.model.ChecklistFrequency
import com.iksystem.food.haccpsetup.dto.FoodProcess
import com.iksystem.food.haccpsetup.dto.FoodType
import com.iksystem.food.haccpsetup.dto.HaccpSetupRequest
import com.iksystem.food.haccpsetup.dto.TempEquipment

/**
 * Represents a single checklist item template.
 */
data class TemplateItem(
    val title: String,
    val description: String? = null,
)

/**
 * Template definition for generating a checklist.
 */
data class ChecklistTemplate(
    val id: String,
    val name: String,
    val description: String,
    val frequency: ChecklistFrequency,
    val items: List<TemplateItem>,
    val condition: (HaccpSetupRequest) -> Boolean,
)

/**
 * Collection of predefined HACCP checklist templates.
 */
object ChecklistTemplates {

    /**
     * All available checklist templates.
     */
    val all: List<ChecklistTemplate> = listOf(
        ChecklistTemplate(
            id = "daily-cleaning",
            name = "Daglig renhold",
            description = "Daglige renholdsrutiner for lokaler og utstyr",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Rengjør arbeidsflater", "Vask og desinfiser alle arbeidsflater etter bruk"),
                TemplateItem("Rengjør gulv i kjøkken", "Fei og vask gulv i alle matområder"),
                TemplateItem("Tøm og rengjør søppelbeholdere", "Tøm søppel og vask beholdere innvendig"),
                TemplateItem("Rengjør oppvaskmaskiner", "Sjekk filtre og rengjør oppvaskmaskin"),
                TemplateItem("Rengjør håndvasker", "Sjekk at alle håndvasker har såpe og papirhåndklær"),
                TemplateItem("Rengjør toaletter og personalrom", "Daglig renhold av personaltoaletter"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "personal-hygiene",
            name = "Personlig hygiene",
            description = "Daglig kontroll av personlig hygiene for ansatte",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kontroller at ansatte bruker rent arbeidstøy"),
                TemplateItem("Sjekk håndvask ved arbeidets start", "Alle ansatte vasker hender før håndtering av mat"),
                TemplateItem("Kontroller at smykker og klokker er fjernet"),
                TemplateItem("Sjekk at sår og kutt er tildekket med blå plaster"),
                TemplateItem("Bekreft at syke ansatte ikke håndterer mat"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "waste-management",
            name = "Avfallshåndtering",
            description = "Daglige rutiner for avfallshåndtering",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Tøm avfallsbeholdere før de er fulle"),
                TemplateItem("Kildesorter avfall korrekt"),
                TemplateItem("Rengjør avfallsområdet", "Vask og desinfiser området rundt søppelbeholdere"),
                TemplateItem("Sjekk at avfallsbeholdere har lokk"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "temp-control-fridge",
            name = "Temperaturkontroll – Kjøleskap",
            description = "Daglig temperaturlogging av kjøleskap",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Logg temperatur i alle kjøleskap", "Maks 4°C. Noter avvik"),
                TemplateItem("Sjekk at dører lukkes ordentlig"),
                TemplateItem("Kontroller at mat er riktig innpakket"),
                TemplateItem("Sjekk holdbarhetsdatoer", "Fjern utgåtte varer"),
                TemplateItem("Kontroller at rå og ferdig mat er adskilt"),
            ),
            condition = { req -> TempEquipment.REFRIGERATORS in req.temperatureEquipment },
        ),
        ChecklistTemplate(
            id = "temp-control-freezer",
            name = "Temperaturkontroll – Frysere",
            description = "Daglig temperaturlogging av frysere",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Logg temperatur i alle frysere", "Maks -18°C. Noter avvik"),
                TemplateItem("Sjekk at dører lukkes ordentlig"),
                TemplateItem("Kontroller at varer er riktig merket med dato"),
                TemplateItem("Sjekk for isdannelse", "Meld fra om behov for avriming"),
            ),
            condition = { req -> TempEquipment.FREEZERS in req.temperatureEquipment },
        ),
        ChecklistTemplate(
            id = "receiving-inspection",
            name = "Mottakskontroll",
            description = "Kontroll av varer ved mottak",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kontroller temperatur på kjøle-/frysevarer ved mottak"),
                TemplateItem("Sjekk holdbarhetsdatoer på alle varer"),
                TemplateItem("Kontroller emballasje for skader"),
                TemplateItem("Sjekk at leveransen samsvarer med bestillingen"),
                TemplateItem("Sett varer i kjøl/frys umiddelbart", "Maks 30 min ved romtemperatur"),
                TemplateItem("Dokumenter eventuelle avvik ved mottak"),
            ),
            condition = { req -> FoodType.SHELF_STABLE_ONLY !in req.foodTypes || req.foodTypes.size > 1 },
        ),
        ChecklistTemplate(
            id = "cooking-control",
            name = "Tilberedningskontroll",
            description = "Kontroll av tilberedningsprosesser",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Mål kjernetemperatur på varmebehandlet mat", "Min 75°C i kjernen"),
                TemplateItem("Logg tilberedningstidspunkt og temperatur"),
                TemplateItem("Kontroller at matallergener er merket"),
                TemplateItem("Bruk rene redskaper mellom rå og ferdig mat"),
                TemplateItem("Sjekk at mat serveres innen 2 timer etter tilberedning"),
            ),
            condition = { req -> FoodProcess.COOKING_HEAT_TREATMENT in req.processes },
        ),
        ChecklistTemplate(
            id = "cooling-control",
            name = "Nedkjølingskontroll",
            description = "Kontroll av nedkjølingsprosesser",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kjøl ned mat fra 60°C til 4°C innen 4 timer"),
                TemplateItem("Logg nedkjølingstidspunkt og temperatur"),
                TemplateItem("Porsjonér mat i mindre beholdere for raskere nedkjøling"),
                TemplateItem("Merk nedkjølt mat med dato og tidspunkt"),
            ),
            condition = { req -> FoodProcess.COOLING_COOKED in req.processes },
        ),
        ChecklistTemplate(
            id = "allergen-control",
            name = "Allergenkontroll",
            description = "Daglig kontroll av allergenhåndtering",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kontroller at allergenliste er oppdatert for alle retter"),
                TemplateItem("Sjekk at allergener er tydelig merket på meny"),
                TemplateItem("Bekreft separate redskaper for allergenfri tilberedning"),
                TemplateItem("Kontroller at ansatte kjenner til allergenrutiner"),
                TemplateItem("Sjekk at ingredienslister fra leverandører er oppdatert"),
            ),
            condition = { req ->
                req.servesVulnerableGroups || FoodType.ALLERGEN_CONTAINING in req.foodTypes
            },
        ),
        ChecklistTemplate(
            id = "critical-control-points",
            name = "Kritiske kontrollpunkter (CCP)",
            description = "Overvåking av kritiske kontrollpunkter i henhold til HACCP",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kontroller alle CCP-er er innenfor kritiske grenser"),
                TemplateItem("Dokumenter korrigerende tiltak ved avvik"),
                TemplateItem("Verifiser at overvåkingsutstyr er kalibrert"),
                TemplateItem("Gjennomgå logg fra foregående skift"),
                TemplateItem("Bekreft at alle ansatte kjenner til CCP-prosedyrer"),
            ),
            condition = { req -> req.servesVulnerableGroups || req.handlesHighRiskProducts },
        ),
        ChecklistTemplate(
            id = "hot-holding-control",
            name = "Varmholdingskontroll",
            description = "Kontroll av varmholding av mat",
            frequency = ChecklistFrequency.DAILY,
            items = listOf(
                TemplateItem("Kontroller at varmholdt mat holder min 60°C"),
                TemplateItem("Logg temperatur på varmholdt mat hver time"),
                TemplateItem("Kast mat som har stått under 60°C i mer enn 2 timer"),
                TemplateItem("Sjekk at varmholdingsutstyr fungerer korrekt"),
            ),
            condition = { req -> TempEquipment.HOT_HOLDING in req.temperatureEquipment },
        ),
        ChecklistTemplate(
            id = "weekly-cleaning",
            name = "Ukentlig renhold",
            description = "Grundig ukentlig rengjøring",
            frequency = ChecklistFrequency.WEEKLY,
            items = listOf(
                TemplateItem("Grundig rengjøring av kjøleskap", "Tøm, vask og desinfiser innvendig"),
                TemplateItem("Rengjør ventilasjon og avtrekkshetter"),
                TemplateItem("Vask vegger og tak i kjøkkenområdet"),
                TemplateItem("Rengjør lagerlokaler"),
                TemplateItem("Desinfiser skjærebrett og kniver grundig"),
                TemplateItem("Rengjør bak og under utstyr"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "pest-control",
            name = "Skadedyrkontroll",
            description = "Månedlig kontroll for skadedyr",
            frequency = ChecklistFrequency.MONTHLY,
            items = listOf(
                TemplateItem("Inspiser lokaler for tegn på skadedyr"),
                TemplateItem("Kontroller at dører og vinduer tetter godt"),
                TemplateItem("Sjekk skadedyrfeller og -stasjoner"),
                TemplateItem("Kontroller lagerlokaler for gnagerspor"),
                TemplateItem("Dokumenter funn og iverksatte tiltak"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "equipment-maintenance",
            name = "Vedlikehold av utstyr",
            description = "Månedlig vedlikehold og kontroll av utstyr",
            frequency = ChecklistFrequency.MONTHLY,
            items = listOf(
                TemplateItem("Kalibrer termometre", "Sjekk at alle termometre viser korrekt"),
                TemplateItem("Kontroller at kjøle-/fryseutstyr fungerer optimalt"),
                TemplateItem("Vedlikehold oppvaskmaskiner", "Sjekk temperatur og dosering"),
                TemplateItem("Kontroller steketermometre og tidtakere"),
                TemplateItem("Dokumenter vedlikeholdsarbeid"),
            ),
            condition = { req ->
                req.temperatureEquipment.isNotEmpty() && TempEquipment.NONE !in req.temperatureEquipment
            },
        ),
        ChecklistTemplate(
            id = "annual-ik-review",
            name = "Årlig gjennomgang av IK-mat",
            description = "Årlig revisjon av internkontrollsystemet",
            frequency = ChecklistFrequency.YEARLY,
            items = listOf(
                TemplateItem("Gjennomgå og oppdater alle rutiner og prosedyrer"),
                TemplateItem("Evaluer avviksstatistikk fra siste år"),
                TemplateItem("Oppdater farevurdering (HACCP-analyse)"),
                TemplateItem("Kontroller at alle dokumenter er oppdatert"),
                TemplateItem("Gjennomgå leverandøravtaler og sertifikater"),
                TemplateItem("Planlegg opplæringsbehov for kommende år"),
                TemplateItem("Dokumenter gjennomgangen og forbedringstiltak"),
            ),
            condition = { true },
        ),
        ChecklistTemplate(
            id = "training-plan",
            name = "Opplæringsplan",
            description = "Årlig gjennomgang av opplæring og kompetanse",
            frequency = ChecklistFrequency.YEARLY,
            items = listOf(
                TemplateItem("Kartlegg opplæringsbehov for alle ansatte"),
                TemplateItem("Planlegg opplæring i mathygiene og HACCP"),
                TemplateItem("Gjennomfør opplæring i allergenbehandling"),
                TemplateItem("Dokumenter gjennomført opplæring for alle ansatte"),
                TemplateItem("Evaluer effekt av opplæringstiltak"),
            ),
            condition = { true },
        ),
    )
}