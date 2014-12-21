Cucumber Report DB
===================

[![Build Status](https://travis-ci.org/stefanmayer/cucumber-report-db.png?branch=master)](https://travis-ci.org/stefanmayer/cucumber-report-db)

Stores results of BDD tests with Cucumber-JVM in a database and provides reporting capabilities.

The project includes the submodule 'silk-bdd-result-plugin' which is a plugin for fetching the test results from the database into a Silc Central test management system.  

Want to learn more? [See the wiki.](https://github.com/porscheinformatik/cucumber-report-db/wiki) or take a look at a [demo with sample data](https://cucumber-report-db.herokuapp.com)

### Report-DB

Requirements:
* Java 6 or laster
* MongoDB 2.6.3 or later [Download](https://www.mongodb.org/downloads) or [as-a-service](https://mongolab.com/)
* Maven 3

Setup:
* Clone the Repo
* Start the MongoDB ```mongod --dbpath /path/to/collection```
* execute ```mvn clean install -PspringBoot```
* execute ```java -cp "cucumber-report-web/target/classes/;cucumber-report-web/target/dependency/*"  at.porscheinformatik.cucumber.CucumberReportApplication```

With the following system properties the app can be configured
* cucumber.report.db.mongo.uri
* cucumber.report.db.mongo.username
* cucumber.report.db.mongo.password

### Setup Formatter
* Include the ```at.porscheinformatik.cucumber.formatter.MongoDbFormatter``` in your cucumber-run (see ```at.porscheinformatik.cucumber.formatter.MongoFormatIT``` for an example)
With the system property cucumber.report.server.baseUrl you can set the path to the "Report-DB"


## License

This software is licensed under the Apache Software License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0.txt
