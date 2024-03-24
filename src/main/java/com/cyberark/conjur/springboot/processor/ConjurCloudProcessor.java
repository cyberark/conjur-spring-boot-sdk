package com.cyberark.conjur.springboot.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.cyberark.conjur.sdk.endpoint.SecretsApi;
import com.cyberark.conjur.springboot.core.env.ConjurConfig;
import com.cyberark.conjur.springboot.service.CustomPropertySourceChain;
import com.cyberark.conjur.springboot.service.DefaultPropertySourceChain;
import com.cyberark.conjur.springboot.service.PropertyProcessorChain;

/**
 * The ValueProcess class will be invoked on boot strap of the applicaiton and
 * will invoke the process chain based on the properties. It call the default
 * property chain if value is found or will call the Custome propertysource to
 * retrieve the value from the Conjur vault . This class in turn will invoke the
 * ConjurPropertySource to autowire the value for @Value annotation
 * 
 *
 */

public class ConjurCloudProcessor implements BeanPostProcessor, InitializingBean, EnvironmentAware {


	private ConfigurableEnvironment environment;

	private final SecretsApi secretsApi;

	private PropertyProcessorChain processorChain;

	private ConjurConfig conjurConfig;
	
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public ConjurCloudProcessor(SecretsApi secretsApi, ConjurConfig conjurConfig) {
		super();
		this.secretsApi = secretsApi;
		this.conjurConfig= conjurConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.processorChain = new DefaultPropertySourceChain("DefaultPropertySource");
		CustomPropertySourceChain customPS = new CustomPropertySourceChain("CustomPropertySource");
		processorChain.setNextChain(customPS);
		customPS.setSecretsApi(secretsApi);
		customPS.setConjurConfig(conjurConfig);
		environment.getPropertySources().addLast(processorChain);

	}

	@Override
	public void setEnvironment(Environment environment) {

		if (environment instanceof ConfigurableEnvironment) {

			this.environment = (ConfigurableEnvironment) environment;

		}

	}


}
