# Conjur Spring Boot Api Plugin

The Conjur Spring Boot Api Plugin provides client-side support for externalized configuration of secrets in a distributed system. You can integrate the plugin with exisiting and new Spring Boot applications to retrieve secrets from Conjur. Using the Spring Boot Api Plugin, you can retrieve application credentials and secrets stored in Conjur with minimal code changes to the existing Spring Boot application code.

## Benefits of storing application secrets in [CyberArk's Vault](https://www.conjur.org/)

* Provides one central location to store and retrieve secrets for applications across all environments.
* Supports the management of static and dynamic secrets such as username and password for remote applications and resources.
* Provides credentials for external services like MySQL, PostgreSQL, Apache Cassandra, Couchbase, MongoDB, Consul, AWS, and more.


## Features

The following features are available with the Spring Boot Api Plugin:

* Retrieve a single secret from the CyberArk Vault by specifying the path to the secret in the Vault.
* Retrieve multiple secrets from the CyberArk Vault by specifying the paths to the secrets in the Vault.
* Retrieve secrets from the CyberArk Vault and initialize the Spring environment with remote property sources.


## Limitations

The Spring Boot Api Plugin does not support creating, deleting, or updating secrets.

## Technical Requirements

|  Technology    |  Version |
|----------------|----------|
| Java           |  11      |
| Conjur OSS     |  1.9+    |
| Conjur Enterprise | 12.5  |
|ConjurSDK(Java) |  4.0.0   |
|Conjur API      |  5.1     |


# Prerequisites

The following are prerequisites to using the Spring Boot Api Plugin.

## Conjur setup

Conjur (OSS or Enterprise) and the Conjur CLI are installed in the environment and running in the background.
If you haven't yet done so, follow the instructions for installing [OSS](https://www.conjur.org/get-started/quick-start/oss-environment/) or [Enterprise](https://www.conjur.org/get-started/quick-start/oss-environment/).

Once Conjur and the Conjur CLI are running in the background, you can start setting up your Spring Boot application to work with our Conjur Spring Boot Api Plugin.

### Setup

You can import the Conjur Spring Boot Api Plugin manually by building the source code locally or using a dependency configuration to import from Maven Central. For information about your specific use case, see the following instructions.

#### Using the source code

You can grab the library's dependencies using Maven:

1. Create a new Maven project using an IDE of your choice.
2. If you are using Maven to manage your project's dependencies, include the following
   Spring Boot Api Plugin dependency snippet in your `pom.xml` under `<project>`/`<dependencies>`:

```xml
       <dependency>
         <groupId>com.cyberark.conjur.api</groupId>
	 <artifactId>conjur-api-springboot</artifactId>
	 <version>1.0.0</version>
      </dependency>
```

NOTE: Depending on the Java compiler version you have, you may need to update
the version. At this time, we are targeting compatibility with Java 11:

```xml
  <properties>
    <maven.compiler.source>{version}</maven.compiler.source>
    <maven.compiler.target>{version}</maven.compiler.target>
  </properties>
```

Run `mvn install -DskipTests` in this repository's directory to install the Spring Boot Api Plugin into your local Maven repository.

#### Using the Jarfile

If generating a JAR is preferred, build the library locally and add the dependency to the project manually:

1. Clone the Spring Boot Api Plugin repository locally: `git clone {repo}`
2. Go into the cloned repository with `cd conjur-api-spring-boot`
3. Run `mvn package -DskipTests` to generate a JAR file. The output `.jar` files are located
   in the `target` directory of the repository.
   
