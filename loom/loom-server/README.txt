########################################
# Requirements
########################################

maven 3+
Java 1.8+

########################################
# Define your own log4j and deployment properties
########################################

Go to src/main/resources.
Make a copy of log4j.properties-example and call it log4j.properties - tweak as you wish.
Make a copy of deployment.properties-example and call it deployment.properties - tweak as you wish.
Make a copy of applicationContext.xml-example and call it applicationContext.xml - tweak as you wish.

!!! DO NOT commit these two new files back into the repository! !!!

########################################
# Build and run unit tests
########################################

mvn clean install

########################################
# Run Loom locally
########################################

mvn -DskipUnitTests=true jetty:run-war

GET http://localhost:8080/loom/status to confirm Loom is running - it should return an empty payload with 200 status code.
