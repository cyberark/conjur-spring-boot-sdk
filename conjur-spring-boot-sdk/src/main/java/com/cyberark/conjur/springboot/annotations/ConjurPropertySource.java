package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * The @ConjurPropertySource annotation allows you to specify the root of a policy to look up. 
 * The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API that we expose.
 * Using @ConjurPropertySource in conjunction with @Configuration classes is required.
 * The names of secrets, passwords, and user IDs all remain as originally specified.
 * <h2>Example usage</h2>
 * <p>
 * Given a vault's path {@code policy/my-application} containing the configuration
 * data pair {@code database.password=mysecretpassword}, the following
 * {@code @Configuration} class uses {@code ConjurPropertySource)}.
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
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConjurPropertySources.class)
@Import(Registrar.class)
public @interface ConjurPropertySource {
	/**
	 * Indicates the name of query vault.
	 * 
	 * @return  name of query vault.
	 */
	String name() default "";

	/**
	 * Indicate the Vault path(s) of the policy to be retrieved. For example,
	 * {@code "policy/my-application/db.userName"}.
	 *
	 * @return Vault path(s)
	 */
	String[] value();

	/**
	 * 
	 * @return false.
	 */
	boolean ignoreResourceNotFound() default false;
}
