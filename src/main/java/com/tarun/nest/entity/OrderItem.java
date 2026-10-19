package com.tarun.nest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "product_name_snapshot", length = 255)
    private String productNameSnapshot;

    @Column(name = "product_image_snapshot", length = 1000)
    private String productImageSnapshot;

    @Column(name = "vendor_user_id_snapshot")
    private Long vendorUserIdSnapshot;

    @Column(name = "vendor_name_snapshot", length = 255)
    private String vendorNameSnapshot;

    @Column(name = "line_total", precision = 12, scale = 2)
    private BigDecimal lineTotal;
}