4a. For Intellij, follow the steps outlined [here](https://www.jetbrains.com/help/idea/library.html)
    to add the SDK JAR files to the new app's project.

4b. For Eclipse, `right click project > Build Path > Configure Build Path > Library > Add External JARs`.

## Setup trust between app and Conjur

By default, Conjur  generates and uses self-signed SSL certificates. Without trusting them, the Java app cannot connect to the Conjur server using the Conjur APIs. You need to configure your app to trust them. You can accomplish this by using the [Client-level]() ```SSLContext``` when creating the client or with a [JVM-level]() trust by loading the Conjur certificate into Java's CA keystore that holds the list of all the allowed certificates for https connections.

#### Client-level trust

We can set up a trust between the client application and a Conjur server using Java ```javax.net.ssl.SSLContext```. This can be done from Java code during Conjur class initialization.

Usable in Kubernetes/OpenShift environment to setup TLS trust with Conjur server dynamically from the Kubernetes secret and/or configmap data.

```xml
  final String conjurTlsCaPath = "/var/conjur-config/tls-ca.pem";

  final CertificateFactory cf = CertificateFactory.getInstance("X.509");
  final FileInputStream certIs = new FileInputStream(conjurTlsCaPath);
  final Certificate cert = cf.generateCertificate(certIs);

  final KeyStore ks = KeyStore.getInstance("JKS");
  ks.load(null);
  ks.setCertificateEntry("conjurTlsCaPath", cert);

  final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
  tmf.init(ks);

  SSLContext conjurSSLContext = SSLContext.getInstance("TLS");
  conjurSSLContext.init(null, tmf.getTrustManagers(), null);
```
#### JVM-level trust

For a JVM-level trust between Conjur and the API client, you need to load the Conjur certificate into Java's CA keystore that holds the list of all the allowed certificates for https connections.

First, we need to get a copy of this certificate, which you can get using ```openssl```. Run the following step from a terminal with OpenSSL that has access to Conjur:

```
  $ openssl s_client -showcerts -servername myconjurserver.com \
        -connect myconjusrserver.com:443 < /dev/null 2> /dev/null \
        | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > conjur.pem

  $ # Check that the certificate was properly retrieved. If you do not see this kind of output
  $ # ensure that you are providing OpenSSL the correct server information
  $ cat conjur.pem
  -----BEGIN CERTIFICATE-----
  ...
  -----END CERTIFICATE-----		
```
This will save the certificate chain to a file called 'conjur.pem'. Since Java doesn't work natively with the ```pem``` certificate encoding format, you'll need to convert it to the ```der``` format:

```   
  $ openssl x509 -outform der -in conjur.pem -out conjur-default.der
```

Next, you'll need to locate the path to the JRE from the process environment running the Java app. In the case of Java 8 on most standard Linux distributions it's ```/Library/Java/JavaVirtualMachines/jdk-11.0.14.jdk/Contents/Home```. We will export this path to ```$JRE_HOME``` for convenience. If the file ```$JRE_HOME/lib/security/cacerts``` doesn't exist (you might need to be root to see it), double check that the ```JRE_HOME``` path is correct. Once you've found it, you can add the appliance's cert to Java's certificate authority keystore like this:

```
  $ sudo -E keytool -importcert \
  -alias conjur-default \
  -keystore "$JRE_HOME/lib/security/cacerts" \
  -storepass changeit \
  -file ./conjur-default.der

  Owner: CN=myconjurserver.com
  Issuer: CN=myconjurserver.com, OU=Conjur CA, O=myorg
  Serial number: 9e930ced498d74b4faf98e6d4f9d90ebdebebd57
  Valid from: Mon Mar 30 16:51:15 CDT 2020 until: Thu Mar 28 16:51:15 CDT 2030
  Certificate fingerprints:
         SHA1: 7A:A3:78:22:50:03:52:C2:B5:3E:1D:98:48:26:82:71:18:FB:2E:26
         SHA256: ED:77:BA:4A:81:EB:6C:26:E9:82:AC:75:51:99:9A:2F:76:D5:3C:A2:B4:8D:5D:87:EB:A6:01:49:FC:2F:28:FF
  ...
  Trust this certificate? [no]:  yes
  Certificate was added to keystore

  $ # Make sure you do not see `keytool error: java.io.FileNotFoundException` error. If you do,
  $ # your addition of the cert did not work.
```
Note: On macOS, your default Java may not be able to run this tool so you may need to install an alternate JDK like ```openjdk```. You can find more info about this [here](https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_jdk.html) and [here](https://formulae.brew.sh/formula/openjdk).

Verify the addition of the SSL key:

```
  $ sudo -E keytool -list \
    -storepass changeit \
    -keystore $JAVA_HOME/lib/security/cacerts | grep conjur
   conjur-default, May 6, 2020, trustedCertEntry,
```
There you have it! Now you are all configured to start leveraging the Conjur Java API in your Java program.

## Environment setup

Once the setup steps are successfully run, define the variables needed to make the connection between the Api plugin and Conjur. You can do this by setting
[environment variables](#environment-variables).


#### Environment variables

In Conjur (both Open Source and Enterprise), environment variables are mapped to configuration variables
by prepending `CONJUR_` to the all-caps name of the configuration variable. 
For example:`appliance_url` is `CONJUR_APPLIANCE_URL`, `account` is `CONJUR_ACCOUNT`.

If no other configuration is done (e.g. over system properties or CLI parameters), include the following environment variables in the app's runtime environment to use the Spring Boot Plugin.

##### Set environment variables in the Eclipse IDE

1. Select the Client Class in Eclipse, then right click Properties -> Run&Debug Setting-> New.
2. In the Select Configuration popup, click the Java App.
3. In the Edit Launch Configuration properties window, select the Environment Tab and click Add.
4. In the New Environment Variable window, enter the properties with the corresponding name and vale one at a time by clicking the
  Add button followed by Apply & Close.

| Name                     | Environment ID           |   Description                 |
| ------------------------ | ------------------       |   -----------------------     |
| Conjur Account           | CONJUR_ACCOUNT           |   Account to connect          |
| API key                  | CONJUR_AUTHN_API_KEY     |   User/host API Key/password  |
| Connection url           | CONJUR_APPLIANCE_URL     |   Conjur instance to connect  |
| User/host identity       | CONJUR_AUTHN_LOGIN       |   User /host identity         |
  

###### Add environment variables

* Enter CONJUR_ACCOUNT in the Name field and the Account Id created during the Conjur OSS setup. For example: myConjurAccount) as value.
* CONJUR_APPLIANCE_URL is the URL of the Conjur instance to which you are connecting. When connecting to Conjur Enterprise, configure for high availability by including the URL of the master load balancer (if performing read and write operations) or the URL of a follower load balancer (if performing read-only operations).
* CONJUR_AUTHN_LOGIN in the Name field and the host/fileName1 created during the Conjur OSS setup. For example: host/<file name where grant permission is defined for the user/userName (for whom the access is granted in fileName1).
* CONJUR_AUTHN_API_KEY - User/host API key 
* For IntelliJ, set up trusted Conjur self-signed certs by following the steps outlined [here](https://www.jetbrains.com/help/idea/settings-tools-server-certificates.html).

## Using the Conjur Spring Boot Api Plugin

There are two ways to use the plugin.
* @Value annotation and an optional conjur.properties file that enables the mapping of secret names.
* @ConjurValue and @ConjurValues, which are Conjur native annotations (custom annotations) that enable individual and bulk secret retrieval.

#### Option 1: Spring Standard @Value annotation
The `@ConjurPropertySource` annotation allows you to specify the root of a policy to look up. The Spring Boot Api Plugin routes the look up to Conjur through the Conjur Api Spring Boot SDK and a REST API we expose. Using @ConjurPropertySource in conjunction with @Configuration classes is required. The names of secrets, passwords, and user IDs all remain as originally specified. You can fetch Conjur managed secrets using a standard @Value annotation. By adding an optional file with the name `conjur.properties` in a Spring Boot classloader discoverable location `(<a path>/resources/)`, you can map the names of secrets as specified in the application code to the names stored in the CyberArk Vault.

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
    conjur.mapping must be prefixed before passing keys in conjur.property file
----


#### Option 2: Conjur native annotations (custom annotation)
The `@ConjurValue` and `@ConjurValues` annotations are intended for new Spring Boot applications. Injecting `@ConjurValue`
into your Spring Boot code allows you to retrieve a single secret from the CyberArk Vault. `@ConjurValues` allows you to retrieve multiple secrets from the CyberArk Vault.

----
    @Configuration
    public class AppConfig {

    @ConjurValue("policy/my-application/databasePassword")
	private byte[] password;

    @ConjurValues({"policy/my-application/dbUserName","policy/my-application/dbPassword","policy/my-application/dbName"})
    private byte[] secrets;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setPassword(password);
        return testBean;
          }
     }
----
