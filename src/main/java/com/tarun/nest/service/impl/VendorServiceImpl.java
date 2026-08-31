package com.tarun.nest.service.impl;

import com.tarun.nest.dto.VendorRequest;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.Vendor;
import com.tarun.nest.repository.VendorRepository;
import com.tarun.nest.service.VendorService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    @Override
    public VendorResponse applyForApproval(
            VendorRequest request,
            User user
    ) {

        Vendor vendor = new Vendor();

        // Company details
        vendor.setCompanyname(
                request.getCompanyDetail().getCompanyname()
        );

        vendor.setSinceyear(
                request.getCompanyDetail().getSinceyear()
        );

        vendor.setAbout(
                request.getCompanyDetail().getAbout()
        );

        // Social links
        vendor.setFb(
                request.getSocialLinks().getFb()
        );

        vendor.setInsta(
                request.getSocialLinks().getInsta()
        );

        vendor.setTwitter(
                request.getSocialLinks().getTwitter()
        );
        
        vendor.setPinterest(
				request.getSocialLinks().getPinterest()
		);

        // Business address
        vendor.setAddress(
                request.getBusinessAddress().getAddress()
        );

        vendor.setCity(
                request.getBusinessAddress().getCity()
        );

        vendor.setState(
                request.getBusinessAddress().getState()
        );

        vendor.setCountry(
                request.getBusinessAddress().getCountry()
        );

        vendor.setZipcode(
                request.getBusinessAddress().getZipcode()
        );

        // Initial status
        vendor.setStatus("PENDING");

        // Associate approval request with logged-in user
        vendor.setUser(user);

        Vendor savedVendor = vendorRepository.save(vendor);

        return mapToResponse(savedVendor);
    }


    // Entity -> Response
    private VendorResponse mapToResponse(Vendor vendor) {

        return new VendorResponse(

                vendor.getId(),

                new VendorResponse.CompanyDetail(
                        vendor.getCompanyname(),
                        vendor.getSinceyear(),
                        vendor.getAbout()
                ),

                new VendorResponse.SocialLinks(
                        vendor.getFb(),
                        vendor.getInsta(),
                        vendor.getTwitter(),
                        vendor.getPinterest()
                ),

                new VendorResponse.BusinessAddress(
                        vendor.getAddress(),
                        vendor.getCity(),
                        vendor.getState(),
                        vendor.getCountry(),
                        vendor.getZipcode(),
                        vendor.getUser().getMobileNumber()
                ),

                vendor.getStatus()
        );
    }
    @Override
    public List<VendorResponse> getPendingVendors() {

        List<Vendor> vendors =
                vendorRepository.findByStatus("PENDING");

        return vendors.stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Override
    public void approveVendor(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        if (!"PENDING".equalsIgnoreCase(vendor.getStatus())) {
            throw new RuntimeException(
                    "Only pending vendors can be approved"
            );
        }

        vendor.setStatus("APPROVED");

        vendorRepository.save(vendor);
    }
    @Override
    public void declineVendor(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new RuntimeException("Vendor not found"));

        if (!"PENDING".equalsIgnoreCase(vendor.getStatus())) {
            throw new RuntimeException(
                    "Only pending vendors can be declined"
            );
        }

        vendor.setStatus("REJECTED");

        vendorRepository.save(vendor);
    }
    @Override
    public List<VendorResponse> getAllVendors() {

        List<Vendor> vendors = vendorRepository.findAll();

        return vendors.stream()
                .map(this::mapToResponse)
                .toList();
    }
}