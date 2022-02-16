package com.cyberark.core.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConjurConfig {
	
	private static Properties props = new Properties();
	
    private static ConjurConfig uniqueInstance = new ConjurConfig();
    
    private ConjurConfig() {
    	
    	InputStream propsFile = ConjurConfig.class.getResourceAsStream("/conjur.properties");
    	
    	if (propsFile != null) {
    		try {
				props.load(propsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		try {
				propsFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    public static ConjurConfig getInstance() {
        return uniqueInstance;
    }
	
	public String mapProperty(String name) {
		String mapped = props.getProperty("conjur.mapping."+name);
		
		return mapped != null ? mapped : name;
	}
}
