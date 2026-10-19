package com.tarun.nest.controller;

import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CustomerAddressResponse;
import com.tarun.nest.service.CustomerAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    @GetMapping
    public ResponseEntity<java.util.List<CustomerAddressResponse>> getAddresses(
            Authentication authentication) {
        return ResponseEntity.ok(
                customerAddressService.getAddresses(authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<CustomerAddressResponse> createAddress(
            @Valid @RequestBody CustomerAddressRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                customerAddressService.createAddress(
                        request, authentication.getName()
                )
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody CustomerAddressRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                customerAddressService.updateAddress(
                        addressId, request, authentication.getName()
                )
        );
    }
}
