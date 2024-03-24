package com.cyberark.conjur.springboot.core.env;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import com.cyberark.conjur.springboot.constant.ConjurConstant;
import com.cyberark.conjur.springboot.domain.ConjurProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;

import static com.cyberark.conjur.springboot.constant.ConjurConstant.CONJUR_PREFIX;

/**
 * 
 * This class loads the external configured conjur.properties file and resolves
 * the keys values defined in properties file.
 *
 */
public class ConjurConfig implements EnvironmentAware, BeanFactoryPostProcessor {

	private static final Properties PROPS = new Properties();

	private static final Logger LOGGER = LoggerFactory.getLogger(ConjurConfig.class);
	/**
	 * The Environment.
	 */
	private Environment environment;
	
	/**
	 *
	 * @param name - key define at given property file.
	 * @return - corresponding value of key defined at given property file.
	 */
	public String mapProperty(String name) {
		String mapped = PROPS.getProperty(ConjurConstant.CONJUR_MAPPING + name);
		return mapped != null ? mapped : name;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		final BindResult<ConjurProperties> result = Binder.get(environment).bind(CONJUR_PREFIX, ConjurProperties.class);
		if (result.isBound()) {
			loadMappingProps(result);
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	private void loadMappingProps(BindResult<ConjurProperties> result) {
		String mappingPath = result.get().getMappingPath();
		InputStream propsFile = null;
		if (mappingPath != null) {
			try {
				File file = ResourceUtils.getFile(mappingPath);
				propsFile = Files.newInputStream(file.toPath());
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		else {
			propsFile = ConjurConfig.class.getResourceAsStream(ConjurConstant.CONJUR_PROPERTIES);
		}

		if (propsFile != null) {
			try {
				PROPS.load(propsFile);
			}
			catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			finally {
				try {
					propsFile.close();
				}
				catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
}
