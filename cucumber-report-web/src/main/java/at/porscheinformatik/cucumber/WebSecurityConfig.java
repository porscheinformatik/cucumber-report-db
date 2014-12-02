package at.porscheinformatik.cucumber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import at.porscheinformatik.cucumber.mongodb.rest.controller.Roles;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        String secured = System.getProperty("cucumber.report.db.secured");
        if (secured != null)
        {
            http.authorizeRequests().anyRequest().authenticated().and().csrf().disable().httpBasic();
        }
        else
        {
            http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        Properties config = loadConfig();

        String defaultUserName = config.getProperty("default.user");
        String defaultUserPassword = config.getProperty("default.password");
        auth.inMemoryAuthentication().withUser(defaultUserName).password(defaultUserPassword).roles(Roles.USER);

        String adminUserName = config.getProperty("admin.user");
        String adminUserPassword = config.getProperty("admin.password");
        auth.inMemoryAuthentication().withUser(adminUserName).password(adminUserPassword).roles(Roles.ADMIN);

        String formatterUserName = config.getProperty("formatter.user");
        String formatterUserPassword = config.getProperty("formatter.password");
        auth.inMemoryAuthentication().withUser(formatterUserName).password(formatterUserPassword).roles(Roles.FORMATTER);

        String reportUserName = config.getProperty("report.user");
        String reportUserPassword = config.getProperty("report.password");
        auth.inMemoryAuthentication().withUser(reportUserName).password(reportUserPassword).roles(Roles.REPORTER);
    }

    private Properties loadConfig() throws IOException
    {
        Properties config = new Properties();
        String configLocation = System.getProperty("cucumber.report.db.config");
        InputStream in;
        if (configLocation != null)
        {
            File configFile = new File(configLocation);
            in = new FileInputStream(configFile);
        } else
        {
            in = this.getClass().getResourceAsStream("/cucumber-report-web.properties");
        }
        config.load(in);
        return config;
    }

    @Bean
    public AnonymousAuthenticationFilter createAnonymousAuthenticationFilter()
    {
        return new AnonymousAuthenticationFilter("key");
    }
}
