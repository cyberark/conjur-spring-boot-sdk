

# Conjur Spring Boot Plugin

The Conjur Spring Boot Plugin provides client-side support for externalized configuration of secrets in a distributed system. You can integrate the plugin with exisiting and new Spring Boot applications. With the Spring Boot Plugin, your application's credentials and secrets are stored in Conjur, allowing you to retrieve them with minimal code changes to your Spring Boot application code.

## Benefits of storing application secrets in [Conjur](https://www.conjur.org/):

* Provides one central location to store and retrieve secrets for applications across all environments.
* Supports the management of static and dynamic secrets such as username and password for remote applications and resources.
* Provides credentials for external services like MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS, and more.

## Certification level

![](https://img.shields.io/badge/Certification%20Level-Certified-28A745?link=https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md)

This repo is a **Certified** level project. It's a community contributed project **reviewed and tested by CyberArk
and trusted to use with Conjur Open Source**. For more detailed information on our certification levels, see [our community guidelines](https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md#certified).


## Features

The following features are available with the Conjur Spring Boot Plugin:

* Retrieve a single secret by specifying the path to the secret in Conjur.
* Retrieve multiple secrets by specifying the paths to the secrets in Conjur.
* Retrieve secrets from Conjur and initialize the Spring environment with remote property sources.


## Limitations

The Spring Boot Plugin does not support creating, deleting, or updating secrets.

## Technical requirements

|  Technology    |  Version |
|----------------|----------|
| Java           |  11      |
| Conjur OSS     |  1.9+    |
|ConjurSDK(Java) |  4.0.0   |
|Conjur API      |  5.1     |


# Prerequisites
The following are prerequisites for using the Spring Boot Plugin.

## Conjur OSS setup

Conjur (OSS or Enterprise) and the Conjur CLI are installed in the environment and are running in the background.
If you haven't done so,follow the installation instructions for [OSS](https://www.conjur.org/get-started/quick-start/oss-environment/) or [Enterprise](https://www.conjur.org/get-started/quick-start/oss-environment/).

Once Conjur and the Conjur CLI are running in the background, setup your Spring Boot application to work with the Spring Boot Plugin.

### Setup

You can import the Spring Boot Plugin manually by building the source code locally or using a dependency configuration to import from Maven Central. For more information, see the following instructions for your specific use case.

#### Using the source code

You can grab the library's dependencies from the source by using Maven. 

1. Create a new Maven project using an IDE of your choice.
2. If using Maven to manage your project's dependencies, include the following
   Conjur Spring Boot Plugin dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
       <dependency>
         <groupId>com.cyberark.conjur.springboot</groupId>
         <artifactId>Spring-boot-conjur</artifactId>
         <version>1.0.0</version>
      </dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update
the version. At this time, we are compatibile with Java 11.

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

Run `mvn install -DskipTests` in this repo's directory to install the Spring Boot Plugin into your
local Maven repository.

#### Using the Jar file

If you prefer to generate a JAR, you can build the library locally and add the dependency
to the project manually.

1. Clone the Spring Boot Plugin repository locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-spring-boot-sdk`
3. Run `mvn package -DskipTests` to generate a JAR file. The `.jar` files that are output are located
   in the repository's `target` directory.


4a. For Intellij, see the steps outlined [here](https://www.jetbrains.com/help/idea/library.html)
    to add the SDK JAR files into the new application's project.

4b. For Eclipse, `right click project > Build Path > Configure Build Path > Library > Add External JARs`.

#### Setup trust between application and Conjur

By default, Conjur  generates and uses self-signed SSL certificates. Without trusting them, the Java app cannot connect to the Conjur server using the Conjur APIs. You must configure your app to trust the self-signed SSL certificates. 
* Copy the .pem certificate created while setting up Conjur.
* Select the Client Class in Eclipse by right clicking Properties-> Run&Debug Setting-> New
* In the Select Configuration dialog, click the Java app.
* In the Edit Launch Configuration properties window, select the Environment tab and click Add.
* In the New Environment Variable window, enter 'CONJUR_SSL_CERTIFICATE' in the Name field and the copied certificate in the Value field.

## Environment Setup

Once the setup steps are successfully run, define the needed variables to make the connection between the Spring Boot Plugin and Conjur by setting the
[environment variables](#environment-variables)


#### Environment variables

In Conjur (both Open Source and Enterprise), environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. For example,
`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.

If no other configuration is done (e.g. over system properties or CLI parameters),include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

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

##### Set the environment variables in the Eclipse IDE

* Select the Client Class in Eclipse then right click Properties-> Run&Debug Setting-> New.
* In the Select Configuration dialog, click the Java app.
* In the Edit Launch Configuration properties window, select the Environment Tab and click Add.
* In the New Environment Variable window, enter the properties with the corresponding name one at a time by clicking
  Add and then Apply & Close.

###### Add environment variables

* Enter CONJUR_ACCOUNT in the Name field and the Account Id (created during the Conjur OSS setup. Example: myConjurAccount as value
* CONJUR_APPLIANCE_URL in the Name field and the https://localhost:8443 as value
* CONJUR_AUTHN_LOGIN in the Name field and the host/fileName1 (created during the Conjur OSS setup). Example: host/<file name where grant permission is defined for the user and /userName for whom the access is granted in fileName1.
* CONJUR_AUTHN_TOKEN_FILE in the Name field and the <path/fileName> as value, where the token is saved.
* CONJUR_CERT_FILE in the Name field and the <path /.der> (.der file created during the Conjur OSS setup).
* CONJUR_SSL_CERTIFICATE in the Name field and the details of the certificate in the Value field.

## Using the Conjur Spring Boot Plugin

There are two ways to use the plugin.
* @Value annotation and an optional conjur.properties file that enables the mapping of secret names.
* @ConjurValue and @ConjurValues, which are Conjur native annotations (Custom annotations) that enable individual and bulk secret retrieval.

#### Option 1 : Using Spring Standard @Value annotation.
The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in the Conjur Vault.

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


#### Option 2 : Using Conjur native annotations(Custom Annotation).
The `@ConjurValue` and `@ConjurValues` annotations are intended for new Spring Boot applications. Injecting `@ConjurValue`
into your Spring Boot code allows you to retrieve a single secret from Conjur. `@ConjurValues` allows you to retrieve multiple secrets from the Conjur.

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
