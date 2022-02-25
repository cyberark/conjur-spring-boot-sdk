package com.cyberark.conjur.springboot;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.cyberark.conjur.sdk.ApiException;
import com.cyberark.conjur.springboot.annotations.ConjurPropertySource;

@SpringBootTest(classes = ConjurPluginTests.class)
//@ConjurPropertySource("jenkinsapp/")
//@ConjurPropertySource("jenkinsapp1/")
@ConjurPropertySource(value = { "jenkinsapp/", "jenkinsapp1/", "jenkinsapp3/", "jenkinsapp4/" }, name = "vault2")
public class ConjurPluginTests {

	@Value("${dbuserName}")
	private String dbuserName;

	@Value("${dbpassWord}")
	private String dbpassWord;


	@Value("${key}")
	private String key;

	// private static Properties properties;
	private static Properties props = new Properties();

	static {
		// move to Conjur Utill Class
		InputStream propsFile = ConjurPluginTests.class.getClassLoader()
				.getResourceAsStream("testParameters.properties");

		if (propsFile != null) {

			try {
				props.load(propsFile);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					propsFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	@Test
	void testForSinglePath() throws ApiException {
		assertNotNull(dbuserName);
		assertEquals(props.getProperty("dbuserName"), getDbuserName());
	}

	@Test
	void testForSecondPath() throws ApiException {
		assertNotNull(getDbpassWord());
		assertEquals(props.getProperty("dbpassWord"), getDbpassWord());

		assertNotNull(getDbpassWord());
		assertEquals(props.getProperty("dbpassWord"), getDbpassWord());

	}

	@Test
	void testForThirdPath() throws ApiException {
		assertNotNull(getKey());
		assertEquals(props.getProperty("key"), getKey());

	}

	/**
	 * @return the props
	 */
	public static Properties getProps() {
		return props;
	}

	/**
	 * @param props the props to set
	 */
	public static void setProps(Properties props) {
		ConjurPluginTests.props = props;
	}

	/**
	 * @return the dbuserName
	 */
	public String getDbuserName() {
		return dbuserName;
	}

	/**
	 * @param dbuserName the dbuserName to set
	 */
	public void setDbuserName(String dbuserName) {
		this.dbuserName = dbuserName;
	}

	/**
	 * @return the dbpassWord
	 */
	public String getDbpassWord() {
		return dbpassWord;
	}

	/**
	 * @param dbpassWord the dbpassWord to set
	 */
	public void setDbpassWord(String dbpassWord) {
		this.dbpassWord = dbpassWord;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}


	
}
