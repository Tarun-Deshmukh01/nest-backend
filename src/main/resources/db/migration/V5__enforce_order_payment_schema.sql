ALTER TABLE payment_orders
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN quantity DROP NOT NULL;

ALTER TABLE payment_orders
    DROP CONSTRAINT IF EXISTS payment_orders_status_check;

ALTER TABLE payment_orders
    ADD CONSTRAINT payment_orders_status_check
    CHECK (status IN ('CREATING', 'CREATED', 'VERIFIED', 'PAID', 'FAILED'));
