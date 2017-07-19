# loom
Loom is a service to breakdown IT management silos by integrating and analyzing data collected from disparate systems. Its query and control APIs support the creation of clients (Weavers) that enable users to manage complex systems using a single pane of glass.

All data is held in an in-memory graph store optimized for providing aggregated and summarized views.  The front-ends use novel visualisations for both displaying subsets of the graphs and providing the context for management operations routed via Loom to the appropriate external management system. Loom is extensible through an adapter framework that enables integration with any system.

Example adapters in this repo include an OpenStack integration.  Loom is used to power the management front-ends for HPE's The Machine.  An adapter for HPE's The Machine can be found [here](https://github.com/HewlettPackard/loom-tm-adapter) and the package for deploying Loom including the dashboards can be found [here](https://github.com/HewlettPackard/loom-tm).

## Installation
These instructions have been written for Loom core developers but there is also information here of use if you are a third-party adapter developer and user of Loom. 

The Loom server is written in Java and deployed as a WAR file suitable for use in a web container such as Jetty and Tomcat.  Weaver clients are available for running in a web browser, as a Windows 8/10 App and there is experimental support for Android and iOS. 

## Repo layout
The `loom` folder contains the Loom server and adapters.  The `examples` folder contains a couple of simple maven projects to build a small example adapter and another to launch Loom pulling pre-compiled, versioned assets  avoiding the need to build everything from source.

The Weaver clients may be found in `front-end`.

## Configuring and Building Loom

Note that in these instructions, all folder paths are stated relative to the location of this `README.md`.

### Prerequisites 

Install nodejs v4.4.5.  If you intend to build other projects on the same machine, e.g. loom-tm-ed, you will need access to other versions of node.  Installing and using [nvm](https://github.com/creationix/nvm) (Node Version Manager) will make life a lot easier.

Install Java 1.8 - note: OpenJDK has been tested but not used in production 

Install maven v3+ 

### Configuration

The default Loom configuration will start Loom without any adapters and the logging level is set to INFO or above.  You must therefore create your own configuration to do anything of use.  (Note: default Loom configuration is expressed in ~/loom-server/src/main/resources/deployment.properties and the default logging configuration in ~/loom-server/src/main/resources/log4j.properties).

The recommended steps are as follows:
- In `loom` (recommended), create a folder called `my-adapters`
- Create a `my-deployment.properties` file at the same level as `my-adapters` and modify it to reference your `my-adapters` folder
- Copy any adapter .properties and .jar files that you wish to use into `my-adapters` -  alternatively if your adapter code is already on the classpath you may just use an appropriate .properties file.
 
An example my-deployment.properties looks like:

```
#---------------------------------------------------------------------------------------
# API configuration
#---------------------------------------------------------------------------------------
session.invalidation.frequency=10000
session.invalidation.interval=1800000
# number of requests per second
api.rate.limit=200 

#---------------------------------------------------------------------------------------
# Relationship calculation configuration
#---------------------------------------------------------------------------------------
relation.algorithm.useVisitedIdsThreshold=100000
relation.algorithm.useMultipleThreadsThreshold=10000

#---------------------------------------------------------------------------------------
# Location of Adapters
#---------------------------------------------------------------------------------------
adapter.configs=my-adapters

#---------------------------------------------------------------------------------------
# Max GA / DA size - default to 100 million items 1000000000
#---------------------------------------------------------------------------------------
max.ga.size=1000000000
max.da.size=1000000000

#---------------------------------------------------------------------------------------
# Control stitching behaviour
#---------------------------------------------------------------------------------------
stitching.allow=true
stitching.indexing.allow=true
```

Note that for each adapter project, you may find the relevant .properties file in its root and the JAR file in its target folder.  For example, to use the test file adapter, you would copy both `loom-adapter-file/file.properties` and `loom-adapter-file/target/loomAdapterFile.jar` into your `my-adapters` folder.

At runtime you use `-Ddeployment.properties=…` to point to your own .properties file.  If you wish to override the default logging configuration then you must specify `–Dlog4j.configuration=…` and point to your `log4j.properties` file.

#### CORS Support
If you are serving Weaver (or other browser-based Javascript that talks to Loom) from a domain other than that running Loom then you must set the `Access-Control-Allow-Origin` header in Loom responses to match the `Origin` of your requests - by default it is `*` (wildcard).  You can do this by setting the `cors.origin` deployment property.  E.g. If you are serving Weaver from a local web server then you might set it so:

```
cors.origin=http://localhost
```
Note the inclusion of the http protocol in the value.

### Building
Within the `loom` folder you can use maven to build, test and run Loom.  To perform a clean install that will also run the unit tests:

```sh
$ mvn clean install
```

By default unit tests are run with most targets but can be disabled using in `-DskipUnitTests=true`.

### Running Loom from the Command-line
To deploy Loom in Jetty (on the port defined by the jetty.port property in the `pom.xml`) and skipping the tests do the following either within `loom` (recommended) or `loom/loom-server`:

```sh
$ mvn clean -DskipUnitTests=true jetty:run-war
```

If you wish to override the default deployment properties you will need to set a system property, i.e.:

```sh
$ mvn jetty:run-war -Ddeployment.properties=<my .properties file>
```

And if you also want to override log4j configuration you need to set another system property, i.e.:

```sh
$ mvn jetty:run-war -Ddeployment.properties=<my .properties file> -Dlog4j.configuration=file:<my log4j.properties file>
```

Note that log4j will look on the classpath for the named file.  If your properties file is not on the classpath you must prefix your path with file:, e.g.:

```sh
$ mvn jetty:run-war -Dlog4j.configuration=file:<my log4j.properties file>
```

### Running Loom from the examples command-line

To run Loom from the examples command-line section `examples/running-loom`:

```sh
$ mvn jetty:run-war -Ddeployment.properties=<my .properties file>
```

This will download the loom artifacts, and weaver web-app. 

### Deploying Loom to a remote Jetty Web Container

Alternatively you can copy the .war file found at ```loom-server/target/loom.war``` to your destination web container.  Additionally, you may wish to place Apache HTTPD in front of the web container and use it to proxy API calls to the container and also server the pages for Weaver.  See below for further details. 

When deploying inside a web app container such as Jetty you will need to ensure it starts on the same port that the `pom.xml` files expect, by default this is 9099.  In addition you may wish to give default minimum and maximum JVM heap sizes.  All this information is set in `/etc/default/jetty`, e.g.:

```
JAVA=/usr/bin/java # Path to Java
JAVA_OPTIONS="-Xmx2048m -Xms2048m -XX:MaxPermSize=512m"
NO_START=0 # Start on boot
JETTY_HOME=/opt/jetty
JETTY_HOST=0.0.0.0 # Listen to all hosts
JETTY_ARGS=jetty.port=9099
JETTY_USER=root # Run as this user
```

### Testing API access
The simplest test is to make a REST API call to retrieve the list of Providers - this call does not require authentication.  E.g.:

```
http://localhost:9099/loom/providers
```

### Fronting with Apache HTTPD
A convenient deployment uses Apache HTTPD to front both Jetty (or similar) and serve the Weaver web pages.  This requires an appropriate folder with the Weaver files to be added to the HTTPD configuration and the use of `mod_proxy` to forward API calls to Loom.

For example, should you wish to start Loom from the command-line and serve Weaver pages directly from your working copy the following would be necessary:

1. Enable serving pages from the weaver dist folder:

```
<Directory "C:/src/loom/front-end/apps/web/dist">
    Options Indexes FollowSymLinks
    AllowOverride all
    Order Allow,Deny
    Allow from all
</Directory>
```

2. Add an alias for /weaver to easily locate those pages:

```
Alias /weaver c:/src/loom/front-end/apps/web/dist
```

3. Enable mod_proxy and add the following configuration:

```
# Turn off support for true Proxy behaviour as we are acting as 
# a transparent proxy
ProxyRequests Off
 
# Turn off VIA header as we know where the requests are proxied
ProxyVia Off
 
# Turn on Host header preservation so that the servlet container
# can write links with the correct host and rewriting can be avoided.
ProxyPreserveHost On
 
# Set the permissions for the proxy
<Proxy *>
  AddDefaultCharset off
  Order deny,allow
  Allow from all
</Proxy>
 
# Turn on Proxy status reporting at /status
# This should be better protected than: Allow from all
ProxyStatus On
<Location /status>
  SetHandler server-status
  Order Deny,Allow
  Allow from all
</Location>

ProxyPass /loom http://localhost:9099/loom/loom
```

4. Having done this you may load Weaver by pointing a browser at 
`http://localhost/weaver`

5. And the proxied API endpoint for Loom can be found at 
`http://localhost/loom`

## Build Weaver

Follow the instructions detailed in `front-end\README.md`.

## Automated tests

In addition to unit tests, there are a number of "integration tests" which run against an already deployed Loom server.  In order to launch these tests you must first deploy a Loom server that is using two instances of the OS adapter.  Copy the `loom/adapters/os/fakeAdapterPrivate.properties` and `loom/adapters/os/fakeAdapterPublic.properties` into your adapters configuration folder, e.g. `my-adapters` and then start the Loom server in the normal way as described above.

Lastly run the integration tests so:

```
$ mvn -DskipUnitTests=true -P integration-tests integration-test
```

Note that due to a timing issue not all of these tests correctly pass at the moment.
