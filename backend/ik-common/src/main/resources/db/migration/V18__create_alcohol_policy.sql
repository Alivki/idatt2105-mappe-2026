CREATE TABLE alcohol_policies (
    id                            BIGINT       NOT NULL AUTO_INCREMENT,
    organization_id               BIGINT       NOT NULL UNIQUE,

    -- Bevillingsinformasjon
    bevilling_number              VARCHAR(50),
    bevilling_valid_until         DATE,
    styrer_name                   VARCHAR(255),
    stedfortreder_name            VARCHAR(255),
    bevilling_document_id         BIGINT,

    -- Kunnskapsprove
    kunnskapsprove_candidate_name VARCHAR(255),
    kunnskapsprove_birth_date     DATE,
    kunnskapsprove_type           VARCHAR(20), -- SALG, SKJENKE, BOTH
    kunnskapsprove_municipality   VARCHAR(255),
    kunnskapsprove_passed_date    DATE,
    kunnskapsprove_document_id    BIGINT,

    -- Alderskontroll
    age_check_limit               VARCHAR(10)  NOT NULL DEFAULT 'UNDER_25',
    accepted_id_types             VARCHAR(255) NOT NULL DEFAULT 'PASS,FORERKORT,BANKKORT,NASJONALT_ID',
    doubt_routine                 TEXT,

    -- Ansvarlig servering
    intoxication_signs            TEXT,
    refusal_procedure             TEXT,

    -- Notification tracking
    expiry_notified               BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at                    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_alcohol_policies_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_policy_bevilling_doc FOREIGN KEY (bevilling_document_id) REFERENCES documents (id) ON DELETE SET NULL,
    CONSTRAINT fk_policy_kunnskap_doc FOREIGN KEY (kunnskapsprove_document_id) REFERENCES documents (id) ON DELETE SET NULL
);