package com.tarun.nest.service;

import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CustomerAddressResponse;

import java.util.List;

public interface CustomerAddressService {

    List<CustomerAddressResponse> getAddresses(String customerEmail);

    CustomerAddressResponse createAddress(
            CustomerAddressRequest request,
            String customerEmail
    );

    CustomerAddressResponse updateAddress(
            Long addressId,
            CustomerAddressRequest request,
            String customerEmail
    );
}
