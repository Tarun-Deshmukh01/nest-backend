package com.tarun.nest.repository;

import com.tarun.nest.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerIdOrderByIdAsc(Long customerId);

    long countByCustomerId(Long customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(
            Long id, Long customerId
    );
}
