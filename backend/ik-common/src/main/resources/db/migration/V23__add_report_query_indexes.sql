-- Composite indexes for report queries that filter on (organization_id, date_column).
-- Single-column indexes already exist on each column individually, but MySQL can only
-- use one index per table access. Composite indexes let the engine satisfy both the
-- org filter and the date range scan in a single B-tree lookup.

-- temperature_measurements: findMeasurements, countTempDeviations
CREATE INDEX idx_temp_meas_org_measured_at
    ON temperature_measurements (organization_id, measured_at);

CREATE INDEX idx_temp_meas_org_measured_at_status
    ON temperature_measurements (organization_id, measured_at, status);

-- food_deviations: findFoodDeviations, countFoodDeviationsByStatus
CREATE INDEX idx_food_dev_org_reported_at
    ON food_deviations (organization_id, reported_at);

CREATE INDEX idx_food_dev_org_reported_at_status
    ON food_deviations (organization_id, reported_at, status);

-- alcohol_deviations: findAlcoholDeviations, countAlcoholDeviationsByStatus, countAlcoholDeviationsByDate
CREATE INDEX idx_alcohol_dev_org_reported_at
    ON alcohol_deviations (organization_id, reported_at);

CREATE INDEX idx_alcohol_dev_org_reported_at_status
    ON alcohol_deviations (organization_id, reported_at, status);
