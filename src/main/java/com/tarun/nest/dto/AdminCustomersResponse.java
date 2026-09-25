package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCustomersResponse {

    private long totalCustomers;

    private List<CustomerResponse> customers;
}