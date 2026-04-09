-- ============================================================
-- V25: Complete seed data reset
-- Clears ALL existing data and inserts comprehensive, realistic
-- seed data for two restaurant organizations.
-- ============================================================

-- ---- 1. CLEAN SLATE (FK-safe delete order) ----
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM checklist_completions;
DELETE FROM generated_reports;
DELETE FROM penalty_points;
DELETE FROM age_verification_shifts;
DELETE FROM alcohol_deviations;
DELETE FROM food_deviations;
DELETE FROM deviations;
DELETE FROM notifications;
DELETE FROM temperature_measurements;
DELETE FROM temperature_appliances;
DELETE FROM alcohol_policies;
DELETE FROM documents;
DELETE FROM invitations;
DELETE FROM training_logs;
DELETE FROM checklist_items;
DELETE FROM checklists;
DELETE FROM sessions;
DELETE FROM refresh_tokens;
DELETE FROM memberships;
DELETE FROM users;
DELETE FROM organizations;

SET FOREIGN_KEY_CHECKS = 1;

-- Reset auto-increment counters
ALTER TABLE organizations AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE memberships AUTO_INCREMENT = 1;
ALTER TABLE checklists AUTO_INCREMENT = 1;
ALTER TABLE checklist_items AUTO_INCREMENT = 1;
ALTER TABLE deviations AUTO_INCREMENT = 1;
ALTER TABLE food_deviations AUTO_INCREMENT = 1;
ALTER TABLE alcohol_deviations AUTO_INCREMENT = 1;
ALTER TABLE penalty_points AUTO_INCREMENT = 1;
ALTER TABLE training_logs AUTO_INCREMENT = 1;
ALTER TABLE temperature_appliances AUTO_INCREMENT = 1;
ALTER TABLE temperature_measurements AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE alcohol_policies AUTO_INCREMENT = 1;
ALTER TABLE age_verification_shifts AUTO_INCREMENT = 1;
ALTER TABLE documents AUTO_INCREMENT = 1;
ALTER TABLE checklist_completions AUTO_INCREMENT = 1;
ALTER TABLE generated_reports AUTO_INCREMENT = 1;

-- ============================================================
-- 2. ORGANIZATIONS
-- ============================================================
INSERT INTO organizations (id, name, org_number) VALUES
    (1, 'Everest Sushi & Fusion AS', '937219997'),
    (2, 'Nordvik Bar & Kjøkken AS',  '912345678');

-- ============================================================
-- 3. USERS
-- All passwords are 'password' hashed with BCrypt strength 12
-- ============================================================
INSERT INTO users (id, email, password, full_name, phone_number, active) VALUES
    (1, 'admin@iksystem.local',    '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Iver Sigvart Berge',    '+4741234567', TRUE),
    (2, 'manager@iksystem.local',  '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Amina Khatri',          '+4792345678', TRUE),
    (3, 'employee@iksystem.local', '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Jonas Eriksen',         '+4798765432', TRUE),
    (4, 'kokk@iksystem.local',     '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Linh Nguyen',           '+4745678901', TRUE),
    (5, 'bartender@iksystem.local', '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Erik Nordmann',         '+4790123456', TRUE),
    (6, 'daglig@iksystem.local',   '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Kristine Haugen',       '+4741122334', TRUE),
    (7, 'servitor@iksystem.local', '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Mohammed Al-Rashid',    '+4799887766', TRUE),
    (8, 'renhold@iksystem.local',  '$2a$12$tBdYa9T7INK1Mxc3x6RKt.X0u/GFD80lTk0n/LsNIjpQQDkBzSn7q', 'Sara Johansen',         '+4745566778', TRUE);

-- ============================================================
-- 4. MEMBERSHIPS
-- Org 1 (Everest): full staff
-- Org 2 (Nordvik): admin + daglig leder + some overlap
-- ============================================================
INSERT INTO memberships (user_id, organization_id, role) VALUES
    -- Everest Sushi & Fusion AS
    (1, 1, 'ADMIN'),       -- Iver - system admin
    (2, 1, 'MANAGER'),     -- Amina - kjøkkensjef / manager
    (3, 1, 'EMPLOYEE'),    -- Jonas - ansatt
    (4, 1, 'EMPLOYEE'),    -- Linh - kokk
    (5, 1, 'EMPLOYEE'),    -- Erik - bartender
    (7, 1, 'EMPLOYEE'),    -- Mohammed - servitør
    (8, 1, 'EMPLOYEE'),    -- Sara - renhold
    -- Nordvik Bar & Kjøkken AS
    (1, 2, 'ADMIN'),       -- Iver - admin for both orgs
    (6, 2, 'MANAGER'),     -- Kristine - daglig leder
    (5, 2, 'EMPLOYEE'),    -- Erik - works at both places
    (7, 2, 'EMPLOYEE');    -- Mohammed - works at both places

-- ============================================================
-- 5. CHECKLISTS — Org 1 (Everest Sushi & Fusion)
-- ============================================================
INSERT INTO checklists (id, organization_id, name, description, frequency, source, active) VALUES
    -- Daily checklists
    (1,  1, 'Åpningsrutiner kjøkken',           'Daglig sjekk før kjøkkenet åpner',                               'DAILY',   'MANUAL', TRUE),
    (2,  1, 'Stengerutiner kjøkken',             'Sjekkliste ved stenging av kjøkken',                             'DAILY',   'MANUAL', TRUE),
    (3,  1, 'Personlig hygiene - daglig',        'Sjekk av håndhygiene, arbeidsklær og helse',                     'DAILY',   'MANUAL', TRUE),
    (4,  1, 'Skjenkekontroll kveldsvakt',        'Alderskontroll og ansvarlig servering',                          'DAILY',   'MANUAL', TRUE),
    (5,  1, 'Mottak av vareleveranse',           'Kontroll av temperatur og kvalitet ved varemottak',              'DAILY',   'MANUAL', TRUE),
    -- Weekly
    (6,  1, 'Ukentlig dyprengjøring',            'Full rengjøring av kjøkken, lager og kjølerom',                  'WEEKLY',  'MANUAL', TRUE),
    (7,  1, 'Ukentlig allergenkontroll',          'Gjennomgang av allergenlister og merking',                       'WEEKLY',  'MANUAL', TRUE),
    -- Monthly
    (8,  1, 'Månedlig temperaturkalibrering',     'Verifisering og kalibrering av alle termometre',                 'MONTHLY', 'MANUAL', TRUE),
    (9,  1, 'Brannvern og sikkerhet',             'Sjekk av brannslukker, nødutgang og førstehjelpsutstyr',        'MONTHLY', 'MANUAL', TRUE),
    (10, 1, 'Skadedyrkontroll',                   'Inspeksjon av lokaler for tegn til skadedyr',                   'MONTHLY', 'MANUAL', TRUE);

