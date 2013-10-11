package cucumber.examples.java.calculator;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(format = "at.porscheinformatik.cucumber.formatter.HtmlFormatter:target/html")
public class RunCukesTest {
}
