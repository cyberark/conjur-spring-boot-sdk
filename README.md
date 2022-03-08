
# Conjur Spring Boot Plugin

Conjur Spring Boot Plugin provides client-side support for externalized configuration in a distributed system. With [Conjur’s Vault](https://www.conjur.org/) you have a central place to manage external secret properties for applications across all environments. Vault can manage static and dynamic secrets such as username/password for remote applications/resources and provide credentials for external services such as MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS and more.


## Features
* JavaConfig for Vault Client.
* Retrieve the single secret for the given vault's path.
* Retrieve secrets from Vault and initialize Spring Environment with remote property sources.
* Retrieves the multi secrets for the given vault's paths.


## Quick Start

Maven configuration

Add the Maven dependency



---
   
         <dependency>
         <groupId>com.cyberark.conjur.springboot</groupId>
         <artifactId>Spring-boot-conjur</artifactId>
         <version>0.0.1-SNAPSHOT</version>
        </dependency>

 ---   

## Environment Setup

* At least Java 11 and a properly configured JAVA_HOME environment variable.
* [At least Conjur OSS version 1.9+ shall be installed.](https://www.conjur.org/get-started/quick-start/oss-environment/)
* Spring boot conjur library utilizes conjur sdk java client to connect and retrieve secrets from conjur vault, which require the following connection properties to set at environment variables-

|            Name   | Environment ID           |   Field Type    | Description                     | 
| ------------------ | ------------------       |   ------------- | -----------------------     |
| Conjur Account     | CONJUR_ACCOUNT           |   STRING        | Account to connect          |
| API key            | CONJUR_AUTHN_API_KEY     |   PASSWORD      | User/host API Key/password  |
| Connection url     | CONJUR_APPLIANCE_URL     |   STRING        | Conjur instance to connect  |
| User/host identity | CONJUR_AUTHN_LOGIN       |   STRING        | User /host identity         |
| ca.cert            | CONJUR_CERT_FILE         |   STRING        |   ca.cert file              |              
| SSL Certificate    | CONJUR_SSL_CERTIFICATE   |   INPUT STREAM  | Certificate Text            |
| Token File         | CONJUR_AUTHN_TOKEN_FILE  |   STRING        | Directoty path of token file      |
## Using Plugin


1. `@ConjurPropertySource` provides a convenient and declarative mechanism for adding a `PropertySource` to Spring’s `Environment`.

To be used in conjunction with @Configuration classes.
Example usage

Given a Vault path `secret/my-application` containing the configuration data pair `database.password=mysecretpassword`, the following `@Configuration`
class uses `@VaultPropertySource` to contribute `secret/my-application` to the `Environment`'s set of `PropertySources`


----
    @Configuration
    @ConjurPropertySource("secret/my-application")
    public class AppConfig {

    @Autowired 
    Environment env;

    @Value("${database.password:notFound}")
	private String password;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(env.getProperty("database.password"));
        return testBean;
          }
     }
----

2. `@ConjurValue` and `@ConjurValues` provides another way to fetching secret

----
    @Configuration
    public class AppConfig {

    @ConjurValue("secret/my-application/database.password")
	private String password;

    @ConjurValues({"secret/my-application","my-secret/my-application1","my-secret/my-application"2})
    private Object secrets;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----