-- Checklists — Org 2 (Nordvik Bar & Kjøkken)
INSERT INTO checklists (id, organization_id, name, description, frequency, source, active) VALUES
    (11, 2, 'Åpningsrutiner bar',                'Daglig klargjøring av barområdet',                              'DAILY',   'MANUAL', TRUE),
    (12, 2, 'Stengerutiner bar og kjøkken',      'Nedstengning og sikring',                                       'DAILY',   'MANUAL', TRUE),
    (13, 2, 'Ukentlig renhold lager',             'Rengjøring og organisering av lager',                           'WEEKLY',  'MANUAL', TRUE),
    (14, 2, 'Månedlig HMS-gjennomgang',           'Helse, miljø og sikkerhet for alle ansatte',                    'MONTHLY', 'MANUAL', TRUE);

-- ============================================================
-- 5b. CHECKLIST ITEMS
-- ============================================================

-- Checklist 1: Åpningsrutiner kjøkken (completed)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (1, 'Sjekk temperatur i alle kjøleskap',        'Loggfør temperaturer i skjema',                       1, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    (1, 'Sjekk temperatur i frysere',               'Alle frysere skal være under -18°C',                  2, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    (1, 'Vask og desinfiser arbeidsflater',          'Benker, skjærebrett og stasjonsbord',                 3, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    (1, 'Kontroller håndvaskstasjoner',              'Såpe, papir og desinfeksjon tilgjengelig',            4, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    (1, 'Sjekk datomerking på råvarer',              'Fjern utgåtte produkter',                             5, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    (1, 'Kontroller renhet på gulv',                 'Ingen søl, fett eller hindringer',                    6, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR));

-- Checklist 2: Stengerutiner kjøkken (in progress)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (2, 'Rengjør alle kokeflater',                   'Komfyr, stekeovn og frityrkoker',                    1, TRUE,  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 MINUTE)),
    (2, 'Tøm og rengjør oppvaskmaskin',              'Kjør rengjøringsprogram',                             2, TRUE,  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 MINUTE)),
    (2, 'Pakk inn og merk restemat',                 'Dato, innhold og navn',                               3, FALSE, NULL),
    (2, 'Tøm søppel og kildesorter',                 'Alle avfallsbeholdere',                               4, FALSE, NULL),
    (2, 'Lås kjølerom og lager',                     'Sjekk at dører er forsvarlig lukket',                 5, FALSE, NULL);

-- Checklist 3: Personlig hygiene (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (3, 'Rent arbeidsantrekk',                       'Ren uniform, forkle og hodeplagg',                    1, FALSE),
    (3, 'Håndhygiene gjennomført',                   'Grundig håndvask med såpe i 20 sekunder',            2, FALSE),
    (3, 'Ingen smykker eller klokker',               'Ringer, armbånd og klokker fjernet',                 3, FALSE),
    (3, 'Sår og kutt dekket',                        'Vanntette plaster og hansker ved behov',             4, FALSE),
    (3, 'Ingen symptomer på sykdom',                 'Oppkast, diaré, feber = meld fra til leder',         5, FALSE);

-- Checklist 4: Skjenkekontroll kveldsvakt (partially done)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (4, 'Kontroller ID på alle som ser under 25 ut', 'Pass, førerkort eller bankkort med bilde',           1, TRUE,  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 HOUR)),
    (4, 'Loggfør antall ID-sjekker',                 'Notering i vaktlogg',                                2, TRUE,  DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 HOUR)),
    (4, 'Observer gjester for beruselse',            'Sløret tale, ustødig gange, aggressiv adferd',       3, FALSE, NULL),
    (4, 'Nekt servering ved tvil',                   'Dokumenter hendelsen',                                4, FALSE, NULL);

-- Checklist 5: Mottak av vareleveranse (completed)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (5, 'Mål temperatur på kjølevarer',              'Skal være under 4°C ved mottak',                     1, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR)),
    (5, 'Mål temperatur på frosne varer',            'Skal være under -18°C ved mottak',                   2, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR)),
    (5, 'Kontroller emballasje',                     'Ingen skader, lekkasjer eller bulker',               3, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR)),
    (5, 'Sjekk holdbarhetsdatoer',                   'Minimum 3 dager gjenværende',                        4, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR)),
    (5, 'Sett varer på riktig plass',                'FIFO-prinsipp (først inn, først ut)',                5, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR));

