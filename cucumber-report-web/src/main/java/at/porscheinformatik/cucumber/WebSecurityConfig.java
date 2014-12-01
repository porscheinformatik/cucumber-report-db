package at.porscheinformatik.cucumber;

import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import at.porscheinformatik.cucumber.mongodb.rest.controller.Roles;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests().anyRequest().authenticated().and().csrf().disable().httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        ResourceBundle credentials = ResourceBundle.getBundle("cucumber-report-web");

        String defaultUserName = credentials.getString("default.user");
        String defaultUserPassword = credentials.getString("default.password");
        auth.inMemoryAuthentication().withUser(defaultUserName).password(defaultUserPassword).roles(Roles.USER);

        String adminUserName = credentials.getString("admin.user");
        String adminUserPassword = credentials.getString("admin.password");
        auth.inMemoryAuthentication().withUser(adminUserName).password(adminUserPassword).roles(Roles.ADMIN);

        String formatterUserName = credentials.getString("formatter.user");
        String formatterUserPassword = credentials.getString("formatter.password");
        auth.inMemoryAuthentication().withUser(formatterUserName).password(formatterUserPassword).roles(Roles.FORMATTER);

        String reportUserName = credentials.getString("report.user");
        String reportUserPassword = credentials.getString("report.password");
        auth.inMemoryAuthentication().withUser(reportUserName).password(reportUserPassword).roles(Roles.REPORTER);

    }
}
