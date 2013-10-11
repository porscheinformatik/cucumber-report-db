package cucumber.examples.java.calculator;

import cucumber.api.junit.Cucumber;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * Create testdata for the HTML report
 */
@RunWith(Cucumber.class)
@Cucumber.Options(format = "at.porscheinformatik.cucumber.formatter.HtmlFormatter:target/html")
@Ignore
public class RunCukesTest {
}
