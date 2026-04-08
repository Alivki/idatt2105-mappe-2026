package com.iksystem.common.report.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate

data class TempApplianceData(
    val id: Long,
    val name: String,
    val applianceType: String,
    val minTemperature: BigDecimal,
    val maxTemperature: BigDecimal,
)

data class TempMeasurementData(
    val id: Long,
    val applianceId: Long,
    val measuredAt: Instant,
    val temperature: BigDecimal,
    val status: String,
)

data class FoodDeviationData(
    val id: Long,
    val reportedAt: Instant,
    val reportedByName: String,
    val deviationType: String,
    val severity: String,
    val description: String,
    val immediateAction: String?,
    val status: String,
)

data class AlcoholDeviationData(
    val id: Long,
    val reportedAt: Instant,
    val reportedByName: String,
    val deviationType: String,
    val description: String,
    val immediateAction: String?,
    val status: String,
)

data class AgeVerificationShiftData(
    val id: Long,
    val shiftDate: LocalDate,
    val userName: String,
    val idsCheckedCount: Int,
    val status: String,
)

data class AlcoholPolicyData(
    val bevillingNumber: String?,
    val bevillingValidUntil: LocalDate?,
    val styrerName: String?,
    val stedfortrederName: String?,
    val kunnskapsproveType: String?,
    val kunnskapsproveCandidateName: String?,
    val kunnskapsproveBirthDate: LocalDate?,
    val kunnskapsprovePassedDate: LocalDate?,
    val kunnskapsproveMunicipality: String?,
)

@Repository
class ReportDataRepository(private val jdbc: JdbcTemplate) {

    fun findActiveAppliances(orgId: Long): List<TempApplianceData> {
        return jdbc.query(
            """
            SELECT id, name, appliance_type, min_temperature, max_temperature
            FROM temperature_appliances
            WHERE organization_id = ? AND is_active = true
            ORDER BY name
            """,
            { rs: ResultSet, _ ->
                TempApplianceData(
                    id = rs.getLong("id"),
                    name = rs.getString("name"),
                    applianceType = rs.getString("appliance_type"),
                    minTemperature = rs.getBigDecimal("min_temperature"),
                    maxTemperature = rs.getBigDecimal("max_temperature"),
                )
            },
            orgId,
        )
    }

    fun findMeasurements(orgId: Long, from: Instant, to: Instant): List<TempMeasurementData> {
        return jdbc.query(
            """
            SELECT id, appliance_id, measured_at, temperature, status
            FROM temperature_measurements
            WHERE organization_id = ? AND measured_at BETWEEN ? AND ?
            ORDER BY measured_at
            """,
            { rs: ResultSet, _ ->
                TempMeasurementData(
                    id = rs.getLong("id"),
                    applianceId = rs.getLong("appliance_id"),
                    measuredAt = rs.getTimestamp("measured_at").toInstant(),
                    temperature = rs.getBigDecimal("temperature"),
                    status = rs.getString("status"),
                )
            },
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
        )
    }

    fun findFoodDeviations(orgId: Long, from: Instant, to: Instant): List<FoodDeviationData> {
        return jdbc.query(
            """
            SELECT fd.id, fd.reported_at, u.full_name AS reported_by_name,
                   fd.deviation_type, fd.severity, fd.description,
                   fd.immediate_action, fd.status
            FROM food_deviations fd
            JOIN users u ON fd.reported_by_user_id = u.id
            WHERE fd.organization_id = ? AND fd.reported_at BETWEEN ? AND ?
            ORDER BY fd.reported_at DESC
            """,
            { rs: ResultSet, _ ->
                FoodDeviationData(
                    id = rs.getLong("id"),
                    reportedAt = rs.getTimestamp("reported_at").toInstant(),
                    reportedByName = rs.getString("reported_by_name"),
                    deviationType = rs.getString("deviation_type"),
                    severity = rs.getString("severity"),
                    description = rs.getString("description"),
                    immediateAction = rs.getString("immediate_action"),
                    status = rs.getString("status"),
                )
            },
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
        )
    }

