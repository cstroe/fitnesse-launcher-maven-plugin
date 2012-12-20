This pom.xml contains example configuration
for using fitnesse-launcher-maven-plugin
to execute FitNesse integration tests for a java project.

The config consists mainly of 2 profiles:

-P wiki : This profile is for use when writing tests using the FitNesse wiki.
          Simply run 'mvn install -P wiki' and use a browser to visit http://localhost:<port>/<suite>
          <port> and <suite> can be found in the fitnesse-launcher-maven-plugin <configuration> section.
         
-P auto : This profile will boot FitNesse in an automated mode
          to run the configured test suite. Run in this way,
          fitnesse-launcher-maven-plugin will create both JUnit style XML reports and HTML reports.
          By default they can be found in target/fitnesse/results and target/fitnesse/reports respectively. 
