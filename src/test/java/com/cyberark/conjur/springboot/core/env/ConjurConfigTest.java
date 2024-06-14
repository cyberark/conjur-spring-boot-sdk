package com.cyberark.conjur.springboot.core.env;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bnasslahsen
 */
@SpringBootTest(classes = SpringBootConjurAutoConfiguration.class)
@ConjurPropertySource({})
public class ConjurConfigTest {

	@Autowired
	private ConjurConfig conjurConfig;

	@Test
	public void testGetMappings() throws ApiException {
		assertEquals("vault/bnl-k8s-safe/mysql-test-db/dsn", conjurConfig.mapProperty("testUrl"));
		assertEquals("vault/bnl-k8s-safe/mysql-test-db/username", conjurConfig.mapProperty("testUsername"));
	}


}