-- Checklist 6: Ukentlig dyprengjøring (completed last week)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (6, 'Avfetting av ventilasjonshetter',           'Bruk godkjent avfettingsmiddel',                     1, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (6, 'Rengjør bak kjøleskap og frysere',          'Flytt ut og rengjør gulv og vegger',                 2, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (6, 'Desinfiser alle avløp',                     'Kjøkken og bak bar',                                 3, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (6, 'Vask vegger og tak i kjøkken',              'Fjern fettavsetninger',                               4, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (6, 'Rengjør lagerhyller',                       'Tøm, vask og sett tilbake',                          5, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY));

-- Checklist 7: Ukentlig allergenkontroll (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (7, 'Oppdater allergenliste for alle retter',    'Sjekk mot faktiske ingredienser',                    1, FALSE),
    (7, 'Kontroller merking i vitriner',             'Tydelig merking av allergener',                      2, FALSE),
    (7, 'Gjennomgå leverandørdokumentasjon',         'Sjekk endringer i ingredienser',                    3, FALSE),
    (7, 'Briefing med kjøkkenpersonale',             'Gjennomgå ukas allergenendringer',                  4, FALSE);

-- Checklist 8: Månedlig temperaturkalibrering (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (8, 'Kalibrer kjøleskapstermometre',             'Test mot referansetermometer',                       1, FALSE),
    (8, 'Kalibrer frysertermometre',                 'Test mot referansetermometer',                       2, FALSE),
    (8, 'Kalibrer stikktermometer',                  'Isvannstest (0°C) og kokepunkttest (100°C)',        3, FALSE),
    (8, 'Dokumenter avvik og korrigeringer',         'Loggfør i kalibreringsjournal',                     4, FALSE);

-- Checklist 9: Brannvern (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (9, 'Kontroller brannslukker',                   'Trykk, plombering og utløpsdato',                   1, FALSE),
    (9, 'Test nødbelysning',                         'Alle nødlys fungerer',                               2, FALSE),
    (9, 'Sjekk nødutganger',                         'Frie for hindringer, skilting synlig',               3, FALSE),
    (9, 'Kontroller førstehjelpsskrin',              'Komplett innhold, ingen utgåtte produkter',          4, FALSE),
    (9, 'Gjennomgå rømningsplan med ansatte',        'Alle kjenner rømningsveier',                        5, FALSE);

-- Checklist 10: Skadedyrkontroll (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (10, 'Inspiser lager for ekskrementer',          'Mus, rotter, insekter',                              1, FALSE),
    (10, 'Kontroller tetting rundt rør',             'Ingen åpninger der skadedyr kan komme inn',          2, FALSE),
    (10, 'Sjekk fellene',                            'Limfeller og fangststasjoner',                      3, FALSE),
    (10, 'Dokumenter funn og tiltak',                'Rapporter til skadedyrkontrollfirma ved behov',     4, FALSE);

-- Checklist 11: Åpningsrutiner bar - Nordvik (completed)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (11, 'Vask og poler bardisk',                    'Desinfiser og tørk av',                              1, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR)),
    (11, 'Fyll opp kjølekasser',                     'Øl, brus, juice',                                    2, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR)),
    (11, 'Sjekk gassflasker for tappeanlegg',        'CO2-nivå og kobling',                                3, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR)),
    (11, 'Rengjør glassvaskmaskin',                   'Kjør rengjøringsprogram',                            4, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR));

-- Checklist 12: Stengerutiner Nordvik (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (12, 'Tøm og rengjør iskasse',                   'Fjern vann og desinfiser',                           1, FALSE),
    (12, 'Tøm kaffemaskin og rengjør',               'Avkalking etter behov',                              2, FALSE),
    (12, 'Lås alkoholskap og kjeller',               'Sjekk at alt er forsvarlig låst',                   3, FALSE),
    (12, 'Sett på alarm',                            'Kontroller at alle dører er lukket',                 4, FALSE);

-- Checklist 13: Ukentlig renhold lager Nordvik (in progress)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed, completed_at) VALUES
    (13, 'Organiser tørrvarer',                      'FIFO, ryddig og merket',                             1, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
    (13, 'Vask lagergulv',                           'Feie og moppe',                                      2, TRUE, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
    (13, 'Sjekk holdbarhetsdatoer',                  'Fjern utgåtte varer',                                3, FALSE, NULL);

-- Checklist 14: Månedlig HMS Nordvik (not started)
INSERT INTO checklist_items (checklist_id, title, description, sort_order, completed) VALUES
    (14, 'Gjennomfør vernerunde',                    'Inspiser alle arbeidsstasjoner',                     1, FALSE),
    (14, 'Oppdater HMS-tavle',                       'Kontaktinfo, rutiner og hendelser',                  2, FALSE),
    (14, 'Gjennomgå sykefravær',                     'Identifiser trender og tiltak',                      3, FALSE);

-- ============================================================
-- 6. CHECKLIST COMPLETIONS
-- ============================================================
INSERT INTO checklist_completions (checklist_id, organization_id, completed_by_user_id, completed_at) VALUES
    -- Everest: Åpningsrutiner kjøkken completed several times this week
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY)),
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY)),
    (1, 1, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY)),
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),
    (1, 1, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY)),
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),
    -- Mottak av vareleveranse
    (5, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 HOUR)),
    (5, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
    (5, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),
    -- Ukentlig dyprengjøring
    (6, 1, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),
    (6, 1, 8, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 DAY)),
    -- Nordvik: Åpningsrutiner bar
    (11, 2, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 HOUR)),
    (11, 2, 7, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),
    (11, 2, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY));

-- ============================================================
-- 7. GENERAL DEVIATIONS (legacy module)
-- ============================================================
INSERT INTO deviations (organization_id, module, title, description, immediate_action, severity, status, reported_by_user_id, assigned_to_user_id, reported_at, resolved_at, closed_at) VALUES
    (1, 'IK_MAT', 'Kjøleskap 2 over grenseverdi',
     'Temperatur målt til 6.1°C kl. 08:30. Grenseverdi er 4°C. Ferske fiskeprodukter (laks, tunfisk) lagret her.',
     'Alle varer flyttet til kjøleskap 1. Tekniker kontaktet for inspeksjon av termostat.',
     'CRITICAL', 'OPEN', 3, 2,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR), NULL, NULL),

    (1, 'IK_MAT', 'Varmholding under 60°C',
     'Varmholdingsskapet i serveringsområdet viste 54°C. Sushi-ris og miso-suppe ble oppbevart her.',
     'Mat kassert. Nytt varmholdingsutstyr bestilt. Midlertidig løsning med bain-marie.',
     'HIGH', 'IN_PROGRESS', 4, 2,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), NULL, NULL),

    (1, 'IK_ALKOHOL', 'Manglende alderskontroll observert',
     'Ny servitør (Mohammed) gjennomførte ikke alderskontroll av gruppe på 4 unge gjester som bestilte sake.',
     'Gjestene ble bedt om legitimasjon i etterkant. Oppfølgingssamtale gjennomført med servitør.',
     'MEDIUM', 'IN_PROGRESS', 2, 1,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), NULL, NULL),

    (1, 'IK_MAT', 'Renholdsplan ikke fullført tirsdag',
     'Ukentlig dyprengjøring av sushikjøkken ble utsatt da Sara var syk.',
     'Gjennomført onsdag av Linh som kompensasjon. Rutine oppdatert med backup-ansvarlig.',
     'LOW', 'RESOLVED', 8, 2,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), NULL),

    (1, 'IK_MAT', 'Feil datomerking på sashimi',
     'Sashimi-porsjoner manglet datomerking etter kveldsproduksjon.',
     'Alle umerkte porsjoner kassert. Ny rutine: dobbeltsjekk av merking ved skiftbytte.',
     'MEDIUM', 'RESOLVED', 4, 2,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY)),

    (2, 'IK_ALKOHOL', 'Skjenkekontroll: beruset gjest i lokalet',
     'Kommunal kontrollør observerte en tydelig beruset gjest som ble servert øl ved bardisken.',
     'Gjesten ble bedt om å forlate lokalet. Bartender fikk muntlig advarsel.',
     'CRITICAL', 'OPEN', 6, 6,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), NULL, NULL),

    (2, 'IK_MAT', 'Manglende temperaturlogg 2 dager',
     'Temperaturlogg for kjøleskap ble ikke ført fredag og lørdag grunnet glemt rutine.',
     'Logg ført retroaktivt med estimerte verdier. Påminnelse satt opp i systemet.',
     'LOW', 'RESOLVED', 5, 6,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), NULL);

