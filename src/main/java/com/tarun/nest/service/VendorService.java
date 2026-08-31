package com.tarun.nest.service;

import java.util.List;


import com.tarun.nest.dto.VendorRequest;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.entity.User;

public interface VendorService {

	VendorResponse applyForApproval(VendorRequest request, User vendor);
	List<VendorResponse> getPendingVendors();
	
	List<VendorResponse> getAllVendors();

	void approveVendor(Long vendorId);

	void declineVendor(Long vendorId);
	
}