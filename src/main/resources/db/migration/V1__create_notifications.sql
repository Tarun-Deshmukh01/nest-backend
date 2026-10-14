CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id),
    notification_type VARCHAR(60) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    related_vendor_id BIGINT REFERENCES vendors(id),
    event_key VARCHAR(160),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    href VARCHAR(300),
    CONSTRAINT uk_notifications_recipient_event UNIQUE (recipient_id, event_key)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_notifications_related_vendor'
          AND conrelid = 'public.notifications'::regclass
    ) THEN
        ALTER TABLE notifications
            ADD CONSTRAINT fk_notifications_related_vendor
            FOREIGN KEY (related_vendor_id) REFERENCES vendors(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created
    ON notifications (recipient_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_unread
    ON notifications (recipient_id, is_read);