

# Conjur Spring Boot Plugin

The Conjur Spring Boot Plugin provides client-side support for externalized configuration of secrets in a distributed system. You can integrate the plugin with exisiting and new Spring Boot applications to retrieve secrets from Conjur. Using the Spring Boot Plugin, you can retrieve application credentials and secrets stored in Conjur with minimal code changes to the existing Spring Boot application code.

## Benefits of storing application secrets in [CyberArk's Vault](https://www.conjur.org/)

* Provides one central location to store and retrieve secrets for applications across all environments.
* Supports the management of static and dynamic secrets such as username and password for remote applications and resources.
* Provides credentials for external services like MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS, and more.

**Note for Kubernetes users**: Customers and users intending to run their Spring Boot based application in Kubernetes are encouraged to follow an alternative to the plugin solution described in this readme. Cyberark offers a Kubernetes native feature 'Push To File' described [here]( https://github.com/cyberark/secrets-provider-for-k8s/blob/main/PUSH_TO_FILE.md#example-custom-templates-spring-boot-configuration/). The documentation illustrates a process to assemble spring-boot application.properties files dynamically and avoids the need for any Java code changes in order to draw secrets directly from Conjur.

## Certification level
[![](https://img.shields.io/badge/Certification%20Level-Certified-28A745?)](https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md)

This repository is a **Certified** level project. It's a community contributed project **reviewed and tested by CyberArk
and trusted to use with Conjur Open Source**. For more detailed information on our certification levels, see [our community guidelines](https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md#certified).


## Features

The following features are available with the Spring Boot Plugin:

* Retrieve a single secret from the CyberArk Vault by specifying the path to the secret in the Vault.
* Retrieve multiple secrets from the CyberArk Vault by specifying the paths to the secrets in the Vault.
* Retrieve secrets from the CyberArk Vault and initialize the Spring environment with remote property sources.


## Limitations

The Spring Boot Plugin does not support creating, deleting, or updating secrets.

## Technical Requirements

|  Technology    |  Version |
|----------------|----------|
| Java           |  11      |
| Conjur OSS     |  1.9+    |
| Conjur Enterprise | 12.5  |
|ConjurSDK(Java) |  4.0.0   |
|Conjur API      |  5.1     |


# Prerequisites

The following are prerequisites to using the Spring Boot Plugin.

## Conjur setup

Conjur (OSS or Enterprise) and the Conjur CLI are installed in the environment and running in the background.
If you haven't yet done so, follow the instructions for installing [OSS](https://www.conjur.org/get-started/quick-start/oss-environment/) or [Enterprise](https://www.conjur.org/get-started/quick-start/oss-environment/).

Once Conjur and the Conjur CLI are running in the background, you can start setting up your Spring Boot application to work with our Conjur Spring Boot Plugin.

### Setup

You can import the Conjur Spring Boot Plugin manually by building the source code locally or using a dependency configuration to import from Maven Central. For information about your specific use case, see the following instructions.

#### Using the source code

You can grab the library's dependencies using Maven:

1. Create a new Maven project using an IDE of your choice.
2. If you are using Maven to manage your project's dependencies, include the following
   Spring Boot Plugin dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
       <dependency>
         <groupId>com.cyberark</groupId>
         <artifactId>conjur-sdk-springboot</artifactId>
         <version>LATEST</version>
      </dependency>
```
![Version Badge](https://img.shields.io/github/v/release/cyberark/conjur-spring-boot-sdk?label=Latest%20Release)

View available versions at [Github Releases](https://github.com/cyberark/conjur-spring-boot-sdk/releases/latest) or [Maven Central](https://search.maven.org/artifact/com.cyberark/conjur-sdk-springboot).

NOTE: Depending on the Java compiler version you have, you may need to update
the version. At this time, we are targeting compatibility with Java 11:

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

Run `mvn install -DskipTests` in this repository's directory to install the Spring Boot Plugin into your local Maven repository.

#### Using the Jarfile

If generating a JAR is preferred, build the library locally and add the dependency to the project manually:

1. Clone the Spring Boot Plugin repository locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-spring-boot-sdk`
3. Run `mvn package -DskipTests` to generate a JAR file. The output `.jar` files are located
   in the `target` directory of the repository.
   
4a. For Intellij, follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html)
    to add the SDK JAR files to the new app's project.

4b. For Eclipse, `right click project > Build Path > Configure Build Path > Library > Add External JARs`.

#### Setup trust between app and Conjur

By default, Conjur  generates and uses self-signed SSL certificates. Without trusting them, the Java app cannot connect to the Conjur server using the Conjur APIs. You need to configure your app to trust them. 
1. Copy the .pem certificate created while setting up the Conjur.
2. Select the Client Class in Eclipse then right-click and select Properties-> Run&Debug Setting-> New.
3. Select Configuration popup and click the Java app.
4. In the Edit Launch Configuration properties window, select the Environment Tab and click Add.
5. In the New Environment Variable window, enter 'CONJUR_SSL_CERTIFICATE' in the Name field and the copied certificate in the Value field.

## Environment setup

Once the setup steps are successfully run, define the variables needed to make the connection between the plugin and Conjur. You can do this by setting
[environment variables](#environment-variables).


#### Environment variables

In Conjur (both Open Source and Enterprise), environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. 
For example:`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT`.

If no other configuration is done (e.g. over system properties or CLI parameters), include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

| Name                     | Environment ID           |   Description                 |
| ------------------------ | ------------------       |   -----------------------     |
| Conjur Account           | CONJUR_ACCOUNT           |   Account to connect          |
| API key                  | CONJUR_AUTHN_API_KEY     |   User/host API Key/password  |
| Connection url           | CONJUR_APPLIANCE_URL     |   Conjur instance to connect  |
| User/host identity       | CONJUR_AUTHN_LOGIN       |   User /host identity         |
| SSL Certificate Path     | CONJUR_CERT_FILE         |   Path to certificate file    |
| SSL Certificate Content  | CONJUR_SSL_CERTIFICATE   |   Certificate content         |

Only one CONJUR_CERT_FILE and CONJUR_SSL_CERTIFICATE is required. There are two variables
to allow the user to specify the path to a certificate file or provide the certificate
data directly in an environment variable.

##### Set environment variables in the Eclipse IDE

1. Select the Client Class in Eclipse, then right click Properties -> Run&Debug Setting-> New.
2. In the Select Configuration popup, click the Java App.
3. In the Edit Launch Configuration properties window, select the Environment Tab and click Add.
4. In the New Environment Variable window, enter the properties with the corresponding name and vale one at a time by clciking the
  Add button followed by Apply & Close.
  

###### Add environment variables

* Enter CONJUR_ACCOUNT in the Name field and the Account Id created during the Conjur OSS setup. For example: myConjurAccount) as value.
* CONJUR_APPLIANCE_URL is the URL of the Conjur instance to which you are connecting. When connecting to Conjur Enterprise, configure for high availability by including the URL of the master load balancer (if performing read and write operations) or the URL of a follower load balancer (if performing read-only operations).
* CONJUR_AUTHN_LOGIN in the Name field and the host/fileName1 created during the Conjur OSS setup. For example: host/<file name where grant permission is defined for the user/userName (for whom the access is granted in fileName1).
* CONJUR_CERT_FILE in the Name field and the <path /.der> (.der file created during the Conjur OSS setup.
* CONJUR_SSL_CERTIFICATE in the Name field and the details of the certificate in the Value field.
* For IntelliJ, set up trusted Conjur self-signed certs by following the steps outlined [here](https://www.jetbrains.com/help/idea/settings-tools-server-certificates.html).

## Using the Conjur Spring Boot Plugin

There are two ways to use the plugin.
* @Value annotation and an optional conjur.properties file that enables the mapping of secret names.
* @ConjurValue and @ConjurValues, which are Conjur native annotations (custom annotations) that enable individual and bulk secret retrieval.

#### Option 1: Spring Standard @Value annotation
The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in the CyberArk Vault.

 Example use case: Given the following Vault path `policy/my-application` containing this configuration data pair `database.password=mysecretpassword`, the following `@Configuration` class uses `@ConjurPropertySource` to contribute `policy/my-application` to the environment's set of `PropertySources.`


----
    @Configuration
    @ConjurPropertySource("policy/my-application/")
    @ConjurPropertySource("policy/my-other-application/")
    @ConjurPropertySource(value={"policy/my-application/", "policy/my-other-application/"}, name="")
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


#### Option 2: Conjur native annotations (custom annotation)
The `@ConjurValue` and `@ConjurValues` annotations are intended for new Spring Boot applications. Injecting `@ConjurValue`
into your Spring Boot code allows you to retrieve a single secret from the CyberArk Vault. `@ConjurValues` allows you to retrieve multiple secrets from the CyberArk Vault.

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
of our development workflows, see our [contributing guide](CONTRIBUTING.md).

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
