ALTER TABLE users
    ADD COLUMN IF NOT EXISTS address_label VARCHAR(50),
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state VARCHAR(100),
    ADD COLUMN IF NOT EXISTS pincode VARCHAR(20),
    ADD COLUMN IF NOT EXISTS country VARCHAR(100);

CREATE TABLE IF NOT EXISTS customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    label VARCHAR(50),
    recipient_name VARCHAR(120),
    phone_number VARCHAR(30),
    address VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    landmark VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    pincode VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE customer_addresses
    ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS address_line_2 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS landmark VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer
    ON customer_addresses (customer_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_customer_addresses_one_default
    ON customer_addresses (customer_id)
    WHERE is_default;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS order_number VARCHAR(40),
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_charges NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS address_line_2 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS landmark VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_orders_order_number'
          AND conrelid = 'public.orders'::regclass
    ) THEN
        ALTER TABLE orders
            ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_orders_user_created
    ON orders (user_id, created_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_user_idempotency
    ON orders (user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS product_name_snapshot VARCHAR(255),
    ADD COLUMN IF NOT EXISTS product_image_snapshot VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS vendor_user_id_snapshot BIGINT,
    ADD COLUMN IF NOT EXISTS vendor_name_snapshot VARCHAR(255),
    ADD COLUMN IF NOT EXISTS line_total NUMERIC(12, 2);

ALTER TABLE payment_orders
    ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders(id);

ALTER TABLE payment_orders
    ADD COLUMN IF NOT EXISTS delivery_address_id BIGINT REFERENCES customer_addresses(id);

ALTER TABLE payment_orders
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN quantity DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payment_orders_order
    ON payment_orders (order_id);

CREATE TABLE IF NOT EXISTS vendor_order_fulfillments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    vendor_user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_vendor_fulfillment_order_vendor
        UNIQUE (order_id, vendor_user_id)
);

-- Retain any legacy address stored directly on the user and expose it as a saved address.
INSERT INTO customer_addresses (
    customer_id, label, recipient_name, phone_number, address, city, state,
    pincode, country, is_default
)
SELECT u.id, u.address_label, u.name, u.mobile_number, u.address, u.city,
       u.state, u.pincode, u.country, TRUE
FROM users u
WHERE u.address IS NOT NULL
  AND u.city IS NOT NULL
  AND u.pincode IS NOT NULL
  AND u.country IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM customer_addresses a WHERE a.customer_id = u.id
  );
