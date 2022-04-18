package com.cyberark.conjur.springboot.annotations;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

import com.cyberark.conjur.springboot.constant.ConjurConstant;

/**
 * 
 * This class helps to get all variables defined on the custom annotation side
 * and registers them with the ConjurPropertySource class for further processing
 * in conjur spring boot api integration plugin.
 *
 */
public class Registrar implements ImportBeanDefinitionRegistrar, BeanFactoryPostProcessor, EnvironmentAware {

	private Environment environment;

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Processes the annotations data.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
		MutablePropertySources propertySources = env.getPropertySources();

		Collection<com.cyberark.conjur.springboot.core.env.ConjurPropertySource> beans = beanFactory
				.getBeansOfType(com.cyberark.conjur.springboot.core.env.ConjurPropertySource.class).values();

		for (PropertySource<?> ps : beans) {

			if (propertySources.contains(ps.getName())) {
				continue;
			}

			propertySources.addLast(ps);
		}
	}

	/**
	 * This method finds all variables values defined at annotation side and
	 * registers then with give bean.
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		if (!registry.isBeanNameInUse(this.getClass().getName())) {
			registry.registerBeanDefinition(this.getClass().getName(),
					BeanDefinitionBuilder.genericBeanDefinition(this.getClass())
							.setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
		}

		// find container annotations
		MultiValueMap<String, Object> attributesCont = importingClassMetadata
				.getAllAnnotationAttributes(ConjurPropertySources.class.getName(), false);

		// find simple annotations
		MultiValueMap<String, Object> attributes = importingClassMetadata
				.getAllAnnotationAttributes(ConjurPropertySource.class.getName(), false);

		// resolve repeatable / container annotations
		if (attributesCont != null) {
			for (Object attribs : attributesCont.get(ConjurConstant.VALUE)) {
				makeAndRegisterBean(registry,
						(String[]) ((LinkedHashMap<String, Object>) attribs).get(ConjurConstant.VALUE),
						((AnnotationAttributes) attribs).getString(ConjurConstant.NAME));
			}
		}

		// resolve single annotations
		if (attributes != null)
			for (Object valuesObj : attributes.get(ConjurConstant.VALUE)) {
				makeAndRegisterBean(registry, (String[]) valuesObj,
						attributes.get(ConjurConstant.NAME).size() != 0
								? (String) attributes.get(ConjurConstant.NAME).get(0)
								: "");
			}
	}

	private void makeAndRegisterBean(BeanDefinitionRegistry registry, String[] values, String name) {
		for (String value : values) {
			if (!registry.containsBeanDefinition(
					com.cyberark.conjur.springboot.core.env.ConjurPropertySource.class.getName() + "-" + value
							+ "@" + name)) {
				registerBeanDefinition(registry,
						com.cyberark.conjur.springboot.core.env.ConjurPropertySource.class,
						com.cyberark.conjur.springboot.core.env.ConjurPropertySource.class.getName() + "-" + value
								+ "@" + name,
						value, name);
			}
		}
	}

	private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type, String name, String value,
			String vaultInfo) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(type);
		builder.addConstructorArgValue(value);
		builder.addConstructorArgValue(vaultInfo);
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		registry.registerBeanDefinition(name, beanDefinition);

	}

}
