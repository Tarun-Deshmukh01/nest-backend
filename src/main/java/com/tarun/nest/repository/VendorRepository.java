package com.tarun.nest.repository;

import com.tarun.nest.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
	List<Vendor> findByStatus(String status);
}