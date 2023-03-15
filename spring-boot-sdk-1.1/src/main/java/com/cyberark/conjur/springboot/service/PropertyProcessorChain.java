package com.cyberark.conjur.springboot.service;


import org.springframework.core.env.EnumerablePropertySource;
/**
 * 
 * PropertySource to be used as a placeholder in cases where an actual property source cannot be eagerly 
 * initialized at application context creation time.
 *
 */


public abstract class PropertyProcessorChain extends EnumerablePropertySource<Object> {
	
//	private static final Logger LOGGER= LoggerFactory.getLogger(PropertyProcessorChain.class);

	private PropertyProcessorChain processorChain;

	

	public PropertyProcessorChain(String name) {	
		super("propertyProcessorChain");
	}
	

	
    public void setNextChain(PropertyProcessorChain processChain) {
		this.processorChain = new DefaultPropertySourceChain("DefaultPropertyChain");

		CustomPropertySourceChain customPS = new CustomPropertySourceChain("CustomerPropertyChain");
		processorChain.setNextChain(customPS);
	}

	
	
	 @Override
	 public Object getProperty(String name) {
		return name;
	}

}
