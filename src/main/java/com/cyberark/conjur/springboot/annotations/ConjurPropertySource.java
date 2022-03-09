package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link ConjurPropertySource} to Spring Boot Application.
 * <h3>Example usage</h3>
 * <p>
 * Given a Vault path {@code policy/my-application} containing the configuration
 * data pair {@code database.password=mysecretpassword}, the following
 * {@code @Configuration} class uses {@code ConjurPropertySource).
 *
 * 
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ConjurPropertySource(&quot;policy/my-application&quot;)
 * public class AppConfig {
 *
 * 	&#064;Value("${database.password:notFound}")
 * 	private String password;
 * 
 * 	&#064;Bean
 * 	public TestBean testBean() {
 * 		TestBean testBean = new TestBean();
 * 		testBean.setPassword(password));
 * 		return testBean;
 * 	}
 * }
 * </pre>
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConjurPropertySources.class)
@Import(Registrar.class)
public @interface ConjurPropertySource {
	/**
	 * Indicates the name of query vault.
	 * 
	 */
	String name() default "";

	/**
	 * Indicate the Vault path(s) of the policy to be retrieved. For example,
	 * {@code "policy/my-application/db.userName"}.
	 *
	 */
	String[] value();

	boolean ignoreResourceNotFound() default false;
}