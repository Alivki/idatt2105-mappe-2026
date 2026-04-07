-- ============================================================
-- Age Verification Shifts
-- Tracks staff shift-based age verification (ID check counting)
-- Links to alcohol_deviations for incidents during shifts
-- ============================================================

CREATE TABLE age_verification_shifts (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id     BIGINT       NOT NULL,
    user_id             BIGINT       NOT NULL,
    shift_date          DATE         NOT NULL,
    started_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at            TIMESTAMP    NULL,
    ids_checked_count   INT          NOT NULL DEFAULT 0,
    signed_off          BOOLEAN      NOT NULL DEFAULT FALSE,
    signed_off_at       TIMESTAMP    NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_avs_org  FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_avs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_avs_org_date_user (organization_id, shift_date, user_id)
);

CREATE INDEX idx_avs_org_date ON age_verification_shifts(organization_id, shift_date);
CREATE INDEX idx_avs_user_status ON age_verification_shifts(user_id, status);

-- Optional FK from alcohol_deviations to link deviations created during a shift
ALTER TABLE alcohol_deviations
    ADD COLUMN age_verification_shift_id BIGINT NULL,
    ADD CONSTRAINT fk_ad_avs FOREIGN KEY (age_verification_shift_id)
        REFERENCES age_verification_shifts(id) ON DELETE SET NULL;
