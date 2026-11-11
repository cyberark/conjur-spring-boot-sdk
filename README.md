# Conjur Spring Boot Plugin

The Spring boot conjur sdk plugin provides client-side support for externalized configuration of secrets in a distributed system. The existing and new Spring Boot Applications can retrieve secrets from Conjur by adding the plugin as dependency.There are two ways to integrate the plugin:

- With **minimal code** change by annotating existing or new POJO/Class with @ConjurPropertySource
- With **no code** change.

The Authentication parameters to connect to Conjur Server can be configured either as the System Env variables(while using @ConjurPropertySource annotation) or through external property source [Spring Cloud Config Server](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/).

## Benefits of storing application secrets in [Conjur](https://www.conjur.org/)

* Provides one central location to store and retrieve secrets for applications across all environments.
* Supports the management of static and dynamic secrets such as username and password for remote applications and resources.
* Provides credentials for external services like MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS, and more.

**Note for Kubernetes users**: Customers and users intending to run their Spring Boot based application in Kubernetes are encouraged to follow an alternative to the plugin solution described in this readme. Cyberark offers a Kubernetes native feature 'Push To File' described [here]( https://github.com/cyberark/secrets-provider-for-k8s/blob/main/PUSH_TO_FILE.md#example-custom-templates-spring-boot-configuration/). The documentation illustrates a process to assemble spring-boot application.properties files dynamically and avoids the need for any Java code changes in order to draw secrets directly from Conjur.

## Certification level
![](https://img.shields.io/badge/Certification%20Level-Certified-28A745?link=https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md)

This repo is a **Certified** level project. It's a community contributed project that **has been reviewed and tested by CyberArk and is trusted to use with Conjur Open Source, Conjur Enterprise, and Conjur Cloud**. For more detailed information on our certification levels, see [our community guidelines](https://github.com/cyberark/community/blob/master/Conjur/conventions/certification-levels.md#certified).

## Features

The following features are available with the Spring Boot Plugin:

* Seamless integration of the spring boot conjur plugin to exisitng or new Spring Boot application to retireve secrets from CyberArk Conjur with miniaml code change by annotating with @ConjurPropertySource or no code change (dynamic mapping of secrets to credential variables annotated with @Value.
* Plugin now allow the users to enable the  the Retry mechanism by configuring max-attempts & wait-duration for tranisent failures which will stop the starting the Microservices. Retry mechanism can be enabled by configuring the conjur.resilience-enabled param. By default the param value is set to false( to support backward compatibility), need to set to true to enable Retry feature.
* If conjur.resilience-enabled flag is set to true, then set the max-attempts and wait-duration parameters as defined in the configuration section.
* The secrets are exposed as environment variables, with this approach ,there will no code change required as usesrs/developers can configure properties as placeholder value in application properties and fetch using @Value (key configured in application properties).
* Support to externalize key mappings within application.properties and environment params, addressing limitations of the current conjur.properties approach. This aligns with standard deployment workflows where pipelines generate OpenShift ConfigMaps from application-specific application.properties files. The existing method requires multiple ConfigMaps and volume mounts, adding unnecessary complexity.
* Support to externalize the path specified in the @ConjurPropertySource annotation to support dynamic configuration, aligning with the current approach for externalizing key mappings.
* Externalize the properties using the Spring cloud Config Server or as System Property
* API authentication


## Limitations

The Spring Cloud Config Conjur plugin does not support creating, updating or removing secrets

## Technical Requirements

| Technology        | Version           |
|-------------------|-------------------|
| Java              | 8+                |
| Conjur OSS        | 1.9+              |
| Conjur Enterprise | 12.5+             |
| Conjur Cloud		|                   |
| ConjurSDK(Java)   | 4.1.0             |
| Conjur API        | 5.1               |
| Spring Cloud      | 2021.x and 2022.x |
| Spring Boot       | 2.x and 3.x       |
| resilience4j      | 1.7.1             |



# Prerequisites

The following are prerequisites to using the Spring Boot Plugin.

## Conjur setup

Conjur (OSS or Enterprise) and the Conjur CLI are installed in the environment and running in the background.
If you haven't yet done so, follow the instructions for installing [OSS](https://www.conjur.org/get-started/quick-start/oss-environment/) or [Enterprise](https://docs.cyberark.com/Product-Doc/OnlineHelp/AAM-DAP/Latest/en/Content/HomeTilesLPs/LP-Tile2.htm?tocpath=Setup%7C_____0).

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

Run the following in this repository's directory to install the Spring Boot Plugin into your local Maven repository:

```sh
# External contributors — fetches directly from Maven Central, no credentials needed:
mvn --settings mvn-settings.xml.dist install -DskipTests
```

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

-  Spring Cloud Config provides server-side and client-side support for externalized configuration in a distributed system.
    \* Config Server provides a central place to manage external properties for applicaiton across all environments. Best 
    \* Can be integrated with any application running any language including Spring applicaiton as Spring Environment provides mapping to the \n
     property sources.
    \* Configuration can be managed across the enviornment while migrating from dev to test and to production, to make sure that applicaitons have\n
     everything they need to run when they migrate.
    \* Default storage is git, so it easily supports labeled versions of configuration environment as well as being accessible to a wide range
     of tooling for managing the content
    \* Support for configuration file security through the encryption/decryption mechanism

  ## Spring Cloud Config Setup

   Setting up the Config Server involves two step process, installing the Server and Client

   ### Spring Cloud Config Server Setup


   #### Git Backend setup

   The Spring Cloud Config Server can be integrated with different storage mechanism like GitHub, File System Backend, Conjur Backend etc,to access Property files from.  The default implementation of the Spring Cloud Config Server uses Git.
  In this documentation Git Backend has been used as an repository to store and acces the application properties. Below are the steps to follow to integrate   Spring Cloud Config Server with Git Repository.

   #### Configure Repository

  

   Below are the steps to create a .properties file and to initialize the Git Repository.

  

  1. Check if git is installed on the sytem with the below command, if not download Git [here](https://git-scm.com/downloads)


   ```
   $ git --version
   ```
   Should return the version of the Git, if already installed.

  2. Create a folder in local environment, to store the <file-name>.properties file that will be used by the application

   Property files can created one for each environment (Dev,Test, Prod)
   
   ```
   $> mkdir example-repo
   ````
   

  3. Change to the directory and create a properties file

   
   ```
   $> cd example-repo
   $ example-repo > vi exampleService.properties
   ```
   Enter the below properties in the exampleService.properties
   
   ```
   exampleService.properties
   conjur.account = <Account to connect>
   conjur.appliance-url = <Conjur instance to connect>
   conjur.authn-login = <User /host identity>
   conjur.authn-api-key = <User/host API Key/password>
   conjur.auth-token-file = <Path to token file containing API key> -optional
   conjur.cert-file = <Path to certificate file>
   conjur.ssl-certificate = <Certificate content>
   conjur.resilience-enabled = false <Mandatory ,default false. Should be true, for Enabling retry mechanism should be true>
   conjur.resilience-max-attempts = <Optional,maximum number of retry to work in case of failure Example:3 -will be retried 2       								times +initial try>
   conjur.resilience-wait-duration= <Optional,amount of time to wait between retry attempts ,should be in milliseconds, seconds,           								 Example :400ms>
   ```
   

  4. Initializing the Git

   
   The below commands will be used to initialize Git in the configuration folder and commit the property files to the Git
   
   ```
   $> cd example-repo
   $ example-repo> git init
   $ example-repo> git add .
   $ example-repo> git commit -m "Initial commit"
   ```

  5. Creating property file for different environment

   The new enhancement allow users to configure params at Env/application.properties/conjur.properties including below
   Properties file can created for different environment from Dev to Prod 
   exampleService-dev.properties
   exampleService-uat.properties 
   exampleService-prod.properties
   
  6. The local Git repository is created only for Dev/Test deployment. To work on the Production deployment, Repository needs to be more secured 

   and created in remote location.
   
   Spring Cloud Config Server provides a HTTP resource based API for extenral configuration by Key/Value pair or .yml file
   @EnableConfigServer annotation will be used to embed the server into the Spring Boot Application
   
   ### Example code:
   #### Step 1:
   ```
   @EnableConfigServer
   @SpringBootApplication
   public class ConfigServerApplication {
    
    public static void main(String[] args) {
      SpringApplication.run(ConfigServerApplication.class, args);
    }
  }
   ```
   #### Step 2: Create bootstrap.properties file in the <b> /resource</b> folder
   
   bootstrap.properties
   ```
   server.port= <port to connect to the server>
   spring.profiles.active= <environment dev/stage/test/prod>
   spring.cloud.config.server.native.search-locations= <location of the storage where the configuration file is maintained,
                             by default its Git>
   spring.cloud.config.server.git.clone-on-start= <specify 'true' to refresh the config server on server startup>
   spring.application.name=<point to the properties file for that particular application>
   ```
   
   #### pom.xml
   Following dependency needs to be included for Spring Cloud Config Server to be started
   
   ```
   <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
   </dependency>
   <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
   </dependency>
   <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

## Environment setup
	
<details>
	<summary><b>Conjur OSS</b></summary>
	
Once the setup steps are successfully run, define the variables needed to make the connection between the plugin and Conjur. 
You can do this by setting Conjur Properties or [Environment variables](#environment-variables).

#### CyberArk Conjur Configuration Properties
The following configuration properties can be set in the standard `spring-boot` configuration files, `application.properties` or `application.yml`:

| Parameter name                    | Description                                                      |
|:----------------------------------|:-----------------------------------------------------------------|
| conjur.account                    | CyberArk Conjur Account                                          |
| conjur.appliance-url              | CyberArk Conjur Appliance URL                                    |
| conjur.authn-login                | CyberArk Conjur User /host identity                              |
| conjur.authn-api-key              | CyberArk Conjur API KEY of the host                              |
| conjur.auth-token-file            | CyberArk Conjur Token, stored in a file                          |
| conjur.cert-file                  | CyberArk Conjur SSL Certificate path (server CA cert)            |
| conjur.ssl-certificate            | CyberArk Conjur SSL Certificate Content (server CA cert)         |
| conjur.resilience-enabled         | CyberArk Conjur Resilience Enabled flag                          |
| conjur.resilience-max-attempts    | CyberArk Conjur max retry attempts                               |
| conjur.resilience-wait-duration   | CyberArk Conjur wait duration to retry                           |
| conjur.authn-cert-service-id      | Service ID of the `authn-cert` authenticator (e.g. `acme-vm`)   |
| conjur.client-cert-file           | Path to the PEM client certificate file (for authn-cert / mTLS) |
| conjur.client-key-file            | Path to the PEM PKCS#8 private key file (for authn-cert / mTLS) |       
	
<h4 id="environment-variables">
 Environment Variables
</h4>

In Conjur,environment variables are mapped to configuration variables by prepending `CONJUR_` to the all-caps name of the configuration variable. 
For example:`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT`.

If no other configuration is done (e.g. over system properties or CLI parameters), include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

| Name                    | Environment ID                | Description                | API KEY | JWT  |
| ----------------------- | ------------------------------| -------------------------- | ------- | ---- |
| Conjur Account          | CONJUR_ACCOUNT                | Account to connect         | Yes     | Yes  |
| API key                 | CONJUR_AUTHN_API_KEY          | User/host API Key/password | Yes     | No   |
| Connection url          | CONJUR_APPLIANCE_URL          | Conjur instance to connect | Yes     | Yes  |
| User/host identity      | CONJUR_AUTHN_LOGIN            | User /host identity        | Yes     | No   |
| SSL Certificate Path    | CONJUR_CERT_FILE              | Path to certificate file   | Yes     | Yes  |
| SSL Certificate Content | CONJUR_SSL_CERTIFICATE        | Certificate content        | Yes     | Yes  |
| Resilience Enabled      | CONJUR_RESILIENCE_ENABLED     | Conjur Resilience Enabled  | Yes     | Yes  |
| Max-Attempts            | CONJUR_RESILIENCE_MAX_ATTEMPTS| Conjur Retry Max-Attemts   | Yes     | Yes  |
| Wait-Duration           | CONJUR_RESILIENCE_WAIT_DURATION| Conjur Retry Wait-Duration| Yes     | Yes  |

Only one CONJUR_CERT_FILE and CONJUR_SSL_CERTIFICATE is required. There are two variables to allow the user to specify the path to a certificate file or provide the certificate data directly in an environment variable.
</details>

<details>
	<summary><b>Conjur Enterprise</b></summary>
Once the setup steps are successfully run, define the variables needed to make the connection between the plugin and Conjur. 
You can do this by setting Conjur Properties or [Environment variables](#environment-variables).

#### CyberArk Conjur Configuration Properties
The following configuration properties can be set in the standard `spring-boot` configuration files, `application.properties` or `application.yml`:

| Parameter name           | Description                             |
|:-------------------------|:----------------------------------------|
| conjur.account           | CyberArk Conjur Account                 |
| conjur.appliance-url     | CyberArk Conjur Appliance URL           |
| conjur.authn-login       | CyberArk Conjur User /host identity     |
| conjur.authn-api-key     | CyberArk Conjur API KEY of the host     |
| conjur.auth-token-file   | CyberArk Conjur Token, stored in a file |
| conjur.cert-file         | CyberArk Conjur SSL Certificate path    |
| conjur.ssl-certificate   | CyberArk Conjur SSL Certificate Content |
| conjur.authenticator-id  | CyberArk Conjur authenticator ID        |
| conjur.jwt-token-path    | CyberArk Conjur Path of the JWT Token   |
| conjur.mapping-path      | CyberArk Conjur Mapping Path            |
| conjur.resilience-enabled| CyberArk Conjur Resilience Enable flag  |
| conjur.max-wait          | CyberArk Conjur Max retry attempts      |
| conjur.wait-duration     | CyberArk Conjur Max wait duration for retry|

	
<h4 id="environment-variables">
 Environment Variables
</h4>

In Conjur,environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. 
For example:`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT`.

If no other configuration is done (e.g. over system properties or CLI parameters), include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

| Name                    | Environment ID                  | Description                | API KEY | JWT  |
| ----------------------- | ------------------------------- | -------------------------- | ------- | ---- |
| Conjur Account          | CONJUR_ACCOUNT                  | Account to connect         | Yes     | Yes  |
| API key                 | CONJUR_AUTHN_API_KEY            | User/host API Key/password | Yes     | No   |
| Connection url          | CONJUR_APPLIANCE_URL            | Conjur instance to connect | Yes     | Yes  |
| User/host identity      | CONJUR_AUTHN_LOGIN              | User /host identity        | Yes     | No   |
| SSL Certificate Path    | CONJUR_CERT_FILE                | Path to certificate file   | Yes     | Yes  |
| SSL Certificate Content | CONJUR_SSL_CERTIFICATE          | Certificate content        | Yes     | Yes  |
| Path of the JWT Token   | CONJUR_JWT_TOKEN_PATH           | Path of the JWT Token      | No      | Yes  |
| Conjur authenticator ID | CONJUR_AUTHENTICATOR_ID         | Conjur authenticator ID    | No      | Yes  |
| Conjur MAPPING PATH     | CONJUR_MAPPING_PATH             | Conjur Mapping PATH        | Yes     | Yes  |
| Resilience Enabled      | CONJUR_RESILIENCE_ENABLED       | Conjur Resilience Enabled  | Yes     | Yes  |
| Max-Attempts            | CONJUR_RESILIENCE_MAX_ATTEMPTS  | Conjur Retry Max-Attemts   | Yes     | Yes  |
| Wait-Duration           | CONJUR_RESILIENCE_WAIT_DURATION | Conjur Retry Wait-Duration | Yes     | Yes  |
| Conjur Scan All @Values | CONJUR_SCAN_ALL_VALUES          | Scan all `@Values` annotations (default: false) | Yes | Yes |

Only one CONJUR_CERT_FILE and CONJUR_SSL_CERTIFICATE is required. There are two variables to allow the user to specify the path to a certificate file or provide the certificate data directly in an environment variable.
</details>
	
<details>
	<summary><b>Conjur Cloud</b></summary>

Once the setup steps are successfully run, define the variables needed to make the connection between the plugin and Conjur. 
You can do this by setting Conjur Properties or [Environment variables](#environment-variables).

#### CyberArk Conjur Configuration Properties
The following configuration properties can be set in the standard `spring-boot` configuration files, `application.properties` or `application.yml`:

| Parameter name                    | Description                                                      |
|:----------------------------------|:-----------------------------------------------------------------|
| conjur.account                    | CyberArk Conjur Account (use `conjur` for Conjur Cloud)          |
| conjur.appliance-url              | CyberArk Conjur Appliance URL                                    |
| conjur.authn-login                | CyberArk Conjur User /host identity                              |
| conjur.authn-api-key              | CyberArk Conjur API KEY of the host                              |
| conjur.authn-cert-service-id      | Service ID of the `authn-cert` authenticator (e.g. `acme-vm`)   |
| conjur.client-cert-file           | Path to the PEM client certificate file (for authn-cert / mTLS) |
| conjur.client-key-file            | Path to the PEM PKCS#8 private key file (for authn-cert / mTLS) |

> **authn-cert with Conjur Cloud (Secrets Manager SaaS)**
>
> Client certificate authentication on Conjur Cloud is routed through **Secrets Manager Edge**.
> The Edge API exposes endpoints under the `/api/` path prefix.  
> To match the Edge URL format (`https://<edge-url>/api/authn-cert/...`), set
> `conjur.appliance-url` to the Edge base URL **including the `/api` suffix**:
>
> ```properties
> conjur.appliance-url=https://us-edge.acme.dev/api
> conjur.account=conjur
> conjur.authn-cert-service-id=acme-vm
> conjur.authn-login=host/data/vm-workloads/vm-01
> conjur.client-cert-file=/etc/conjur/client.crt
> conjur.client-key-file=/etc/conjur/client-pkcs8.key
> ```
>
> The private key must be in **PKCS#8 PEM format** (`-----BEGIN PRIVATE KEY-----`).  
> To convert a PKCS#1 key: `openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem`
	
<h4 id="environment-variables">
 Environment Variables
</h4>

In Conjur, environment variables are mapped to configuration variables by prepending `CONJUR_` to the all-caps name of the configuration variable. 
For example:`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT`.

If no other configuration is done (e.g. over system properties or CLI parameters), include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

| Name                    | Environment ID                  | Description                                      | API KEY | JWT | authn-cert |
| ----------------------- | ------------------------------- | ------------------------------------------------ | ------- | --- | ---------- |
| Conjur Account          | CONJUR_ACCOUNT                  | Account to connect (`conjur` for Conjur Cloud)   | Yes     | Yes | Yes        |
| API key                 | CONJUR_AUTHN_API_KEY            | User/host API Key/password                       | Yes     | No  | No         |
| Connection url          | CONJUR_APPLIANCE_URL            | Conjur / Edge API URL (include `/api` for Edge)  | Yes     | Yes | Yes        |
| User/host identity      | CONJUR_AUTHN_LOGIN              | User /host identity (workload ID for authn-cert) | Yes     | No  | Yes        |
| Cert service ID         | CONJUR_AUTHN_CERT_SERVICE_ID    | authn-cert authenticator service ID              | No      | No  | Yes        |
| Client certificate file | CONJUR_CLIENT_CERT_FILE         | Path to PEM client certificate (authn-cert)      | No      | No  | Yes        |
| Client private key file | CONJUR_CLIENT_KEY_FILE          | Path to PEM PKCS#8 private key (authn-cert)      | No      | No  | Yes        |

</details>

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
* CONJUR_JWT_TOKEN_PATH in the Name field and the JWT token path (Only required for JWT)
* CONJUR_AUTHENTICATOR_ID in the Name field and the Conjur authenticator ID (Only required for JWT)
* For IntelliJ, set up trusted Conjur self-signed certs by following the steps outlined [here](https://www.jetbrains.com/help/idea/settings-tools-server-certificates.html).

## Using the Conjur Spring Boot Plugin

There are two ways to use the plugin.

* @Value annotation and an optional conjur.properties file that enables the mapping of secret names.
* @ConjurValue and @ConjurValues, which are Conjur native annotations (custom annotations) that enable individual and bulk secret retrieval.

#### Option 1: Spring Standard @Value annotation

The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Plugin routes the look up to Conjur through the Conjur Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in CyberArk Conjur.

 Example use case: Given the following Conjur path `policy/my-application` containing this configuration data pair `database.password=mysecretpassword`, the following `@Configuration` class uses `@ConjurPropertySource` to contribute `policy/my-application` to the environment's set of `PropertySources.`


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
into your Spring Boot code allows you to retrieve a single secret from the CyberArk Conjur. `@ConjurValues` allows you to retrieve multiple secrets from the CyberArk Conjur.

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

##Option 3: Spring Cloud @value Annotation 


	@SpringBootApplication
	public class ConjurSpringCloudPluginTest implements CommandLineRunner{
		
		private static Logger logger = LoggerFactory.getLogger(ConjurSpringCloudPluginTest.class);@Value("${test1}")
	private byte[] pass1;
	
	@Value("${test2}")
	private byte[] pass2;
	
	@Value("${test3}")
	private byte[] pass3;
	
	@Autowired
	ApplicationContext appContext;
	
	public static void main(String[] args) {
		
	    SpringApplication.run(ConjurSpringCloudPluginTest.class, args);
	}
	public void run(String... args) throws Exception {
		
		logger.info("By Using Standard Spring annotation -->  " + new String(pass1) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass2) + "  " );
		logger.info("By Using Standard Spring annotation -->  " + new String(pass3) + "  " );
	
	}}

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

