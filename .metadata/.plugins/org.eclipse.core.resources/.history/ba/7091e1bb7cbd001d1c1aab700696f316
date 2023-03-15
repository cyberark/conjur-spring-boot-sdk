package com.cyberark.conjur.cloudTest;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = com.cyberark.conjur.springboot.domain.ConjurProperties.class)
@SpringBootTest(classes = SpringBootConjurAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")

public class ConjurCloudTest {

	private static final Logger LOGGER= LoggerFactory.getLogger(ConjurCloudTest.class);
	
	@Autowired
	private ConjurProperties conjurProperties;
	

	@Value("${jenkins-app/dbUserName}")
	private byte[] pass1;

	@Value("${jenkins-app/dbPassword}")
	private byte[] pass2;

	@Value("${jenkins-app/dbUrl}")
	private byte[] pass3;

	/*
	 * @Test public void contextLoads() {
	 * 
	 * }
	 */
	@Test
	void checkForConfiguration() {
//		assertEquals("myConjurAccount", conjurProperties.getAccount());	
		assertNotNull(conjurProperties.getAccount());
		assertNotNull(conjurProperties.getApplianceUrl());
		assertNotNull(conjurProperties.getAuthnLogin());
		assertNotNull(conjurProperties.getAuthTokenFilePath());
		assertNotNull(conjurProperties.getCertFile());
		assertNotNull(conjurProperties.getSslCertificate());
		
	}
	

	@Test
	void checkForConnection() {
		assertNotNull(ConjurConnectionManager.getInstance());

	}
	
	@Test
	void checkForUserName()
	{
//		System.out.println("Pass1 value is:"+ pass1);
		LOGGER.debug("Pass1 value is:"+ pass1);
		String pass1Str = new String(pass1);
		assertNotNull(pass1Str);
//		assertEquals("Maggiee2210", pass1Str);
	}
	@Test
	void checkForPassword()
	{
//		System.out.println("Pass2 value is:"+ pass2);
		LOGGER.debug("Pass2 value is:"+ pass2);
		String pass2Str = new String(pass2);
		assertNotNull(pass2Str);
		
//		assertEquals("secret", pass2Str);
	}
	@Test
	void checkForUrl()
	{
//		System.out.println("Pass3 value is:"+ pass3);
		LOGGER.debug("Pass3 value is:"+ pass3);
		String pass3Str = new String(pass3);
		assertNotNull(pass3Str);
//		assertEquals("https://www.logo.com", pass3Str);
	}

}
