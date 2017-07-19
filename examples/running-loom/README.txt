This project is setup to pull down the version of loom specified in the pom
and run it, loading any adapters that are in the ./adapters folder which is
empty by default.

To run Loom using maven:

$ mvn jetty:run-war -Ddeployment.properties=deployment.properties

If you also want to override log4j configuration you need to set another system property, i.e.:

$ mvn jetty:run-war -Ddeployment.properties=<my .properties file> -Dlog4j.configuration=<my log4j.properties file>

Note that log4j will look on the classpath for the named file.  If your properties file is not on the classpath you must prefix your path with file:, e.g.:

$ mvn jetty:run-war -Dlog4j.configuration=file:<my log4j.properties file>

Alternatively you could run using jetty-runner (make sure you add the -D arguments first):

$ java -Ddeployment.properties=deployment.properties -jar jetty-runner.jar --config jetty-runner.xml wars\loom-server.war

Access Loom via http://localhost:9099/weaver