-- ============================================================
-- 8. FOOD DEVIATIONS
-- ============================================================
INSERT INTO food_deviations (organization_id, reported_at, reported_by_user_id, deviation_type, severity,
    description, immediate_action, immediate_action_by_user_id, immediate_action_at,
    cause, preventive_measures, preventive_responsible_user_id, preventive_deadline, status)
VALUES
    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR), 3,
     'TEMPERATUR', 'HIGH',
     'Kjøleskap 2 målt til 6.1°C kl. 08:30. Grenseverdi er 4°C. Laks og tunfisk for sashimi lagret her.',
     'Alle råvarer flyttet til kjøleskap 1 og 3. Tekniker varslet for inspeksjon av termostat.',
     3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 HOUR),
     'Termostat sviktet etter strømbrudd natt til onsdag. Nødstrøm koblet ikke inn kjøling.',
     'Installere temperaturalarm med SMS-varsling. Koble kjøleskap til nødstrøm.',
     2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
     'OPEN'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 2,
     'ALLERGEN', 'MEDIUM',
     'Allergenliste for «Dragon Roll» ikke oppdatert etter bytte fra sesamolje til jordnøttolje. Gjest med nøtteallergi informert feil.',
     'Rettet allergenlisten umiddelbart. Informerte gjesten og tilbød ny rett og full kompensasjon.',
     2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
     'Kokk byttet ingrediens uten å varsle kjøkkensjef. Kommunikasjonsrutine manglet.',
     'Innføre obligatorisk signering ved alle ingrediensendringer. Oppdatere allergenliste som del av menyendring-sjekkliste.',
     2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
     'UNDER_TREATMENT'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 8,
     'RENHOLD', 'LOW',
     'Ukentlig dyprengjøring av sushikjøkkenet ble utsatt fra tirsdag grunnet sykdom hos renholdspersonale.',
     'Gjennomført onsdag av kokk Linh som kompensasjon.',
     4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY),
     'Kun én person ansvarlig for dyprengjøring. Ingen backup i bemanningsplanen.',
     'Oppdatere bemanningsplan med to backup-ansvarlige for renhold. Innføre vikarordning.',
     2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 14 DAY),
     'CLOSED'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 4,
     'TEMPERATUR', 'MEDIUM',
     'Varmholdingsskap viste 54°C. Sushi-ris og miso-suppe stod i 40 minutter under 60°C.',
     'Mat kassert (ca. 4 kg ris, 8 liter suppe). Midlertidig bain-marie tatt i bruk.',
     4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY),
     'Varmholdingsskap har defekt termostat. Har vært ustabilt i flere uker.',
     'Bestille nytt varmholdingsskap. Daglig sjekk av temperatur kl. 11:00 og 15:00 inntil utskifting.',
     2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY),
     'CLOSED'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY), 4,
     'ANNET', 'MEDIUM',
     'Sashimi-porsjoner fra kveldsproduksjon manglet datomerking. 12 porsjoner funnet umerket neste morgen.',
     'Alle umerkte porsjoner kassert. Estimert svinn: 2.5 kg fisk.',
     2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY),
     'Merkerutine ble glemt under travelt kveldsskift. Ingen sjekk ved skiftbytte.',
     'Innføre obligatorisk sjekkpunkt ved skiftbytte: verifiser datomerking på alle ferdigprodukter.',
     2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY),
     'CLOSED'),

    -- Nordvik food deviations
    (2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 5,
     'TEMPERATUR', 'LOW',
     'Temperaturlogg for kjøleskap ble ikke ført fredag og lørdag. Estimert temperatur OK basert på varekvalitet.',
     'Logg ført retroaktivt. Digital påminnelse lagt inn i systemet.',
     6, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY),
     'Ansvarlig bartender glemte rutinen i travelt helg.',
     'Sette opp automatisk push-varsel kl. 08:00 og 16:00 for temperaturlogging.',
     6, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
     'UNDER_TREATMENT');

-- ============================================================
-- 9. ALCOHOL DEVIATIONS
-- ============================================================
INSERT INTO alcohol_deviations (organization_id, reported_at, reported_by_user_id, report_source,
    deviation_type, description, immediate_action, causal_analysis, causal_explanation,
    preventive_measures, preventive_deadline, preventive_responsible_user_id, status)
