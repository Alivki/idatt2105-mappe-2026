-- Track each time a checklist is fully completed (all items marked done).
-- Used by the report system to calculate period-based completion rates.
CREATE TABLE checklist_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    checklist_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    completed_by_user_id BIGINT NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cc_checklist FOREIGN KEY (checklist_id) REFERENCES checklists(id) ON DELETE CASCADE,
    CONSTRAINT fk_cc_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_cc_user FOREIGN KEY (completed_by_user_id) REFERENCES users(id),
    INDEX idx_cc_checklist_completed (checklist_id, completed_at),
    INDEX idx_cc_org_completed (organization_id, completed_at)
);
