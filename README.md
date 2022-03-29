
# Conjur Spring Boot Plugin

The Conjur Spring Boot Plugin provides client-side support for externalized configuration of secrets in a distributed system. The plugin is intended for both newly built Spring Boot applications and those built prior to the availability of CyberArk Conjur. Using the Conjur Spring Boot Plugin requires minimal changes to your Spring Boot application code, supports CyberArk secrets in the code, and maintains the names of your application's secrets and passwords. Your application’s secrets are stored in [Conjur’s Vault](https://www.conjur.org/), which offers the following benefits:

* Provides one central location to store and retrieve secrets for applications across all environments. 
* Supports the management of static and dynamic secrets such as username and password for remote applications and resources.  
* Provides credentials for external services like MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS, and more. 


## Features

The following features are available with the Conjur Spring Boot Plugin: 

* Retrieve a single secret from the Conjur Vault by specifying the path to the secret in the vault. 
* Retrieve multiple secrets from the Conjur Vault by specifying the paths to the secrets in the vault. 
* Retrieve secrets from the Conjur Vault and initialize the Spring environment with remote property sources.


## Limitations

The Conjur Spring Boot Plugin does not support creating, deleting, or updating secrets.


## Maven Configuration

Maven version 3.0 and above is a requirement for using the Conjur Spring Boot Plugin. You must have the Maven tool and add the following Maven dependency to your pom.xml file.   

---
   
         <dependency>
            <groupId>com.cyberark.conjur.springboot</groupId>
            <artifactId>Spring-boot-conjur</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

 ---   

## Technical Stack

The following is the technology stack used for the development of the Conjur Spring Boot SDK, which provides APIs to access the Conjur Vault. 

*	Java 11 (JDK 11 and JRE 11)   
	For more information, refer to the [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html).
*	Conjur Open Source Suite (OSS) version 1.9+
*	Conjur SDK Java version 4.0.0
*	Conjur API version 5.1

## Environment Setup

The following is the list of environment requirements.

* Java 11 or higher and a properly configured [JAVA_HOME environment variable](https://docs.oracle.com/en/cloud/saas/enterprise-performance-management-common/diepm/epm_set_java_home_104x6dd63633_106x6dd6441c.html)      
* [Conjur OSS version 1.9 or higher](https://www.conjur.org/get-started/quick-start/oss-environment/)
* The Conjur Spring Boot SDK uses the Conjur SDK Java client to connect and retrieve secrets from the Conjur Vault. You must set the following connection properties as environment variables.

| Name               | Environment ID           |   Description                 | 
| ------------------ | ------------------       |   -----------------------     |
| Conjur Account     | CONJUR_ACCOUNT           |   Account to connect          |
| API key            | CONJUR_AUTHN_API_KEY     |   User/host API Key/password  |
| Connection url     | CONJUR_APPLIANCE_URL     |   Conjur instance to connect  |
| User/host identity | CONJUR_AUTHN_LOGIN       |   User /host identity         |
| ca.cert            | CONJUR_CERT_FILE         |   ca.cert file                |              
| SSL Certificate    | CONJUR_SSL_CERTIFICATE   |   Certificate Text            |

## Using the Conjur Spring Boot Plugin

Applications have two alternative methods for using the plugin. The first method is based on a standard Spring Boot @Value annotation and an optional conjur.properties file that enables the mapping of secret names. The second method is based on @ConjurValue and @ConjurValues, which are Conjur native annotations that enable individual and bulk secret retrieval.

Option 1. The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in the Conjur Vault. 

 Example use case: Given the following vault path `policy/my-application` containing this configuration data pair `database.password=mysecretpassword`, the following `@Configuration` class uses `@ConjurPropertySource` to contribute `policy/my-application` to the environment's set of `PropertySources.`


----
    @Configuration
    @ConjurPropertySource("policy/my-application")
    @ConjurPropertySource("policy/my-other-application")
    public class AppConfig {

    @Autowired 
    Environment env;

    @Value("${database.password}")
	private byte[] password;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----
 
Conjur Properties (conjur.properties)

----
    conjur.mapping.database.password=MyConjurOraclePassword
----


Option 2. The `@ConjurValue` and `@ConjurValues` annotations are intended for new Spring Boot applications. Injecting `@ConjurValue` 
into your Spring Boot code allows you to retrieve a single secret from the Conjur Vault. `@ConjurValues` allows you to retrieve multiple secrets from the Conjur Vault.

----
    @Configuration
    public class AppConfig {

    @ConjurValue("policy/my-application/database.password")
	private byte[] password;

    @ConjurValues({"policy/my-application/db.userName","policy/my-application/db.password","policy/my-application/db.name"})
    private byte[] secrets;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----

## Contributing

We welcome contributions of all kinds to this repository. For instructions on how to get started and descriptions
of our development workflows, please see our [contributing guide](CONTRIBUTING.md).

## License

Copyright (c) 2022 CyberArk Software Ltd. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

For the full license text see [`LICENSE`](LICENSE).


