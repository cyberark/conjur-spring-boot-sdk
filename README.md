
# Conjur Spring Boot Plugin

Conjur Spring Boot Plugin provides client-side support for externalized configuration in a distributed system. With [Conjur’s Vault](https://www.conjur.org/) you have a central place to manage external secret properties for applications across all environments. Vault can manage static and dynamic secrets such as username/password for remote applications/resources and provide credentials for external services such as MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS and more.


## Features
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

## Technical Stack

Following is the technology stack that is used for the development of the library.

*	Java 11(JDK 11 and JRE 11)  (For more info of java version Please refer given link-https://www.oracle.com/java/technologies/java-se-support-roadmap.html )

*	Conjur OSS version 1.9+

*	Conjur sdk  java version 4.0.0
*	Conjur api version 5.1.

## Environment Setup

* At least Java 11 and a properly configured JAVA_HOME environment variable.
* [At least Conjur OSS version 1.9+ shall be installed.](https://www.conjur.org/get-started/quick-start/oss-environment/)
* Spring boot conjur library utilizes conjur sdk java client to connect and retrieve secrets from conjur vault, which require the following connection properties to set at environment variables-

|            Name   | Environment ID            |   Description                 | 
| ------------------ | ------------------       |   -----------------------     |
| Conjur Account     | CONJUR_ACCOUNT           |   Account to connect          |
| API key            | CONJUR_AUTHN_API_KEY     |   User/host API Key/password  |
| Connection url     | CONJUR_APPLIANCE_URL     |   Conjur instance to connect  |
| User/host identity | CONJUR_AUTHN_LOGIN       |   User /host identity         |
| ca.cert            | CONJUR_CERT_FILE         |   ca.cert file                |              
| SSL Certificate    | CONJUR_SSL_CERTIFICATE   |   Certificate Text            |
| Token File         | CONJUR_AUTHN_TOKEN_FILE  |   Directoty path of token file|

## Using Plugin


1. `@ConjurPropertySource` provides a convenient and declarative mechanism for adding a `PropertySource` to Spring’s `Environment`.

To be used in conjunction with @Configuration classes.
Example usage

Given a Vault path `policy/my-application` containing the configuration data pair `database.password=mysecretpassword`, the following `@Configuration`
class uses `@ConjurPropertySource` to contribute `policy/my-application` to the `Environment`'s set of `PropertySources`


----
    @Configuration
    @ConjurPropertySource("policy/my-application")
    public class AppConfig {

    @Autowired 
    Environment env;

    @Value("${database.password}")
	private String password;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----

2. `@ConjurValue` and `@ConjurValues` provides another way to fetching secret

----
    @Configuration
    public class AppConfig {

    @ConjurValue("policy/my-application/database.password")
	private String password;

    @ConjurValues({"policy/my-application/db.userName","policy/my-application/db.password","policy/my-application/db.name"})
    private String [] secrets;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----




