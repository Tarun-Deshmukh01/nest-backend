ALTER TABLE payment_orders
    DROP CONSTRAINT IF EXISTS payment_orders_status_check;

ALTER TABLE payment_orders
    ADD CONSTRAINT payment_orders_status_check
    CHECK (status IN ('CREATING', 'CREATED', 'VERIFIED', 'PAID', 'FAILED'));
