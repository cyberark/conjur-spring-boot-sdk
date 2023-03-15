/*
 * package com.cyberark.conjur.springboot;
 * 
 * import static org.junit.Assert.assertNotNull; import static
 * org.junit.Assert.assertNull; import static
 * org.junit.jupiter.api.Assertions.assertEquals;
 * 
 * import java.io.IOException; import java.io.InputStream; import
 * java.util.Properties;
 * 
 * import org.junit.jupiter.api.Test; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.boot.test.context.SpringBootTest;
 * 
 * import com.cyberark.conjur.sdk.ApiException; import
 * com.cyberark.conjur.springboot.annotations.ConjurPropertySource; import
 * com.cyberark.conjur.springboot.annotations.ConjurValue; import
 * com.cyberark.conjur.springboot.annotations.ConjurValues; import
 * com.cyberark.conjur.springboot.constant.ConjurConstant; import
 * com.cyberark.conjur.springboot.core.env.ConjurConnectionManager;
 * 
 * @SpringBootTest(classes = ConjurPluginTests.class)
 * 
 * @ConjurPropertySource("db/") public class ConjurPluginTests {
 * 
 * 
 * 
 * @Value("${dbpassWord}") private byte[] dbpassWord;
 * 
 * @Value("${dbuserName}") private byte[] dbuserName;
 * 
 * @Value("${dbPort:notFound}") private byte[] dbPort;
 * 
 * @ConjurValue(key = "db/dbPort") private byte[] ddbPortFromCustomAnnotation;
 * 
 * 
 * @Value("${key}") private byte[] key;
 * 
 * @ConjurValue(key = "db/dbuserName") private byte[]
 * dbuserNameFromCustomAnnotation;
 * 
 * // private static Properties properties; private static Properties props =
 * new Properties();
 * 
 * static { // move to Conjur Utill Class InputStream propsFile =
 * ConjurPluginTests.class.getClassLoader()
 * .getResourceAsStream("testParameters.properties");
 * 
 * if (propsFile != null) {
 * 
 * try { props.load(propsFile); } catch (IOException e) { e.printStackTrace(); }
 * finally { try { propsFile.close(); } catch (IOException e) {
 * e.printStackTrace(); } }
 * 
 * }
 * 
 * }
 * 
 * @Test void testForAllEnvVariables() {
 * 
 * assertNotNull(System.getenv().getOrDefault("CONJUR_AUTHN_LOGIN", null));
 * assertNotNull(System.getenv().getOrDefault("CONJUR_AUTHN_API_KEY", null));
 * assertNotNull(System.getenv().getOrDefault("CONJUR_ACCOUNT", null));
 * assertNotNull(System.getenv().getOrDefault("CONJUR_CERT_FILE", null));
 * //assertNotNull(System.getenv().getOrDefault("CONJUR_SSL_CERTIFICATE",
 * null)); assertNotNull(System.getenv().getOrDefault("CONJUR_AUTHN_API_KEY",
 * null)); assertNotNull(System.getenv().getOrDefault("CONJUR_AUTHN_TOKEN_FILE",
 * null));
 * 
 * }
 * 
 * @Test void testForConnection() {
 * assertNotNull(ConjurConnectionManager.getInstance());
 * 
 * }
 * 
 * @Test void testForNotExistingSecretNames() {
 * assertNull(getDdbPortFromCustomAnnotation()); assertNotNull(getDbPort());
 * assertEquals(ConjurConstant.NOT_FOUND, getDbPort());
 * 
 * }
 * 
 * 
 * @Test void testForSinglePath() throws ApiException {
 * assertNotNull(dbuserName); assertEquals(props.getProperty("dbuserName"),
 * getDbuserName()); }
 * 
 * @Test void testForSecondPath() throws ApiException {
 * assertNotNull(getDbpassWord()); assertEquals(props.getProperty("dbpassWord"),
 * getDbpassWord());
 * 
 * assertNotNull(getDbpassWord()); assertEquals(props.getProperty("dbpassWord"),
 * getDbpassWord());
 * 
 * }
 * 
 * @Test void testForThirdPath() throws ApiException { assertNotNull(getKey());
 * assertEquals(props.getProperty("key"), getKey());
 * 
 * }
 * 
 *//**
	 * @return the props
	 */
/*
 * public static Properties getProps() { return props; }
 * 
 *//**
	 * @param props the props to set
	 */
/*
 * public static void setProps(Properties props) { ConjurPluginTests.props =
 * props; }
 * 
 *//**
	 * @return the dbuserName
	 */
/*


*//**
	 * @return the dbpassWord
	 */
/*
 * public String getDbpassWord() { return new String(dbpassWord); }
 * 
 *//**
	 * @param dbpassWord the dbpassWord to set
	 */
/*
 * public void setDbpassWord(byte[] dbpassWord) { this.dbpassWord = dbpassWord;
 * }
 * 
 *//**
	 * @return the key
	 */
/*
 * public String getKey() { return new String(key); }
 * 
 *//**
	 * @param key the key to set
	 */
/*
 * public void setKey(byte[] key) { this.key = key; }
 * 
 * public String getDbuserName() { return new String(dbuserName); }
 *//**
	 * @param dbuserName the dbuserName to set
	 */
/*
 * 
 * public void setDbuserName(byte[] dbuserName) { this.dbuserName = dbuserName;
 * }
 * 
 *//**
	 * @return the dbPort
	 */
/*
 * public String getDbPort() { return new String(dbPort); }
 * 
 *//**
	 * @param dbPort the dbPort to set
	 */
/*
 * public void setDbPort(byte[] dbPort) { this.dbPort = dbPort; }
 * 
 *//**
	 * @return the ddbPortFromCustomAnnotation
	 */
/*
 * public byte[] getDdbPortFromCustomAnnotation() { return
 * ddbPortFromCustomAnnotation; }
 * 
 *//**
	 * @param ddbPortFromCustomAnnotation the ddbPortFromCustomAnnotation to set
	 *//*
		 * public void setDdbPortFromCustomAnnotation(byte[]
		 * ddbPortFromCustomAnnotation) { this.ddbPortFromCustomAnnotation =
		 * ddbPortFromCustomAnnotation; }
		 * 
		 * 
		 * 
		 * }
		 */