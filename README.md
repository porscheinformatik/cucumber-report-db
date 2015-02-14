Cucumber Report DB
===================

Stores results of BDD tests with Cucumber-JVM in a database and provides reporting capabilities. It can display graphs of different test runs
as well as compute statistics of most failed steps or highest runtime grouped by steps

The project includes the submodule 'silk-bdd-result-plugin' which is a plugin for fetching the test results from the database into a Silk Central test management system.

Want to learn more? [See the wiki.](https://github.com/porscheinformatik/cucumber-report-db/wiki) or take a look at a [demo with sample data](https://cucumber-report-db.herokuapp.com)

## Setup

The Cucumber-Report-DB has two main components. The [cucumber-report-web](cucumber-report-web) and the [cucumber-formatter](cucumber-formatter). In order to store and display the results of cucumber tests the web application
has to be set up correctly and the formatter has to be included in the cucumber test run.

### Web application (cucumber-report-web)

Requirements:
* Java 6 or later
* MongoDB 2.6.3 or later [Download](https://www.mongodb.org/downloads) or [as-a-service](https://mongolab.com/)
* Maven 3

Setup:
* Clone the Repo
* Start the MongoDB ```mongod --dbpath /path/to/db```
* execute ```mvn clean install```
* execute ```java -jar cucumber-report-web/target/cucumber-report-web*-bootable.jar```

Per default the web application connects to a mongodb hosted on ```mongodb://localhost:27017/``` without authentication (the mongodb default)

By setting the following system properties the connection to the mongodb can be configured
* cucumber.report.db.mongo.uri (uri to mongodb e.g. ```mongodb://user:passwd@xxx.mongolab.com:55980/cucumberreportdb```)
* cucumber.report.db.mongo.username
* cucumber.report.db.mongo.password
* cucumber.report.db.mongo.database

Security (Http basic auth):
By setting the system property ```cucumber.report.db.secured``` a http basic auth for the web application can be activated.
You have to specify the location of a property file by setting the system property cucumber.report.db.config. The config file must have the format [cucumber-report-web.properties)[cucumber-report-web\src\main\resources\cucumber-report-web.properties]

### Formatter (MongoDbFormatter)
In order to publish the results of cucumber test runs to the web application, include the ```at.porscheinformatik.cucumber.formatter.MongoDbFormatter``` in your cucumber-run (see ```at.porscheinformatik.cucumber.formatter.MongoFormatIT``` for an example)

Per default the formatter expects the web application to be hosted on ```http://localhost:8081``` and the name of the subject under test is "product_version"
By setting the following system property the location of the webapp can be defined
* cucumber.report.server.baseUrl

By setting the following system properties the name and version of the subject under test can be specified
* cucumber.report.product.name
* cucumber.report.product.version

When the webapp has enabled basic auth the user and password for the formatter have to be specified via the following system properties
* cucumber.report.server.username
* cucumber.report.server.password

## License

This software is licensed under the Apache Software License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0.txt