VALUES
    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), 2,
     'EGENRAPPORT', 'SKJENKING_APENBART_BERUSET',
     'Servitør Mohammed serverte sake til gjest som allerede viste tegn på beruselse (sløret tale, ustødig).',
     'Stoppet videre servering. Tilbød vann og mat. Gjesten forlot lokalet frivillig.',
     'MANGLENDE_OPPLAERING',
     'Mohammed er ny og hadde ikke fullført opplæring i å gjenkjenne beruselsestegn.',
     'Gjennomføre obligatorisk opplæring i ansvarlig servering for alle nye ansatte innen første uke.',
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 2,
     'UNDER_TREATMENT'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 1,
     'SJENKEKONTROLL', 'BERUSET_PERSON_I_LOKALET',
     'Kommunal skjenkekontroll avdekket at en tydelig beruset gjest satt i restaurantdelen og drakk øl.',
     'Gjesten ble umiddelbart bedt om å forlate lokalet. Personalet beklaget overfor kontrollør.',
     'RUTINE_IKKE_FULGT',
     'Bartender Erik overvåket ikke gjestene tilstrekkelig under høy aktivitet (fredagskveld).',
     'Innføre faste runder i lokalet hver 30. minutt ved kveldsarrangementer. To ansatte i bar ved helg.',
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 14 DAY), 1,
     'OPEN'),

    (1, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 DAY), 5,
     'EGENRAPPORT', 'SKJENKING_MINDREAARIGE',
     'Oppdaget at gjest som ble servert vin var 17 år. ID ble ikke sjekket da gjesten kom med en eldre gruppe.',
     'Alkohol fjernet fra bordet. Gjesten ble informert. Foreldre kontaktet.',
     'RUTINE_IKKE_FULGT',
     'Bartender antok at hele gruppen var over 18 basert på utseende til de voksne i selskapet.',
     'Obligatorisk ID-sjekk for alle i et selskap dersom én person ser ung ut. Null-toleranse-policy.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), 2,
     'CLOSED'),

    -- Nordvik alcohol deviations
    (2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 6,
     'SJENKEKONTROLL', 'BERUSET_PERSON_I_LOKALET',
     'Kommunal kontrollør observerte tydelig beruset gjest som ble servert øl ved bardisken.',
     'Gjesten ble bedt om å forlate lokalet. Bartender fikk skriftlig advarsel.',
     'RUTINE_IKKE_FULGT',
     'Stor pågang ved bardisken. Bartender prioriterte tempo over observasjon.',
     'Innføre maks antall bestillinger per runde. To ansatte i bar ved helgeåpningstider.',
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY), 6,
     'OPEN'),

    (2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), 7,
     'EGENRAPPORT', 'NEKTET_VISE_LEGITIMASJON',
     'Gjest nektet å vise legitimasjon ved bestilling av cocktail. Ble aggressiv da servitør insisterte.',
     'Servering nektet. Vakt tilkalt. Gjesten forlot lokalet etter kort diskusjon.',
     'ANNET',
     'Gjest mente det var unødvendig da vedkommende hevdet å være over 30.',
     'Opprettholde konsekvent ID-policy. Sette opp tydelig skilting om alderskontroll ved inngang.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), 6,
     'CLOSED');

-- ============================================================
-- 10. PENALTY POINTS
-- ============================================================
INSERT INTO penalty_points (organization_id, alcohol_deviation_id, points, violation_type, description) VALUES
    (1, (SELECT id FROM alcohol_deviations WHERE organization_id = 1 AND deviation_type = 'BERUSET_PERSON_I_LOKALET' LIMIT 1),
     2, 'BERUSET_PERSON_I_LOKALET', 'Skjenkekontroll: beruset person observert i lokalet'),
    (1, (SELECT id FROM alcohol_deviations WHERE organization_id = 1 AND deviation_type = 'SKJENKING_MINDREAARIGE' LIMIT 1),
     8, 'SKJENKING_MINDREAARIGE', 'Servering til mindreårig (17 år) uten ID-kontroll'),
    (2, (SELECT id FROM alcohol_deviations WHERE organization_id = 2 AND deviation_type = 'BERUSET_PERSON_I_LOKALET' LIMIT 1),
     2, 'BERUSET_PERSON_I_LOKALET', 'Kommunal kontroll: beruset gjest servert ved bar');

-- ============================================================
-- 11. TRAINING LOGS
-- ============================================================
INSERT INTO training_logs (organization_id, employee_user_id, logged_by_user_id, title, description, completed_at, expires_at, status)
VALUES
    -- Everest staff training
    (1, 3, 1, 'Brannvernkurs',
     'Årlig brannvernopplæring inkl. bruk av slukkeapparat og evakueringsrutiner.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 9 MONTH),
     'COMPLETED'),

    (1, 4, 1, 'Brannvernkurs',
     'Årlig brannvernopplæring inkl. bruk av slukkeapparat og evakueringsrutiner.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 9 MONTH),
     'COMPLETED'),

    (1, 2, 1, 'HMS-kurs for ledere',
     'HMS-opplæring for arbeidsledere. Lovpålagt etter arbeidsmiljøloven §3-5.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 20 DAY),
     'EXPIRES_SOON'),

    (1, 4, 2, 'Allergenhåndtering',
     'Opplæring i de 14 matallergener, kryssforurensning og merking iht. Mattilsynets krav.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 14 MONTH),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH),
     'EXPIRED'),

    (1, 5, 1, 'Ansvarlig vertskap',
     'Kurs i ansvarlig alkoholservering, gjenkjenning av beruselse og alderskontroll.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 MONTH),
     'COMPLETED'),

    (1, 7, 2, 'Ansvarlig vertskap',
     'Kurs i ansvarlig alkoholservering. Obligatorisk for alle som serverer alkohol.',
     NULL, NULL,
     'NOT_COMPLETED'),

    (1, 8, 2, 'Grunnkurs i mathygiene',
     'Mattilsynets grunnkurs i næringsmiddelhygiene.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 11 MONTH),
     'COMPLETED'),

    (1, 3, 2, 'Førstehjelp',
     'Grunnkurs i førstehjelp inkl. HLR og bruk av hjertestarter.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 6 MONTH),
     'COMPLETED'),

    (1, 7, 1, 'Brannvernkurs',
     'Årlig brannvernopplæring. Påmeldt neste kurs.',
     NULL, NULL,
     'NOT_COMPLETED'),

    (1, 5, 2, 'Næringsmiddelhygiene for bartendere',
     'Spesialtilpasset hygienekurs for bardrift og enkel matservering.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 MONTH),
     'COMPLETED'),

    -- Nordvik training
    (2, 6, 1, 'HMS-kurs for ledere',
     'HMS-opplæring for daglig leder. Lovpålagt etter arbeidsmiljøloven §3-5.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 4 MONTH),
     'COMPLETED'),

    (2, 5, 6, 'Brannvernkurs',
     'Årlig brannvernopplæring.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 DAY),
     'EXPIRES_SOON'),

    (2, 7, 6, 'Ansvarlig vertskap',
     'Kurs i ansvarlig alkoholservering.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 MONTH),
     DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 8 MONTH),
     'COMPLETED'),

    (2, 5, 6, 'Førstehjelp',
     'Grunnkurs i førstehjelp.',
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MONTH),
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 MONTH),
     'EXPIRED');

