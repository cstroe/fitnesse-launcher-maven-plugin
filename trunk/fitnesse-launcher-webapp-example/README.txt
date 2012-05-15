This pom.xml contains example configuration
for using fitnesse-launcher-maven-plugin
to execute FitNesse integration tests for a webapp.

Much of the config has to do with downloading FitNesse and unpacking it correctly,
and in cleaning up afterwards some of the filesystem mess which FitNesse makes.
The future intent if for all this to be done behind the scenes by the fitnesse-launcher-maven-plugin itself.

Apart from this, the config consists mainly of 3 profiles:

-P headless : This profile is for use on a server or any computer without a display.
              It launches Xvfb (X-Server Virtual Frame Buffer) which must be installed first.
              The intent of this profile is to enable a Continuous Integration server to run the tests. 

-P wiki : This profile is for use when writing tests using the FitNesse wiki.
          Simply run 'mvn install -P wiki' and use a browser to visit http://localhost:<port>/<suite>
          <port> and <suite> can be found in the fitnesse-launcher-maven-plugin <configuration> section.
         
-P auto : This profile will use maven-jetty-plugin to automatically boot the webapp,
          and then boot FitNesse to run the configured test suite. Run in this way,
          fitnesse-launcher-maven-plugin will create both JUnit style XML reports and HTML reports.
          By default they can be found in target/fitnesse/results and target/fitnesse/reports respectively. 
