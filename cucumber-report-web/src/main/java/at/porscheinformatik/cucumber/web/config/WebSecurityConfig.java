package at.porscheinformatik.cucumber.web.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import at.porscheinformatik.cucumber.mongodb.rest.controller.Roles;
import at.porscheinformatik.cucumber.web.config.CucumberReportProperties.Security.SecurityType;
import at.porscheinformatik.cucumber.web.config.CucumberReportProperties.User;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    @Autowired
    private CucumberReportProperties settings;

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        if (settings.getSecurity().getType() == SecurityType.DISABLED)
        {
            http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();

            http.anonymous().authorities(Roles.ROLE_ADMIN);
        }
        else
        {
            http.csrf().disable();

            http.httpBasic().and()
                .authorizeRequests()
                .anyRequest().authenticated();
        }
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        if (settings.getSecurity().getType() == SecurityType.LDAP)
        {
            configureLDAP(auth);
        }
        else if (settings.getSecurity().getType() == SecurityType.IN_MEMORY)
        {
            configureInMemory(auth);
        }
        else if (settings.getSecurity().getType() == SecurityType.DISABLED)
        {
            //nothing
        }
    }

    private void configureLDAP(AuthenticationManagerBuilder auth) throws Exception
    {
        LdapContextSource ldapContextSource = ldapContextSource();

        DefaultLdapAuthoritiesPopulator ldapAuthoritiesPopulator =
            new DefaultLdapAuthoritiesPopulator(ldapContextSource, "ou=group,dc=tree,dc=porsche,dc=co,dc=at")
            {
                @Override
                protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username)
                {
                    Set<GrantedAuthority> authorities = new HashSet<>();
                    if (settings.getSecurity().getAdmins().contains(username))
                    {
                        authorities.add(new SimpleGrantedAuthority(Roles.ROLE_ADMIN));
                    }
                    if (settings.getSecurity().getFormatters().contains(username))
                    {
                        authorities.add(new SimpleGrantedAuthority(Roles.ROLE_FORMATTER));
                    }
                    return authorities;
                }
            };
        ldapAuthoritiesPopulator.setRolePrefix("ROLE_");
        ldapAuthoritiesPopulator.setGroupSearchFilter("(memberUid={1})");

        auth.ldapAuthentication()
            .contextSource(ldapContextSource)
            .userSearchBase("ou=people,dc=tree,dc=porsche,dc=co,dc=at")
            .userSearchFilter("(uid={0})")
            .rolePrefix("ROLE_")
            .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator);
    }

    public LdapContextSource ldapContextSource()
    {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(settings.getSecurity().getLdap().getUrl());
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }

    private void configureInMemory(AuthenticationManagerBuilder auth) throws Exception
    {
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryAuthentication =
            auth.inMemoryAuthentication();
        for (User user : settings.getUsers())
        {
            inMemoryAuthentication
                .withUser(user.getName())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]));
        }
    }
}
