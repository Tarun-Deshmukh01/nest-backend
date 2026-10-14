package com.tarun.nest.service.impl;

import com.tarun.nest.dto.VendorRequest;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.Vendor;
import com.tarun.nest.repository.VendorRepository;
import com.tarun.nest.service.RealtimeNotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorServiceImplTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private RealtimeNotificationService notificationService;

    @InjectMocks
    private VendorServiceImpl vendorService;

    @Test
    void applicationIsSavedPendingAndNotifiesAdminsForItsPersistedId() {
        User user = new User();
        user.setId(45L);
        user.setName("Vendor User");
        VendorRequest request = request();
        when(vendorRepository.findByUserIdAndStatusOrderByIdDesc(45L, "PENDING"))
                .thenReturn(List.of());
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> {
            Vendor saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });

        VendorResponse response = vendorService.applyForApproval(request, user);

        Assertions.assertEquals(77L, response.getId());
        Assertions.assertEquals("PENDING", response.getStatus());
        verify(notificationService).notifyAdminsOfVendorApplication(77L, "Vendor User");
    }

    private VendorRequest request() {
        VendorRequest request = new VendorRequest();
        VendorRequest.CompanyDetail company = new VendorRequest.CompanyDetail();
        company.setCompanyname("Example Shop");
        request.setCompanyDetail(company);
        request.setSocialLinks(new VendorRequest.SocialLinks());
        request.setBusinessAddress(new VendorRequest.BusinessAddress());
        return request;
    }
}