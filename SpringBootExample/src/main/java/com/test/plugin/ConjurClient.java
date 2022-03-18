package com.test.plugin;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;
import com.cyberark.conjur.springboot.annotations.ConjurValue;
import com.cyberark.conjur.springboot.annotations.ConjurValues;

@SpringBootApplication
@ConjurPropertySource(value={"db/"})
public class ConjurClient implements CommandLineRunner{

	private static Logger logger = LoggerFactory.getLogger(ConjurClient.class);

	@Value("${password}")
	private byte[] pass;

	@Value("${dbuserName}")
	private byte[] pass1;

	@Value("${dbpassWord}")
	private byte[] pass2;

	@Value("${key}")
	private byte[] pass3;

	@ConjurValue(key="db/password")
	private byte[] customVal;

	@ConjurValues(keys={"db/password","db/password"})
	private byte[] keys;

    public static void main(String[] args) {
        SpringApplication.run(ConjurClient.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		logger.info("By Using Standard Spring annotation -->  " + new String(pass) + "  " );
		logger.info("By Using Custom annotation -->"+new String(customVal));

		logger.info("By Using Standard Spring annotation -->  " + new String(pass1) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass2) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass3) + "  " );

		logger.info("By Using Custom annotation for multiple retrieval -->"+new String(keys));
		
		
	}
}

