import type { FoodDeviationType, AlcoholDeviationType } from '@/types/deviation'

export const FOOD_DEVIATION_TYPE_LABELS: Record<FoodDeviationType, string> = {
  TEMPERATUR: 'Temperatur',
  RENHOLD: 'Renhold',
  PERSONLIG_HYGIENE: 'Personlig hygiene',
  ALLERGEN: 'Allergen',
  SKADEDYR: 'Skadedyr',
  MOTTAKSKONTROLL: 'Mottakskontroll',
  ANNET: 'Annet',
}

export const ALCOHOL_DEVIATION_TYPE_LABELS: Record<AlcoholDeviationType, string> = {
  SKJENKING_MINDREAARIGE: 'Skjenking mindreårige',
  BRUDD_BISTANDSPLIKT: 'Brudd bistandsplikt',
  UFORSVARLIG_DRIFT: 'Uforsvarlig drift',
  HINDRING_KONTROLL: 'Hindring kontroll',
  SKJENKING_APENBART_BERUSET: 'Skjenking beruset',
  BRUDD_SJENKETIDER: 'Brudd sjenketider',
  BRENNEVIN_18_19: 'Brennevin 18-19',
  BERUSET_PERSON_I_LOKALET: 'Beruset i lokalet',
  MANGLER_IK_SYSTEM: 'Mangler IK-system',
  MANGLER_STYRER_STEDFORTREDER: 'Mangler styrer',
  NARKOTIKA: 'Narkotika',
  ALKOHOLFRI_ALTERNATIV_MANGLER: 'Alkoholfri mangler',
  MEDBRAKT_ALKOHOL: 'Medbrakt alkohol',
  REKLAMEBRUDD: 'Reklamebrudd',
  VILKAARSBRUDD: 'Vilkårsbrudd',
  NEKTET_VISE_LEGITIMASJON: 'Nektet å vise leg',
  GLEMTE_SJEKKE_LEGITIMASJON: 'Glemte å sjekke leg',
  MINDREAARIG_FORSOK: 'Mindreårig forsøk',
  FALSK_LEGITIMASJON: 'Falsk legitimasjon',
  UTGAATT_LEGITIMASJON: 'Utgått legitimasjon',
  LEGITIMASJON_ANNET: 'Annet (legitimasjon)',
}
