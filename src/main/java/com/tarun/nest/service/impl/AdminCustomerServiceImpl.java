package com.tarun.nest.service.impl;

import com.tarun.nest.dto.AdminCustomersResponse;
import com.tarun.nest.dto.CustomerResponse;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.exception.CustomerNotFoundException;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.AdminCustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminCustomersResponse getAllCustomers() {

        List<User> customers = userRepository.findByRole(Role.CUSTOMER);

        List<CustomerResponse> customerResponses = customers.stream()
                .map(this::mapToResponse)
                .toList();

        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        log.info("Retrieved {} customers, total count: {}",
                customerResponses.size(), totalCustomers);

        return new AdminCustomersResponse(totalCustomers, customerResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {

        User customer = findCustomerOrThrow(customerId);

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(Long customerId, boolean active) {

        User customer = findCustomerOrThrow(customerId);

        customer.setActive(active);

        User updatedCustomer = userRepository.save(customer);

        log.info("Customer {} status updated to active={}", customerId, active);

        return mapToResponse(updatedCustomer);
    }

    private User findCustomerOrThrow(Long customerId) {

        User user = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id: " + customerId
                        )
                );

        if (user.getRole() != Role.CUSTOMER) {
            throw new CustomerNotFoundException(
                    "User with id " + customerId + " is not a customer"
            );
        }

        return user;
    }

    private CustomerResponse mapToResponse(User user) {

        return new CustomerResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}