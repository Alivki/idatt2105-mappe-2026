CREATE TABLE generated_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    period_from DATE NOT NULL,
    period_to DATE NOT NULL,
    generated_by_user_id BIGINT NOT NULL,
    document_id BIGINT,
    config JSON NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_report_user FOREIGN KEY (generated_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_report_document FOREIGN KEY (document_id) REFERENCES documents(id),
    INDEX idx_report_org (organization_id),
    INDEX idx_report_generated_at (generated_at)
);
