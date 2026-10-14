package com.tarun.nest;

import com.tarun.nest.dto.VendorRequest;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.entity.UserNotification;
import com.tarun.nest.repository.UserNotificationRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.repository.VendorRepository;
import com.tarun.nest.service.RealtimeNotificationService;
import com.tarun.nest.service.VendorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.allowed-origins=https://nest-frontend-blue.vercel.app,http://localhost:5173")
@AutoConfigureMockMvc
class NestBackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VendorService vendorService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private UserNotificationRepository notificationRepository;

	@Autowired
	private RealtimeNotificationService notificationService;

	@Test
	void contextLoads() {
	}

	@Test
	void corsPreflightAllowsConfiguredOriginsBeforeAuthentication() throws Exception {
		assertPreflightAllowed("https://nest-frontend-blue.vercel.app");
		assertPreflightAllowed("http://localhost:5173");
	}

	private void assertPreflightAllowed(String origin) throws Exception {
		mockMvc.perform(options("/api/notifications")
				.header(HttpHeaders.ORIGIN, origin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
			.andExpect(status().isOk())
			.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
	}

	@Test
	@Transactional
	void vendorApplicationAndAdminNotificationArePersistedIdempotently() {
		String suffix = UUID.randomUUID().toString();
		User admin = user("admin-" + suffix + "@example.test", Role.ADMIN);
		User vendor = user("vendor-" + suffix + "@example.test", Role.VENDOR);
		admin = userRepository.saveAndFlush(admin);
		vendor = userRepository.saveAndFlush(vendor);

		VendorResponse response = vendorService.applyForApproval(vendorRequest(), vendor);
		assertEquals("PENDING", response.getStatus());
		assertNotNull(vendorRepository.findById(response.getId()).orElse(null));

		String eventKey = "vendor-application:" + response.getId();
		UserNotification persisted = notificationRepository
				.findByRecipient_IdAndEventKey(admin.getId(), eventKey)
				.orElseThrow();
		assertEquals(response.getId(), persisted.getRelatedVendorId());
		assertFalse(persisted.isReadStatus());

		notificationService.notifyAdminsOfVendorApplication(response.getId(), vendor.getName());
		assertEquals(1, notificationRepository.countByRecipient_IdAndReadStatusFalse(admin.getId()));
	}

	private User user(String email, Role role) {
		User user = new User();
		user.setName("Notification Test User");
		user.setEmail(email);
		user.setMobileNumber(UUID.randomUUID().toString());
		user.setPassword("not-a-real-password");
		user.setRole(role);
		return user;
	}

	private VendorRequest vendorRequest() {
		VendorRequest request = new VendorRequest();
		VendorRequest.CompanyDetail company = new VendorRequest.CompanyDetail();
		company.setCompanyname("Notification Test Shop");
		request.setCompanyDetail(company);
		request.setSocialLinks(new VendorRequest.SocialLinks());
		request.setBusinessAddress(new VendorRequest.BusinessAddress());
		return request;
	}

}