-- ============================================================
-- 12. TEMPERATURE APPLIANCES
-- ============================================================
INSERT INTO temperature_appliances (id, organization_id, name, appliance_type, min_temperature, max_temperature, is_active) VALUES
    -- Everest Sushi & Fusion
    (1, 1, 'Kjøleskap 1 – Fisk',       'FRIDGE',   0.00,   4.00, TRUE),
    (2, 1, 'Kjøleskap 2 – Grønnsaker',  'FRIDGE',   0.00,   4.00, TRUE),
    (3, 1, 'Kjøleskap 3 – Meieri',      'FRIDGE',   0.00,   4.00, TRUE),
    (4, 1, 'Sushidisk (kjølt)',          'FRIDGE',   0.00,   5.00, TRUE),
    (5, 1, 'Fryser 1 – Sjømat',         'FREEZER', -25.00, -18.00, TRUE),
    (6, 1, 'Fryser 2 – Generell',       'FREEZER', -25.00, -18.00, TRUE),
    -- Nordvik Bar & Kjøkken
    (7, 2, 'Barkjøleskap',              'FRIDGE',   0.00,   5.00, TRUE),
    (8, 2, 'Kjøkken kjøleskap',         'FRIDGE',   0.00,   4.00, TRUE),
    (9, 2, 'Fryser',                     'FREEZER', -25.00, -18.00, TRUE);

-- ============================================================
-- 13. TEMPERATURE MEASUREMENTS (7 days of morning + afternoon readings)
-- ============================================================
INSERT INTO temperature_measurements (organization_id, appliance_id, measured_by_user_id, measured_at, temperature, note, status) VALUES
    -- Day 1 (7 days ago) - morning
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR,   2.3, 'Morgenkontroll', 'OK'),
    (1, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR,   3.1, 'Morgenkontroll', 'OK'),
    (1, 3, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR,   2.8, 'Morgenkontroll', 'OK'),
    (1, 4, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR,   3.5, 'Morgenkontroll', 'OK'),
    (1, 5, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR, -20.5, 'Morgenkontroll', 'OK'),
    (1, 6, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR, -21.2, 'Morgenkontroll', 'OK'),
    -- Day 1 - afternoon
    (1, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 15 HOUR,  2.5, 'Ettermiddagskontroll', 'OK'),
    (1, 2, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 15 HOUR,  3.4, 'Ettermiddagskontroll', 'OK'),
    (1, 5, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 15 HOUR,-19.8, 'Ettermiddagskontroll', 'OK'),

    -- Day 2 (6 days ago)
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR,   2.1, 'Morgenkontroll', 'OK'),
    (1, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR,   3.0, 'Morgenkontroll', 'OK'),
    (1, 3, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR,   2.6, 'Morgenkontroll', 'OK'),
    (1, 4, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR,   3.8, 'Morgenkontroll', 'OK'),
    (1, 5, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR, -21.0, 'Morgenkontroll', 'OK'),
    (1, 6, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR, -20.8, 'Morgenkontroll', 'OK'),

    -- Day 3 (5 days ago)
    (1, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR,   2.4, 'Morgenkontroll', 'OK'),
    (1, 2, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR,   2.9, 'Morgenkontroll', 'OK'),
    (1, 3, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR,   3.2, 'Morgenkontroll', 'OK'),
    (1, 5, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR, -20.1, 'Morgenkontroll', 'OK'),
    (1, 6, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR, -22.0, 'Morgenkontroll', 'OK'),

    -- Day 4 (4 days ago) - deviation on fridge 2
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR,   2.6, 'Morgenkontroll', 'OK'),
    (1, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR,   5.2, 'Temperatur for høy! Termostat sjekket.', 'DEVIATION'),
    (1, 3, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR,   2.9, 'Morgenkontroll', 'OK'),
    (1, 5, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR, -19.5, 'Morgenkontroll', 'OK'),
    (1, 6, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR, -20.3, 'Morgenkontroll', 'OK'),
    -- Follow-up measurement on fridge 2 after adjustment
    (1, 2, 2, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 12 HOUR,  3.8, 'Etterkontroll etter justering', 'OK'),

    -- Day 5 (3 days ago)
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR,   2.2, 'Morgenkontroll', 'OK'),
    (1, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR,   3.5, 'Morgenkontroll – stabilisert', 'OK'),
    (1, 3, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR,   3.0, 'Morgenkontroll', 'OK'),
    (1, 4, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR,   4.1, 'Morgenkontroll', 'OK'),
    (1, 5, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR, -20.8, 'Morgenkontroll', 'OK'),
    (1, 6, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 8 HOUR, -21.5, 'Morgenkontroll', 'OK'),

    -- Day 6 (2 days ago)
    (1, 1, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR,   2.0, 'Morgenkontroll', 'OK'),
    (1, 2, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR,   3.3, 'Morgenkontroll', 'OK'),
    (1, 3, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR,   2.7, 'Morgenkontroll', 'OK'),
    (1, 5, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR, -21.1, 'Morgenkontroll', 'OK'),
    (1, 6, 3, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR, -20.0, 'Morgenkontroll', 'OK'),

    -- Day 7 (yesterday)
    (1, 1, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR,   2.4, 'Morgenkontroll', 'OK'),
    (1, 2, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR,   3.2, 'Morgenkontroll', 'OK'),
    (1, 3, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR,   2.5, 'Morgenkontroll', 'OK'),
    (1, 4, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR,   3.9, 'Morgenkontroll', 'OK'),
    (1, 5, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR, -20.3, 'Morgenkontroll', 'OK'),
    (1, 6, 4, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR, -21.8, 'Morgenkontroll', 'OK'),

    -- Today - with a deviation on fridge 2 (the current open deviation)
    (1, 1, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,  2.1, 'Morgenkontroll', 'OK'),
    (1, 2, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,  6.1, 'AVVIK: Over grenseverdi! Varer flyttes.', 'DEVIATION'),
    (1, 3, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,  2.9, 'Morgenkontroll', 'OK'),
    (1, 4, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,  3.6, 'Morgenkontroll', 'OK'),
    (1, 5, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,-20.7, 'Morgenkontroll', 'OK'),
    (1, 6, 3, CURRENT_TIMESTAMP - INTERVAL 2 HOUR,-19.9, 'Morgenkontroll', 'OK'),

    -- Nordvik measurements (past 3 days)
    (2, 7, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 9 HOUR,   3.8, 'Morgenkontroll', 'OK'),
    (2, 8, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 9 HOUR,   2.5, 'Morgenkontroll', 'OK'),
    (2, 9, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 9 HOUR, -19.4, 'Morgenkontroll', 'OK'),
    (2, 7, 7, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 9 HOUR,   4.1, 'Morgenkontroll', 'OK'),
    (2, 8, 7, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 9 HOUR,   2.8, 'Morgenkontroll', 'OK'),
    (2, 9, 7, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 9 HOUR, -20.2, 'Morgenkontroll', 'OK'),
    (2, 7, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 9 HOUR,   3.5, 'Morgenkontroll', 'OK'),
    (2, 8, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 9 HOUR,   3.0, 'Morgenkontroll', 'OK'),
    (2, 9, 5, DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 9 HOUR, -21.0, 'Morgenkontroll', 'OK');

