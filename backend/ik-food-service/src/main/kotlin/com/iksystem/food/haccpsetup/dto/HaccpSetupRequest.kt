package com.iksystem.food.haccpsetup.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * Types of food businesses.
 */
enum class BusinessType {
    RESTAURANT, CAFE, BAR, KIOSK, CATERING, CANTEEN,
    INSTITUTION_KITCHEN, FOOD_PRODUCTION, RETAIL, MOBILE_VENDOR,
}

/**
 * Size categories for businesses.
 */
enum class BusinessSize {
    SMALL_1_5, MEDIUM_6_15, LARGE_16_50, ENTERPRISE_50_PLUS,
}

/**
 * Types of food handled.
 */
enum class FoodType {
    READY_TO_EAT, PERISHABLE_TEMP_CONTROL, RAW_MEAT_POULTRY,
    SEAFOOD_FISH, DAIRY, ALLERGEN_CONTAINING, SHELF_STABLE_ONLY,
}

/**
 * Food processing methods.
 */
enum class FoodProcess {
    COOKING_HEAT_TREATMENT, COOLING_COOKED, THAWING_FROZEN,
    VACUUM_PACKING, CURING_SMOKING_PRESERVING, NONE_PRE_MADE,
}

/**
 * Available facility types.
 */
enum class Facility {
    COMMERCIAL_KITCHEN, COLD_STORAGE, FREEZER_STORAGE, DRY_STORAGE,
    SEPARATE_PREP_AREAS, DISHWASHING_STATION, HANDWASHING_STATIONS, STAFF_CHANGING_AREA,
}

/**
 * Types of temperature equipment.
 */
enum class TempEquipment {
    REFRIGERATORS, FREEZERS, HOT_HOLDING, COOKING_EQUIPMENT, NONE,
}

/**
 * Methods of receiving goods.
 */
enum class GoodsReceiving {
    DIRECT_DELIVERY, PICK_UP, BOTH,
}

/**
 * Request payload for generating HACCP checklists.
 */
data class HaccpSetupRequest(
    @field:NotNull(message = "Business type is required")
    val businessType: BusinessType,

    @field:NotNull(message = "Business size is required")
    val businessSize: BusinessSize,

    @field:NotEmpty(message = "At least one food type is required")
    val foodTypes: Set<FoodType>,

    @field:NotEmpty(message = "At least one process is required")
    val processes: Set<FoodProcess>,

    val facilities: Set<Facility> = emptySet(),

    val temperatureEquipment: Set<TempEquipment> = emptySet(),

    @field:NotNull(message = "Serves vulnerable groups is required")
    val servesVulnerableGroups: Boolean,

    @field:NotNull(message = "Handles high risk products is required")
    val handlesHighRiskProducts: Boolean,

    @field:NotNull(message = "Goods receiving method is required")
    val goodsReceiving: GoodsReceiving,
)