package com.tarun.nest.service;

import com.tarun.nest.dto.AdminCustomersResponse;
import com.tarun.nest.dto.CustomerResponse;

public interface AdminCustomerService {

    AdminCustomersResponse getAllCustomers();

    CustomerResponse getCustomerById(Long customerId);

    CustomerResponse updateCustomerStatus(Long customerId, boolean active);
}