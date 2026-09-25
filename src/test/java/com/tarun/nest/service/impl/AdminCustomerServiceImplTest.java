package com.tarun.nest.service.impl;

import com.tarun.nest.dto.AdminCustomersResponse;
import com.tarun.nest.dto.CustomerResponse;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.exception.CustomerNotFoundException;
import com.tarun.nest.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCustomerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminCustomerServiceImpl adminCustomerService;

    private User createUser(Long id, String name, String email, Role role, Boolean active) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setMobileNumber("+91 98765 43210");
        user.setRole(role);
        user.setActive(active);
        user.setCreatedAt(LocalDateTime.of(2026, 9, 20, 10, 30));
        user.setUpdatedAt(LocalDateTime.of(2026, 9, 20, 10, 30));
        return user;
    }

    @Test
    void getAllCustomers_returnsOnlyCustomersAndTotalCount() {

        User customer = createUser(1L, "Aarav Sharma", "aarav@example.com", Role.CUSTOMER, true);
        User vendor = createUser(2L, "Vendor One", "vendor@example.com", Role.VENDOR, true);

        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(List.of(customer));
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(1L);

        AdminCustomersResponse response = adminCustomerService.getAllCustomers();

        assertThat(response.getTotalCustomers()).isEqualTo(1L);
        assertThat(response.getCustomers()).hasSize(1);
        CustomerResponse body = response.getCustomers().get(0);
        assertThat(body.getId()).isEqualTo(1L);
        assertThat(body.getName()).isEqualTo("Aarav Sharma");
        assertThat(body.getEmail()).isEqualTo("aarav@example.com");
        assertThat(body.getMobileNumber()).isEqualTo("+91 98765 43210");
        assertThat(body.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(body.getActive()).isTrue();

        verify(userRepository).findByRole(Role.CUSTOMER);
        verify(userRepository).countByRole(Role.CUSTOMER);
    }

    @Test
    void getAllCustomers_handlesEmptyCustomerList() {

        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(Collections.emptyList());
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(0L);

        AdminCustomersResponse response = adminCustomerService.getAllCustomers();

        assertThat(response.getTotalCustomers()).isZero();
        assertThat(response.getCustomers()).isEmpty();
    }

    @Test
    void getCustomerById_returnsCustomer() {

        User customer = createUser(1L, "Aarav Sharma", "aarav@example.com", Role.CUSTOMER, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = adminCustomerService.getCustomerById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("aarav@example.com");
    }

    @Test
    void getCustomerById_throwsWhenCustomerNotFound() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCustomerService.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void getCustomerById_throwsWhenUserIsNotACustomer() {

        User vendor = createUser(2L, "Vendor One", "vendor@example.com", Role.VENDOR, true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(vendor));

        assertThatThrownBy(() -> adminCustomerService.getCustomerById(2L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("not a customer");
    }

    @Test
    void updateCustomerStatus_updatesActiveFlag() {

        User customer = createUser(1L, "Aarav Sharma", "aarav@example.com", Role.CUSTOMER, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(customer)).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = adminCustomerService.updateCustomerStatus(1L, false);

        assertThat(response.getActive()).isFalse();
        verify(userRepository).save(customer);
    }
}