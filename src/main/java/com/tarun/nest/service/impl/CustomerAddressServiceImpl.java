package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CustomerAddressResponse;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.User;
import com.tarun.nest.exception.CustomerNotFoundException;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
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
    private final PaymentOrderRepository paymentOrderRepository;

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
        List<CustomerAddress> addresses = loadAddresses(user);
        if (addressRepository.countByCustomerId(user.getId())
                >= MAX_ADDRESSES_PER_CUSTOMER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A customer can save at most five addresses"
            );
        }

        CustomerAddress address = toEntity(request, user);
        if (addresses.isEmpty() || Boolean.TRUE.equals(request.defaultAddress())) {
            clearDefaultAddresses(addresses);
            address.setDefaultAddress(true);
        }
        return toResponse(addressRepository.save(address));
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
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            clearDefaultAddresses(loadAddresses(user));
            address.setDefaultAddress(true);
        } else if (Boolean.FALSE.equals(request.defaultAddress())
                && address.isDefaultAddress()) {
            List<CustomerAddress> addresses = loadAddresses(user);
            CustomerAddress nextDefault = addresses.stream()
                    .filter(savedAddress -> !savedAddress.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);
            if (nextDefault == null) {
                address.setDefaultAddress(true);
            } else {
                clearDefaultAddresses(addresses);
                nextDefault.setDefaultAddress(true);
            }
        }
        applyRequest(address, request);
        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, String customerEmail) {
        User user = findCustomerForUpdate(customerEmail);
        CustomerAddress address = addressRepository
                .findByIdAndCustomerId(addressId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"
                ));
        if (address.isDefaultAddress()) {
            List<CustomerAddress> remainingAddresses = loadAddresses(user).stream()
                    .filter(savedAddress -> !savedAddress.getId().equals(addressId))
                    .toList();
            address.setDefaultAddress(false);
            addressRepository.saveAndFlush(address);
            paymentOrderRepository.clearDeliveryAddressReferences(addressId);
            addressRepository.delete(address);
            if (!remainingAddresses.isEmpty()) {
                CustomerAddress nextDefault = remainingAddresses.get(0);
                nextDefault.setDefaultAddress(true);
                addressRepository.save(nextDefault);
            }
            return;
        }
        paymentOrderRepository.clearDeliveryAddressReferences(addressId);
        addressRepository.delete(address);
    }

    private User findCustomerForUpdate(String customerEmail) {
        return userRepository.findByEmailForUpdate(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found"
                ));
    }

    private List<CustomerAddress> loadAddresses(User user) {
        List<CustomerAddress> addresses = addressRepository
                .findByCustomerIdOrderByDefaultAddressDescIdAsc(user.getId());
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
        address.setRecipientName(user.getName());
        address.setPhoneNumber(user.getMobileNumber());
        address.setAddress(user.getAddress());
        address.setCity(user.getCity());
        address.setState(user.getState());
        address.setPincode(user.getPincode());
        address.setCountry(user.getCountry());
        address.setDefaultAddress(true);
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
        address.setRecipientName(trimToNull(request.recipientName()));
        address.setPhoneNumber(trimToNull(request.phoneNumber()));
        address.setAddress(request.address().trim());
        address.setAddressLine2(trimToNull(request.addressLine2()));
        address.setLandmark(trimToNull(request.landmark()));
        address.setCity(request.city().trim());
        address.setState(trimToNull(request.state()));
        address.setPincode(request.pincode().trim());
        address.setCountry(request.country().trim());
        if (request.defaultAddress() != null) {
            address.setDefaultAddress(request.defaultAddress());
        }
    }

    private CustomerAddressResponse toResponse(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.getAddress(),
                address.getAddressLine2(),
                address.getLandmark(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry(),
                address.isDefaultAddress()
        );
    }

    private void clearDefaultAddresses(List<CustomerAddress> addresses) {
        addresses.stream()
                .filter(CustomerAddress::isDefaultAddress)
                .forEach(address -> address.setDefaultAddress(false));
        addressRepository.saveAll(addresses);
        addressRepository.flush();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
