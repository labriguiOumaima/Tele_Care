package com.telecare.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest(properties={"spring.cloud.config.enabled=false","spring.config.import="})
class EurekaServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
