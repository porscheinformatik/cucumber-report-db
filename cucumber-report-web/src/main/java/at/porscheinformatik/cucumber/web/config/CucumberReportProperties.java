package at.porscheinformatik.cucumber.web.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("cucumber.report")
public class CucumberReportProperties
{
    private Security security = new Security();

    private List<User> users = new ArrayList<>();

    public Security getSecurity()
    {
        return security;
    }

    public List<User> getUsers()
    {
        return users;
    }

    public static class Security
    {
        // TODO doc cucumber.report.db.secured
        private SecurityType type = SecurityType.DISABLED;

        private List<String> admins = new ArrayList<>();

        private List<String> formatters = new ArrayList<>();

        public static enum SecurityType
        {
            LDAP,
            IN_MEMORY,
            DISABLED
        }

        private Ldap ldap = new Ldap();

        public SecurityType getType()
        {
            return type;
        }

        public void setType(SecurityType type)
        {
            this.type = type;
        }

        public Ldap getLdap()
        {
            return ldap;
        }

        public List<String> getAdmins()
        {
            return admins;
        }

        public List<String> getFormatters()
        {
            return formatters;
        }
    }

    public static class Ldap
    {
        private String url;

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }
    }

    public static class User
    {
        private String name;

        private String password;

        private List<String> roles = new ArrayList<>();

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public List<String> getRoles()
        {
            return roles;
        }
    }
}
