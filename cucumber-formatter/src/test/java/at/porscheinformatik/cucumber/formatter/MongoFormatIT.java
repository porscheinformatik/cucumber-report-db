package at.porscheinformatik.cucumber.formatter;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.Ignore;
import org.junit.runner.RunWith;

@CucumberOptions(features = "classpath:at/porscheinformatik/cucumber/formatter",
        format = "at.porscheinformatik.cucumber.formatter.MongoDbFormatter",
        glue = "at.porscheinformatik.cucumber.formatter", tags = "~@Skip")
@RunWith(Cucumber.class)
@Ignore
public class MongoFormatIT
{

}
