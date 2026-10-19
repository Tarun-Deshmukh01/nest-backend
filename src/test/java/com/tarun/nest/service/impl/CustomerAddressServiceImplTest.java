package com.tarun.nest.service.impl;

import com.tarun.nest.dto.CustomerAddressRequest;
import com.tarun.nest.dto.CustomerAddressResponse;
import com.tarun.nest.entity.CustomerAddress;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.CustomerAddressRepository;
import com.tarun.nest.repository.PaymentOrderRepository;
import com.tarun.nest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Test
    void createsAddressOnExistingCustomerAndTrimsInput() {
        User customer = new User();
        customer.setId(10L);
        customer.setEmail("address-test@example.test");
        when(userRepository.findByEmailForUpdate(customer.getEmail()))
                .thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomerIdOrderByDefaultAddressDescIdAsc(10L))
                .thenReturn(List.of());
        when(addressRepository.countByCustomerId(10L)).thenReturn(0L);
        when(addressRepository.save(any(CustomerAddress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerAddressServiceImpl service =
                new CustomerAddressServiceImpl(
                        userRepository, addressRepository, paymentOrderRepository);
        CustomerAddressResponse response = service.createAddress(
                new CustomerAddressRequest(
                        " Home ",
                        " Recipient ",
                        " 9876543210 ",
                        " 12 Market Road ",
                        " Flat 3 ",
                        " Near Park ",
                        " Mumbai ",
                        " Maharashtra ",
                        " 400001 ",
                        " India ",
                        true
                ),
                customer.getEmail()
        );

        assertEquals("Home", response.label());
        assertEquals("Recipient", response.recipientName());
        assertEquals("9876543210", response.phoneNumber());
        assertEquals("12 Market Road", response.address());
        assertEquals("Flat 3", response.addressLine2());
        assertEquals("Near Park", response.landmark());
        assertEquals("Mumbai", response.city());
        assertEquals("Maharashtra", response.state());
        assertEquals("400001", response.pincode());
        assertEquals("India", response.country());
        org.junit.jupiter.api.Assertions.assertTrue(response.defaultAddress());
        verify(addressRepository).save(any(CustomerAddress.class));
    }

    @Test
    void listsSavedAddressesForCustomer() {
        User customer = new User();
        customer.setId(10L);
        when(userRepository.findByEmailForUpdate("address-test@example.test"))
                .thenReturn(Optional.of(customer));
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setAddress("12 Market Road");
        address.setCity("Mumbai");
        address.setPincode("400001");
        address.setCountry("India");
        when(addressRepository.findByCustomerIdOrderByDefaultAddressDescIdAsc(10L))
                .thenReturn(List.of(address));

        CustomerAddressServiceImpl service =
                new CustomerAddressServiceImpl(
                        userRepository, addressRepository, paymentOrderRepository);

        List<CustomerAddressResponse> response =
                service.getAddresses("address-test@example.test");

        assertEquals(1, response.size());
        assertEquals("12 Market Road", response.get(0).address());
    }

    @Test
    void refusesToCreateMoreThanFiveAddresses() {
        User customer = new User();
        customer.setId(10L);
        when(userRepository.findByEmailForUpdate("address-test@example.test"))
                .thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomerIdOrderByDefaultAddressDescIdAsc(10L))
                .thenReturn(List.of());
        when(addressRepository.countByCustomerId(10L)).thenReturn(5L);

        CustomerAddressServiceImpl service =
                new CustomerAddressServiceImpl(
                        userRepository, addressRepository, paymentOrderRepository);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createAddress(
                        new CustomerAddressRequest(
                                "Home", null, null, "12 Market Road",
                                null, null, "Mumbai", "Maharashtra",
                                "400001", "India", true
                        ),
                        "address-test@example.test"
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
