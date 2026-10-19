 package com.tarun.nest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class NestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestBackendApplication.class, args);
    }

    private static final Logger logger = LoggerFactory.getLogger(NestBackendApplication.class);

    @Bean
    public CommandLineRunner printResolvedProperties(Environment env) {
        return args -> {
            // Print resolved datasource URL and username for local debugging (do not print passwords)
            String url = env.getProperty("spring.datasource.url");
            String user = env.getProperty("spring.datasource.username");
            logger.info("Resolved spring.datasource.url={}", url);
            logger.info("Resolved spring.datasource.username={}", user);
        };
    }

    @Bean
    public CommandLineRunner ensurePaymentOrderSchema(
            JdbcTemplate jdbcTemplate) {
        return args -> {
            boolean productIdNullable = Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("""
                            SELECT NOT attnotnull
                            FROM pg_attribute
                            WHERE attrelid = 'payment_orders'::regclass
                              AND attname = 'product_id'
                              AND NOT attisdropped
                            """, Boolean.class)
            );
            boolean quantityNullable = Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("""
                            SELECT NOT attnotnull
                            FROM pg_attribute
                            WHERE attrelid = 'payment_orders'::regclass
                              AND attname = 'quantity'
                              AND NOT attisdropped
                            """, Boolean.class)
            );
            if (!productIdNullable || !quantityNullable) {
                jdbcTemplate.execute("""
                        ALTER TABLE payment_orders
                            ALTER COLUMN product_id DROP NOT NULL,
                            ALTER COLUMN quantity DROP NOT NULL
                        """);
            }

            boolean allowsCreating = Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject("""
                            SELECT EXISTS (
                                SELECT 1
                                FROM pg_constraint
                                WHERE conrelid = 'payment_orders'::regclass
                                  AND conname = 'payment_orders_status_check'
                                  AND pg_get_constraintdef(oid) LIKE '%CREATING%'
                            )
                            """, Boolean.class)
            );
            if (!allowsCreating) {
                jdbcTemplate.execute("""
                        ALTER TABLE payment_orders
                            DROP CONSTRAINT IF EXISTS payment_orders_status_check
                        """);
                jdbcTemplate.execute("""
                        ALTER TABLE payment_orders
                            ADD CONSTRAINT payment_orders_status_check
                            CHECK (
                                status IN (
                                    'CREATING', 'CREATED', 'VERIFIED',
                                    'PAID', 'FAILED'
                                )
                            )
                        """);
            }
            logger.info(
                    "Ensured payment_orders supports order-level payment attempts and CREATING status"
            );
        };
    }

}