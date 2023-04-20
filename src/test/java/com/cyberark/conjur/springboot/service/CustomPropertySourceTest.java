package com.cyberark.conjur.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import com.cyberark.conjur.springboot.processor.SpringBootConjurAutoConfiguration;

@SpringBootTest(classes = { SpringBootConjurAutoConfiguration.class, com.cyberark.conjur.springboot.service.CustomPropertySourceTest.CustomPropertySourceConfiguration.class,
		CustomPropertySourceTest.class })
public class CustomPropertySourceTest {
	


	@Configuration
	class CustomPropertySourceConfiguration {

		@Value("${dbpassWord}")
		private byte[] dbpassWord;

		@Value("${dbuserName}")
		private byte[] dbuserName;

		@Value("${key}")
		private byte[] key;
	}

}
