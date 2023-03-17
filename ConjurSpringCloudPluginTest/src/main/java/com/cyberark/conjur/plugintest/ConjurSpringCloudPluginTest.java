package com.cyberark.conjur.plugintest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class ConjurSpringCloudPluginTest implements CommandLineRunner{
	private static Logger logger = LoggerFactory.getLogger(ConjurSpringCloudPluginTest.class);

	
	@Value("${uid}")
	private byte[] pass1;

	@Value("${password}")
	private byte[] pass2;

	@Value("${url}")
	private byte[] pass3;

	
	@Autowired
	ApplicationContext appContext;
	
    public static void main(String[] args) {
    	
        SpringApplication.run(ConjurSpringCloudPluginTest.class, args);
    }

    
	public void run(String... args) throws Exception {
		
		logger.info("By Using Standard Spring annotation -->  " + new String(pass1) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass2) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass3) + "  " );
	
	}

	
}
