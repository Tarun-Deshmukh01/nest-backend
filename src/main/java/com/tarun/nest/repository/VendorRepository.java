package com.tarun.nest.repository;

import com.tarun.nest.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByStatus(String status);

    long countByStatus(String status);

    Optional<Vendor> findByUserId(Long userId);
}