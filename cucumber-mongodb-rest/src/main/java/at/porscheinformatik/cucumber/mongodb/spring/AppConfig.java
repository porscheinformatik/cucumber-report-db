package at.porscheinformatik.cucumber.mongodb.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Stefan Mayer (yms)
 */
@EnableWebMvc
@Configuration
@ComponentScan("at.porscheinformatik.cucumber")
public class AppConfig
{

}