-- ============================================================
-- 14. ALCOHOL POLICIES
-- ============================================================
INSERT INTO alcohol_policies (organization_id, bevilling_number, bevilling_valid_until,
    styrer_name, stedfortreder_name,
    kunnskapsprove_candidate_name, kunnskapsprove_birth_date, kunnskapsprove_type,
    kunnskapsprove_municipality, kunnskapsprove_passed_date,
    age_check_limit, accepted_id_types, doubt_routine,
    intoxication_signs, refusal_procedure)
VALUES
    (1, 'BEV-2024-0342', '2028-06-30',
     'Iver Sigvart Berge', 'Amina Khatri',
     'Iver Sigvart Berge', '1998-05-15', 'SKJENKE',
     'Trondheim', '2024-03-20',
     'UNDER_25',
     'PASS,FORERKORT,BANKKORT,NASJONALT_ID',
     'Ved tvil om alder: krev alltid legitimasjon. Dersom gjesten ikke kan fremvise gyldig ID, skal servering nektes. Vær høflig men bestemt.',
     'Sløret tale, ustødig gange, høylytt eller aggressiv oppførsel, sovner ved bordet, problemer med å fokusere blikket, gjentar seg selv.',
     'Si klart og vennlig at du ikke kan servere mer alkohol. Tilby vann, kaffe eller mat. Ikke diskuter. Ved aggresjon: tilkall daglig leder eller vakt.'),

    (2, 'BEV-2023-1187', '2027-12-31',
     'Kristine Haugen', 'Erik Nordmann',
     'Kristine Haugen', '1990-11-02', 'SKJENKE',
     'Trondheim', '2023-09-15',
     'UNDER_25',
     'PASS,FORERKORT,BANKKORT,NASJONALT_ID',
     'Alle gjester som ser ut til å være under 25 skal be om legitimasjon. Ingen unntak.',
     'Usammenhengende tale, vakler, søler, aggressiv, legger hodet på bordet, glassaktige øyne.',
     'Informer gjesten rolig om at videre servering ikke er mulig. Tilby alkoholfrie alternativer. Eskaler til daglig leder ved behov.');

-- ============================================================
-- 15. AGE VERIFICATION SHIFTS (past 7 days for Everest)
-- ============================================================
INSERT INTO age_verification_shifts (organization_id, user_id, shift_date, started_at, ended_at, ids_checked_count, signed_off, signed_off_at, status) VALUES
    -- 7 days ago
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 7 DAY),
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 23 HOUR,
     18, TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    (1, 7, DATE_SUB(CURDATE(), INTERVAL 7 DAY),
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 18 HOUR,
     12, TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 18 HOUR, 'COMPLETED'),

    -- 6 days ago
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 6 DAY),
     DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 23 HOUR,
     22, TRUE, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),

    -- 5 days ago (busy Friday)
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 5 DAY),
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 24 HOUR,
     31, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 24 HOUR, 'COMPLETED'),
    (1, 7, DATE_SUB(CURDATE(), INTERVAL 5 DAY),
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 20 HOUR,
     19, TRUE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 20 HOUR, 'COMPLETED'),

    -- 4 days ago (Saturday)
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 4 DAY),
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 24 HOUR,
     28, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 24 HOUR, 'COMPLETED'),
    (1, 7, DATE_SUB(CURDATE(), INTERVAL 4 DAY),
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 19 HOUR,
     15, TRUE, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 19 HOUR, 'COMPLETED'),

    -- 3 days ago
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 3 DAY),
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR,
     20, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),

    -- 2 days ago
    (1, 7, DATE_SUB(CURDATE(), INTERVAL 2 DAY),
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR,
     14, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR, 'COMPLETED'),
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 2 DAY),
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR,
     24, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),

    -- Yesterday
    (1, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY),
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 23 HOUR,
     17, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    (1, 7, DATE_SUB(CURDATE(), INTERVAL 1 DAY),
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR,
     10, TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, 'COMPLETED'),

    -- Today (active shift)
    (1, 5, CURDATE(),
     NOW() - INTERVAL 3 HOUR,
     NULL,
     6, FALSE, NULL, 'ACTIVE'),

    -- Nordvik shifts
    (2, 5, DATE_SUB(CURDATE(), INTERVAL 3 DAY),
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 17 HOUR,
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR,
     16, TRUE, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 23 HOUR, 'COMPLETED'),
    (2, 7, DATE_SUB(CURDATE(), INTERVAL 2 DAY),
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 17 HOUR,
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR,
     13, TRUE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 23 HOUR, 'COMPLETED');

