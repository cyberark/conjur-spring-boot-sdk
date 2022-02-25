package com.cyberark.conjur.springboot.clientApp;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;
import com.cyberark.conjur.springboot.annotations.ConjurValue;

@SpringBootApplication
@ConjurPropertySource("jenkinsapp/") 
//@ConjurPropertySource("jenkinsapp1/")
//@ConjurPropertySource(value={"jenkinsapp1/", "jenkinsapp2/", "jenkinsapp3/", "jenkinsapp4/"}, name="vault2")//multi vault support to be added later
public class ConjurClient implements CommandLineRunner{
	
	private static Logger logger = LoggerFactory.getLogger(ConjurClient.class);

	
	@Value("${uid}")
	private String value;

	@Value("${password}")
	private String pass;
	
	 
//	@Value("${keyVal}")
//	private String keyVal;
//	
	@ConjurValue(key="jenkinsapp/dbuserName")
	private String customVal;
	
    public static void main(String[] args) {
    	
    	
        SpringApplication.run(ConjurClient.class, args);
    }

	@Override
	public void run(String... args) throws Exception {
		logger.info("By Using Standard Spring annotation --> " + value + " " + pass + "  " );
		logger.info("By Using Custom annotation -->"+customVal);

	}
}