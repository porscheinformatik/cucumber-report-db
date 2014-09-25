package at.porscheinformatik.common.utils.silkplugin;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class SilkSynchronizer
{
    private static final String CONFIG_FILE = "config.properties";

    private static final String DEFAULT_SILK_IDENTIFIER = "#sctm_execdef_id";
    private static final String DEFAULT_SILK_VERSION = "#sctm_version";
    private static final String DEFAULT_SILK_EXECUTION_DEF_NAME = "#sctm_execdef_name";
    public static final String SERVER_BASE_URL = "server.base.url";
    public static final String REPORT_OBSOLETE_LIMIT_PROPERTY = "report.obsolete-limit";
    public static final String COLLECTION_NAMING_REGEX_CONVENTION = "collection.naming-convention";

    private static final Logger LOGGER = LoggerFactory.getLogger(SilkSynchronizer.class);

    private Properties config;
    private WebResource restResource;

    @Before
    public void preCondition()
    {
        LOGGER.info("Starting Turntable-Silk-BDD-Plugin");

        loadConfigurations(CONFIG_FILE);

        String baseUrl = config.getProperty(SERVER_BASE_URL);
        Client client = Client.create(new DefaultClientConfig());
        restResource = client.resource(baseUrl);
    }

    public Properties loadConfigurations(String file)
    {
        config = new Properties();
        try
        {
            LOGGER.info("Loading properties from URL \"" + getClass().getResource("/" + file) + "\"");
            InputStream in = new FileInputStream(new File(System.getProperty("user.home"), file));
            config.load(in);
            in.close();
        }
        catch (IOException e)
        {
            LOGGER.error("Error loading properties", e);
            fail("[Error] Could not read the configuration file!");
        }
        return config;
    }
    
    private String getCollectionName()
    {
        final String version = System.getProperty(DEFAULT_SILK_VERSION);
        final String product = System.getProperty(DEFAULT_SILK_EXECUTION_DEF_NAME);
        final String collection = String.format("%s_%s", product.split("_", 2)[0], version);
        final String namingConvention = config.getProperty(COLLECTION_NAMING_REGEX_CONVENTION);

        if (!Pattern.matches(namingConvention, collection))
        {
            final String error =
                String.format("[ERROR] Invalid naming convention for the given collection (current: %s, expected: %s)",
                    collection, namingConvention);
            LOGGER.error(error);
            fail(error);
        }

        return collection;
    }

    @After
    public void postCondition()
    {
        LOGGER.info("Finished SilkBDDPlugin");
    }

    @Test
    public void run()
    {
        String executionId = System.getProperty(DEFAULT_SILK_IDENTIFIER);
        String obsoleteLimitInDays = config.getProperty(REPORT_OBSOLETE_LIMIT_PROPERTY);

        try
        {
            String result = restResource.path("rest").path("reports").path(getCollectionName()).path(executionId)
                    .queryParam("obsoleteLimitInDays", obsoleteLimitInDays).get(String.class);
            LOGGER.info("Test passed: "+ result);
        }
        catch (UniformInterfaceException e)
        {
            String errormessage = e.getResponse().getEntity(String.class);
            fail(errormessage);
        }
    }

}
