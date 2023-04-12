package com.cyberark.conjur.springboot.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @ConjurValue annotation is a Conjur native annotation intended for new Spring Boot applications.
 * Injecting the annotation into your Spring Boot code allows you to retrieve a single secret from the Conjur Vault.
 * <h2>Example usage:</h2>
 * <p>
 * Given the path {@code policy/my-application/database.password} containing
 * the configuration data pair {@code database.password=mysecretpassword}, the
 * following {@code @Configuration} class uses {@code ConjurValue)}.
 *
 * 
 *  <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 * 	&#064;ConjurValues(&quot;policy/my-application/database.password&quot;)
 * 	private String password;
 * 
 * 	&#064;Bean
 * 	public TestBean testBean() {
 * 		TestBean testBean = new TestBean();
 * 		testBean.setPassword(password);
 * 		return testBean;
 * 	}
 * }
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConjurValue {
	/**
	 * Indicates the complete path of the variable.
	 * @return key.
	 */
	String key();

	String kind() default "variable";
}
