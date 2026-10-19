package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CustomerAddressResponse;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.User;
import com.tarun.nest.exception.CustomerNotFoundException;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.CustomerAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private static final int MAX_ADDRESSES_PER_CUSTOMER = 5;

    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;

    @Override
    @Transactional
    public List<CustomerAddressResponse> getAddresses(String customerEmail) {
        User user = findCustomerForUpdate(customerEmail);
        return loadAddresses(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomerAddressResponse createAddress(
            CustomerAddressRequest request,
            String customerEmail) {
        User user = findCustomerForUpdate(customerEmail);
        loadAddresses(user);
        if (addressRepository.countByCustomerId(user.getId())
                >= MAX_ADDRESSES_PER_CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer can save at most five addresses"
            );
        }

        return toResponse(addressRepository.save(toEntity(request, user)));
    }

    @Override
    @Transactional
    public CustomerAddressResponse updateAddress(
            Long addressId,
            CustomerAddressRequest request,
            String customerEmail) {
        User user = findCustomerForUpdate(customerEmail);
        loadAddresses(user);
        CustomerAddress address = addressRepository
                .findByIdAndCustomerId(addressId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"
                ));
        applyRequest(address, request);
        return toResponse(addressRepository.save(address));
    }

    private User findCustomerForUpdate(String customerEmail) {
        return userRepository.findByEmailForUpdate(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found"
                ));
    }

    private List<CustomerAddress> loadAddresses(User user) {
        List<CustomerAddress> addresses = addressRepository
                .findByCustomerIdOrderByIdAsc(user.getId());
        if (addresses.isEmpty() && user.hasSavedAddress()) {
            addresses = List.of(addressRepository.saveAndFlush(
                    toLegacyEntity(user)
            ));
        }
        return addresses;
    }

    private CustomerAddress toLegacyEntity(User user) {
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(user);
        address.setLabel(user.getAddressLabel());
        address.setAddress(user.getAddress());
        address.setCity(user.getCity());
        address.setState(user.getState());
        address.setPincode(user.getPincode());
        address.setCountry(user.getCountry());
        return address;
    }

    private CustomerAddress toEntity(CustomerAddressRequest request, User user) {
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(user);
        applyRequest(address, request);
        return address;
    }

    private void applyRequest(
            CustomerAddress address,
            CustomerAddressRequest request) {
        address.setLabel(trimToNull(request.label()));
        address.setAddress(request.address().trim());
        address.setCity(request.city().trim());
        address.setState(trimToNull(request.state()));
        address.setPincode(request.pincode().trim());
        address.setCountry(request.country().trim());
    }

    private CustomerAddressResponse toResponse(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getAddress(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
