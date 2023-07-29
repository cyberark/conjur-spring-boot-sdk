package com.cyberark.conjur.springboot.processor;

import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ConjurCloudProcessorScanTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withUserConfiguration(SampleApp.class);

	private final WebApplicationContextRunner contextRunnerConjurPropertySource = new WebApplicationContextRunner()
			.withUserConfiguration(SampleAppConjurPropertySource.class);
	@Test
	public void scanning_not_loaded_by_default() {
		contextRunner
				.run(context -> assertThat(context)
						.hasNotFailed()
						.doesNotHaveBean("conjurCloudProcessor")
				);
	}

	@Test
	public void scanning_loaded_explicitly() {
		contextRunner
				.withPropertyValues("conjur.scan-all-values=true")
				.run(context -> assertThat(context)
						.hasNotFailed()
						.hasBean("conjurCloudProcessor")
				);
	}

	@Test
	public void scanning_not_loaded_by_if_conjur_property_source_present() {
		contextRunnerConjurPropertySource
				.withPropertyValues("conjur.scan-all-values=true")
				.run(context -> assertThat(context)
						.hasNotFailed()
						.doesNotHaveBean("conjurCloudProcessor")
				);
	}
	
	@EnableAutoConfiguration
	static class SampleApp {}

	@EnableAutoConfiguration
	@ConjurPropertySource(value = "test")
	static class SampleAppConjurPropertySource {}	
}
