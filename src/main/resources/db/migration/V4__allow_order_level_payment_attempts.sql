ALTER TABLE payment_orders
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN quantity DROP NOT NULL;