-- Link some alcohol deviations to shifts
UPDATE alcohol_deviations ad
    JOIN age_verification_shifts avs ON avs.organization_id = 1
        AND avs.user_id = 7
        AND avs.shift_date = DATE_SUB(CURDATE(), INTERVAL 7 DAY)
SET ad.age_verification_shift_id = avs.id
WHERE ad.organization_id = 1
  AND ad.deviation_type = 'SKJENKING_APENBART_BERUSET';

UPDATE alcohol_deviations ad
    JOIN age_verification_shifts avs ON avs.organization_id = 1
        AND avs.user_id = 5
        AND avs.shift_date = DATE_SUB(CURDATE(), INTERVAL 5 DAY)
SET ad.age_verification_shift_id = avs.id
WHERE ad.organization_id = 1
  AND ad.deviation_type = 'BERUSET_PERSON_I_LOKALET';

-- ============================================================
-- 16. NOTIFICATIONS
-- Valid types: BEVILLING_EXPIRY, TRAINING_EXPIRY, DEVIATION_CREATED,
--              DEVIATION_ASSIGNED, CHECKLIST_OVERDUE, LOG_REMINDER, SYSTEM_ALERT
-- Valid reference types: ALCOHOL_POLICY, TRAINING_LOG, DEVIATION, CHECKLIST, AGE_VERIFICATION_LOG
-- ============================================================
INSERT INTO notifications (organization_id, recipient_id, title, message, type, reference_type, reference_id, is_read, read_at, email_sent, created_at) VALUES
    -- Unread notifications
    (1, 1, 'Kritisk temperaturavvik',
     'Kjøleskap 2 målt til 6.1°C – over grenseverdi. Varer er flyttet. Tekniker er varslet.',
     'DEVIATION_CREATED', 'DEVIATION', NULL, FALSE, NULL, FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),

    (1, 2, 'Kritisk temperaturavvik – du er ansvarlig',
     'Kjøleskap 2 målt til 6.1°C – over grenseverdi. Du er satt som ansvarlig for oppfølging.',
     'DEVIATION_ASSIGNED', 'DEVIATION', NULL, FALSE, NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)),

    (1, 2, 'HMS-kurs utløper snart',
     'Ditt HMS-kurs for ledere utløper om 20 dager. Bestill nytt kurs.',
     'TRAINING_EXPIRY', 'TRAINING_LOG', 3, FALSE, NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),

    (1, 7, 'Opplæring mangler: Ansvarlig vertskap',
     'Du har ikke fullført obligatorisk kurs i ansvarlig vertskap. Kontakt din leder for påmelding.',
     'TRAINING_EXPIRY', 'TRAINING_LOG', 6, FALSE, NULL, FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY)),

    (1, 4, 'Allergenhåndtering kurs utgått',
     'Ditt kurs i allergenhåndtering utløp for 2 måneder siden. Du må ta nytt kurs snarest.',
     'TRAINING_EXPIRY', 'TRAINING_LOG', 4, FALSE, NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),

    (1, 1, 'Ny alkoholavvik rapportert',
     'Servering til beruset gjest observert. Amina har opprettet avvikssak.',
     'DEVIATION_CREATED', 'DEVIATION', NULL, FALSE, NULL, FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),

    (1, 1, 'Sjekkliste ikke utfylt: Personlig hygiene',
     'Sjekklisten «Personlig hygiene - daglig» ble ikke fullført i går.',
     'CHECKLIST_OVERDUE', 'CHECKLIST', 3, FALSE, NULL, FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 HOUR)),

    (1, 2, 'Temperaturlogging påminnelse',
     'Ettermiddagskontroll av temperaturer er ikke utført. Vennligst loggfør innen stengetid.',
     'LOG_REMINDER', NULL, NULL, FALSE, NULL, FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 HOUR)),

    -- Read notifications
    (1, 2, 'Avvik lukket: Renholdsplan',
     'Avviket «Renholdsplan ikke fullført tirsdag» er nå lukket.',
     'DEVIATION_CREATED', 'DEVIATION', NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),

    (1, 1, 'Skjenkekontroll gjennomført',
     'Kommunal skjenkekontroll: 2 prikker ilagt. Se avvikssaken for detaljer.',
     'SYSTEM_ALERT', 'DEVIATION', NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY), TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),

    (1, 5, 'Opplæring fullført: Ansvarlig vertskap',
     'Gratulerer! Du har fullført kurset «Ansvarlig vertskap». Sertifikatet er gyldig til neste år.',
     'SYSTEM_ALERT', 'TRAINING_LOG', 5, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH), TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 MONTH)),

    (1, 8, 'Sjekkliste fullført',
     'Ukentlig dyprengjøring er markert som fullført. Bra jobba!',
     'SYSTEM_ALERT', 'CHECKLIST', 6, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), FALSE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY)),

    -- Nordvik notifications
    (2, 6, 'Kommunal skjenkekontroll – prikker',
     'Nordvik ble ilagt 2 prikker etter skjenkekontroll. Beruset gjest ble observert i lokalet.',
     'SYSTEM_ALERT', 'DEVIATION', NULL, FALSE, NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY)),

    (2, 5, 'Brannvernkurs utløper snart',
     'Ditt brannvernkurs utløper om 15 dager. Meld deg på nytt kurs.',
     'TRAINING_EXPIRY', 'TRAINING_LOG', 12, FALSE, NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY)),

    (2, 6, 'Temperaturlogg mangler',
     'Temperaturlogg ble ikke ført for fredag og lørdag. Sjekk avvikssak.',
     'LOG_REMINDER', 'DEVIATION', NULL, TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY), TRUE,
     DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY));
