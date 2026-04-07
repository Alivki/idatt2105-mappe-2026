-- ============================================================
-- Seed data for age verification shifts and linked deviations
-- Uses org 1 (IK System), users 2 (manager) and 3 (employee)
-- ============================================================

-- Completed shifts over the past week
INSERT INTO age_verification_shifts (organization_id, user_id, shift_date, started_at, ended_at, ids_checked_count, signed_off, signed_off_at, status)
VALUES
    -- 7 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 18 HOUR, 14, TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 18 HOUR, 'COMPLETED'),
    (1, 2, DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 23 HOUR, 22, TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    -- 6 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 19 HOUR, 18, TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 19 HOUR, 'COMPLETED'),
    -- 5 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR, 11, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR, 'COMPLETED'),
    (1, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 23 HOUR, 19, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    -- 4 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 20 HOUR, 16, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 20 HOUR, 'COMPLETED'),
    -- 3 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR, 20, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR, 'COMPLETED'),
    (1, 2, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR, 15, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    -- 2 days ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR, 13, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR, 'COMPLETED'),
    (1, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR, 25, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    -- 1 day ago
    (1, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, 17, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, 'COMPLETED'),
    (1, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 23 HOUR, 21, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 23 HOUR, 'COMPLETED');

-- Deviations linked to some of those shifts
-- shift IDs will be 1-12 based on insert order above
INSERT INTO alcohol_deviations (organization_id, reported_at, reported_by_user_id, report_source, deviation_type, description, status, age_verification_shift_id)
VALUES
    -- Shift 1 (employee, 7 days ago): 1 deviation
    (1, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 14 HOUR, 3, 'EGENRAPPORT', 'NEKTET_VISE_LEGITIMASJON', 'Gjest nektet å vise legitimasjon ved bestilling av øl', 'OPEN', 1),
    -- Shift 2 (manager, 7 days ago): 1 deviation
    (1, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 20 HOUR, 2, 'EGENRAPPORT', 'UTGAATT_LEGITIMASJON', 'Gjest presenterte utgått førerkort', 'OPEN', 2),
    -- Shift 5 (manager, 5 days ago): 2 deviations
    (1, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 19 HOUR, 2, 'EGENRAPPORT', 'MINDREAARIG_FORSOK', 'Mindreårig forsøkte å kjøpe alkohol med falskt ID', 'OPEN', 5),
    (1, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 21 HOUR, 2, 'EGENRAPPORT', 'FALSK_LEGITIMASJON', 'Oppdaget forfalsket nasjonalt ID-kort', 'OPEN', 5),
    -- Shift 7 (employee, 3 days ago): 1 deviation
    (1, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 15 HOUR, 3, 'EGENRAPPORT', 'GLEMTE_SJEKKE_LEGITIMASJON', 'Glemte å sjekke ID på ung gjest i rushperiode', 'OPEN', 7),
    -- Shift 10 (manager, 2 days ago): 1 deviation
    (1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 HOUR, 2, 'EGENRAPPORT', 'NEKTET_VISE_LEGITIMASJON', 'Gjest ble aggressiv da vi spurte om legitimasjon', 'OPEN', 10),
    -- Shift 11 (employee, 1 day ago): 2 deviations
    (1, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 13 HOUR, 3, 'EGENRAPPORT', 'LEGITIMASJON_ANNET', 'Gjest viste bilde av legitimasjon på telefon, avvist', 'OPEN', 11),
    (1, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 16 HOUR, 3, 'EGENRAPPORT', 'UTGAATT_LEGITIMASJON', 'Utgått bankkort brukt som legitimasjon', 'OPEN', 11);
