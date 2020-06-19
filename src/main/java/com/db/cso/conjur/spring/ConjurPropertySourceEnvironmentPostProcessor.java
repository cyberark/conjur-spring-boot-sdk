package com.db.cso.conjur.spring;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

public class ConjurPropertySourceEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered, ApplicationListener<ApplicationEvent> {

    private static final DeferredLog log = new DeferredLog();

    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("env :" + environment);
        System.out.println("conjur.spring.enabled :" + environment.getProperty("conjur.spring.enabled"));

        if ("true".equalsIgnoreCase(environment.getProperty("conjur.spring.enabled"))) {
            MutablePropertySources propertySources = environment.getPropertySources();
            Map<String, Object> secrets = new ConjurLookup(environment).fetchSecrets();
            if (secrets != null)
                propertySources.addFirst(new MapPropertySource("conjur", secrets));

        }
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        log.replayTo(ConjurPropertySourceEnvironmentPostProcessor.class);
    }
}
