-- Apply explicitly before enabling the asynchronous Voice Report consumer.
-- Existing rows predate job tracking and therefore represent completed reports.
ALTER TABLE voice_report
    ADD COLUMN processing_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN media_object_key VARCHAR(512) NULL,
    ADD COLUMN failure_reason VARCHAR(1000) NULL,
    ADD COLUMN processing_started_at DATETIME(6) NULL,
    ADD COLUMN completed_at DATETIME(6) NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_voice_report_job_recovery
    ON voice_report (processing_status, processing_started_at);
