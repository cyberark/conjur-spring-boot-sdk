

# Conjur Spring Boot Plugin

The Conjur Spring Boot Plugin provides client-side support for externalized configuration of secrets in a distributed system. The plugin can be integrated with exisiting and new Spring Boot applications to retrieve the secrets from Conjur. Application credentials/secrets stored in Conjur can be retrieved with minimal code changes to the existing Spring Boot application code using Conjur Spring Boot Plugin.

## Benefits of storing application’s secrets in [Conjur’s Vault](https://www.conjur.org/):

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

## Technical Requirements

|  Technology    |  Version |
|----------------|----------|
| Java           |  11      |
| Conjur OSS     |  1.9+    |
|ConjurSDK(Java) |  4.0.0   |
|Conjur API      |  5.1     |


# Prerequisites

## Conjur OSS setup

It is assumed that Conjur (OSS or Enterprise) and the Conjur CLI have already been installed in the environment and running in the background. 
If you haven't done so,follow these instructions doucmented for .If you haven't done so,follow the instructions for installation of the 
[OSS](https://www.conjur.org/get-started/quick-start/oss-environment/) and for installation of [Enterprise](https://www.conjur.org/get-started/quick-start/oss-environment/).

Once Conjur and the Conjur CLI are running in the background, you are ready to start setting up your Spring Boot application to work with our Conjur Spring Boot Plugin.

### Setup

The Conjur Spring Boot Plugin can be imported manually through building the source code locally, 
or by using a dependency configuration to import from Maven Central. Please refer to
the following instructions for your specific use case.

#### Using the Source Code

You can grab the library's dependencies from the source by using Maven

To do so from the source using Maven, follow the setup steps below:

1. Create new Maven project using an IDE of your choice
2. If you are using Maven to manage your project's dependencies, include the following
   Conjur Spring Boot Plugin dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
       <dependency>
         <groupId>com.cyberark.conjur.springboot</groupId>
         <artifactId>Spring-boot-conjur</artifactId>
         <version>4.5.7</version>
      </dependency>
```

_NOTE:_ Depending on what version of the Java compiler you have, you may need to update
the version. At this time, the `{version}` that we are targeting compatibility with is
Java 11:

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

3. Run `mvn install -DskipTests` in this repo's directory to install Conjur Spring Boot Plugin into your
   local maven repository.

#### Using the Jarfile

If generating a JAR is preferred, you can build the library locally and add the dependency
to the project manually by following the setup steps below:

1. Clone the Conjur Spring Boot Plugin repo locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-spring-boot-sdk`
3. Run `mvn package -DskipTests` to generate a JAR file. The output `.jar` files will be located
   in the `target` directory of the repo


4a. For Intellij, Follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html)
    to add the SDK JAR files into the new app's project.

4b. For Eclipse you `Right click project > Build Path > Configure Build Path > Library > Add External JARs`.

#### Set Up Trust Between App and Conjur

By default, the Conjur  generates and uses self-signed SSL certificates. Without trusting them, Java app will not be able to connect to the Conjur server using the Conjur APIs , so you will need to configure your app to trust them . This is accomplished by  following steps:
* Copy the .pem certificate created while setting up the Conjur
* Select the Client Class in Eclipse then do RightClick->Properties-> Run&Debug Setting-> Click New
* In the Select Configuration popup click the Java App
* In the Edit Launch Configuration properties window -> select Environment Tab -> click Add
* In the New Environment Variable window , enter 'CONJUR_SSL_CERTIFICATE' in the name field and the copied certificate in the value field

## Environment Setup

Once the setup steps have been successfully run, we will now define the variables needed
to make the connection between the plugin and Conjur. You can do this by setting
[environment variables](#environment-variables) 


#### Environment Variables

In Conjur (both Open Source and Enterprise), environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. For example,
`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT` etc.

The following environment variables need to be included in the app's runtime environment in
order use the Conjur Spring Boot Plugin if no other configuration is done (e.g. over system properties or
CLI parameters):

| Field Name         | Field ID                 |   Field Type    | Description                 | 
| ------------------ | ------------------       |   ------------- | -----------------------     |
| Conjur Account     | CONJUR_ACCOUNT           |   STRING        | Account to connect          |
| API key            | CONJUR_AUTHN_API_KEY     |   PASSWORD      | User/host API Key/password  |
| Connection url     | CONJUR_APPLIANCE_URL     |   STRING        | Conjur instance to connect  |
| User/host identity | CONJUR_AUTHN_LOGIN       |   STRING        | User /host identity         |
| ca.cert            | CONJUR_CERT_FILE         |   STRING        |   ca.cert file              |              
| SSL Certificate    | CONJUR_SSL_CERTIFICATE   |   INPUT STREAM  | Certificate Text            |

##### Steps to set the environment variables in the Eclipse IDE

Select the Client Class in Eclipse then do RightClick->Properties-> Run&Debug Setting-> Click New
* In the Select Configuration popup click the Java App
* In the Edit Launch Configuration properties window -> select Environment Tab -> click Add
* In the New Environment Variable window , enter the properties with the corresponding name and vale one at a time by clciking the 
  Add button->Click Apply &Close

###### Environment variables to add:

* Enter CONJUR_ACCOUNT in the name field and the Account Id (created during the Conjur OSS setup. Ex: myConjurAccount) as value 
* CONJUR_APPLIANCE_URL in the name field and the https://localhost:8443 as value
* CONJUR_AUTHN_LOGIN in the name field and the host/fileName1(created during the Conjur OSS setup Ex:host/<file name where grant permission is defined for   the user)/userName( for whom the access is granted in fileName1)
* CONJUR_AUTHN_TOKEN_FILE in the name field and the <path/fileName> as value, where the Token is saved
* CONJUR_CERT_FILE in the name field and the <path /.der> (.der file created during the Conjur OSS setup)
* CONJUR_SSL_CERTIFICATE in the name filed and the details of the certificate in the value field

## Using the Conjur Spring Boot Plugin

There are two ways to use the plugin. 
* @Value annotation and an optional conjur.properties file that enables the mapping of secret names. 
* @ConjurValue and @ConjurValues, which are Conjur native annotations(Custom Annotation) that enable individual and bulk secret retrieval.

Option 1. 
The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in the Conjur Vault. 

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


Option 2. 
The `@ConjurValue` and `@ConjurValues` annotations are intended for new Spring Boot applications. Injecting `@ConjurValue` 
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