    fun countFoodDeviationsByStatus(orgId: Long, from: Instant, to: Instant, status: String): Long {
        return jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM food_deviations
            WHERE organization_id = ? AND reported_at BETWEEN ? AND ? AND status = ?
            """,
            Long::class.java,
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
            status,
        ) ?: 0L
    }

    fun findAlcoholDeviations(orgId: Long, from: Instant, to: Instant): List<AlcoholDeviationData> {
        return jdbc.query(
            """
            SELECT ad.id, ad.reported_at, u.full_name AS reported_by_name,
                   ad.deviation_type, ad.description,
                   ad.immediate_action, ad.status
            FROM alcohol_deviations ad
            JOIN users u ON ad.reported_by_user_id = u.id
            WHERE ad.organization_id = ? AND ad.reported_at BETWEEN ? AND ?
            ORDER BY ad.reported_at DESC
            """,
            { rs: ResultSet, _ ->
                AlcoholDeviationData(
                    id = rs.getLong("id"),
                    reportedAt = rs.getTimestamp("reported_at").toInstant(),
                    reportedByName = rs.getString("reported_by_name"),
                    deviationType = rs.getString("deviation_type"),
                    description = rs.getString("description"),
                    immediateAction = rs.getString("immediate_action"),
                    status = rs.getString("status"),
                )
            },
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
        )
    }

    fun countAlcoholDeviationsByStatus(orgId: Long, from: Instant, to: Instant, status: String): Long {
        return jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM alcohol_deviations
            WHERE organization_id = ? AND reported_at BETWEEN ? AND ? AND status = ?
            """,
            Long::class.java,
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
            status,
        ) ?: 0L
    }

    fun findAgeVerificationShifts(orgId: Long, from: LocalDate, to: LocalDate): List<AgeVerificationShiftData> {
        return jdbc.query(
            """
            SELECT avs.id, avs.shift_date, u.full_name AS user_name,
                   avs.ids_checked_count, avs.status
            FROM age_verification_shifts avs
            JOIN users u ON avs.user_id = u.id
            WHERE avs.organization_id = ? AND avs.shift_date BETWEEN ? AND ?
            ORDER BY avs.shift_date DESC
            """,
            { rs: ResultSet, _ ->
                AgeVerificationShiftData(
                    id = rs.getLong("id"),
                    shiftDate = rs.getDate("shift_date").toLocalDate(),
                    userName = rs.getString("user_name"),
                    idsCheckedCount = rs.getInt("ids_checked_count"),
                    status = rs.getString("status"),
                )
            },
            orgId,
            java.sql.Date.valueOf(from),
            java.sql.Date.valueOf(to),
        )
    }

    fun countAlcoholDeviationsByDate(orgId: Long, date: LocalDate): Long {
        val from = java.sql.Timestamp.valueOf(date.atStartOfDay())
        val to = java.sql.Timestamp.valueOf(date.plusDays(1).atStartOfDay())
        return jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM alcohol_deviations
            WHERE organization_id = ? AND reported_at BETWEEN ? AND ?
            """,
            Long::class.java,
            orgId, from, to,
        ) ?: 0L
    }

    fun findAlcoholPolicy(orgId: Long): AlcoholPolicyData? {
        val results = jdbc.query(
            """
            SELECT bevilling_number, bevilling_valid_until, styrer_name, stedfortreder_name,
                   kunnskapsprove_type, kunnskapsprove_candidate_name, kunnskapsprove_birth_date,
                   kunnskapsprove_passed_date, kunnskapsprove_municipality
            FROM alcohol_policies
            WHERE organization_id = ?
            """,
            { rs: ResultSet, _ ->
                AlcoholPolicyData(
                    bevillingNumber = rs.getString("bevilling_number"),
                    bevillingValidUntil = rs.getDate("bevilling_valid_until")?.toLocalDate(),
                    styrerName = rs.getString("styrer_name"),
                    stedfortrederName = rs.getString("stedfortreder_name"),
                    kunnskapsproveType = rs.getString("kunnskapsprove_type"),
                    kunnskapsproveCandidateName = rs.getString("kunnskapsprove_candidate_name"),
                    kunnskapsproveBirthDate = rs.getDate("kunnskapsprove_birth_date")?.toLocalDate(),
                    kunnskapsprovePassedDate = rs.getDate("kunnskapsprove_passed_date")?.toLocalDate(),
                    kunnskapsproveMunicipality = rs.getString("kunnskapsprove_municipality"),
                )
            },
            orgId,
        )
        return results.firstOrNull()
    }

    fun countTempDeviations(orgId: Long, from: Instant, to: Instant): Long {
        return jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM temperature_measurements
            WHERE organization_id = ? AND measured_at BETWEEN ? AND ? AND status = 'DEVIATION'
            """,
            Long::class.java,
            orgId,
            java.sql.Timestamp.from(from),
            java.sql.Timestamp.from(to),
        ) ?: 0L
    }
}
