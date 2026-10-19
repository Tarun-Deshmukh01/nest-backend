package com.tarun.nest.repository;

import com.tarun.nest.entity.VendorOrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface VendorOrderFulfillmentRepository
        extends JpaRepository<VendorOrderFulfillment, Long> {

    Optional<VendorOrderFulfillment> findByOrderIdAndVendorId(
            Long orderId, Long vendorId
    );

    List<VendorOrderFulfillment> findByOrderId(Long orderId);
}
