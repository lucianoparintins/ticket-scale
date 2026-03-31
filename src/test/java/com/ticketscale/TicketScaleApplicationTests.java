package com.ticketscale;

import com.ticketscale.application.port.out.LockManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TicketScaleApplicationTests {

	@MockitoBean
	private LockManager lockManager;

	@Test
	void contextLoads() {
	}

}
