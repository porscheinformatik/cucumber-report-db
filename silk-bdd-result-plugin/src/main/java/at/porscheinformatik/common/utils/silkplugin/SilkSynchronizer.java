package at.porscheinformatik.common.utils.silkplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.porscheinformatik.common.utils.silkplugin.dto.FeatureDTO;
import at.porscheinformatik.common.utils.silkplugin.dto.ReportDTO;
import at.porscheinformatik.common.utils.silkplugin.dto.ScenarioDTO;
import at.porscheinformatik.common.utils.silkplugin.dto.StepDTO;
import at.porscheinformatik.cucumber.nosql.db.MongoDB;
import at.porscheinformatik.cucumber.nosql.driver.DatabaseDriver;
import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

/**
 * @author Stefan Mayer (yms)
 */
public class SilkSynchronizer
{
    private static final String CONFIG_FILE = "config.properties";
    private static final String NL = System.getProperty("line.separator");

    private static final String DEFAULT_SILK_IDENTIFIER = "#sctm_execdef_id";
    private static final String DEFAULT_SILK_VERSION = "#sctm_version";
    private static final String DEFAULT_SILK_EXECUTION_DEF_NAME = "#sctm_execdef_name";

    private static final String DEFAULT_SILK_EXECUTION_ID_NAME = "@SILK_ID_%s";

    private static Logger LOGGER;

    private String collection;
    private DatabaseDriver dbDriver;
    private Properties config;
    
    @Before
    public void preCondition()
    {
        LOGGER = Logger.getLogger(SilkSynchronizer.class);
        LOGGER.info("Starting Turntable-Silk-BDD-Plugin");

        loadConfigurations(CONFIG_FILE);

        final String host = config.getProperty("database.host");
        final int port = Integer.valueOf(config.getProperty("database.port"));
        final String db = config.getProperty("database.name");

        initDbDriver(host, port, db, getCollectionName());
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
            LOGGER.error(e);
            Assert.fail("[Error] Could not read the configuration file!");
        }
        return config;
    }
    
    private String getCollectionName()
    {
        final String version = System.getProperty(DEFAULT_SILK_VERSION);
        final String product = System.getProperty(DEFAULT_SILK_EXECUTION_DEF_NAME);
        final String collection = String.format("%s_%s", product.split("_", 2)[0], version);
        final String namingConvention = config.getProperty("collection.naming-convention", "[A-Z0-9]*\\_[A-Z0-9\\.\\-]*");

        if (!Pattern.matches(namingConvention, collection))
        {
            final String error =
                String.format("[ERROR] Invalid naming convention for the given collection (current: %s, expected: %s)",
                    collection, namingConvention);
            LOGGER.error(error);
            Assert.fail(error);
        }

        return collection;
    }

    private void initDbDriver(String host, int port, String db, String collection)
    {
        try
        {
            this.collection = collection;
            dbDriver = new MongoDbDriver(new MongoDB(host, port, db, collection));
            dbDriver.connect();
            
            LOGGER.info("Using the following collection: " + collection);
            LOGGER.info("Created MongoDB database factory:");
            LOGGER.info("    host: \"" + host + "\"");
            LOGGER.info("    port: \"" + port + "\"");
            LOGGER.info("    db: \"" + db + "\"");
            LOGGER.info("    collection: \"" + collection + "\"");
        }
        catch (Exception e)
        {
            LOGGER.error(e);
            Assert.fail("[Error] Cannot connect to nosql database!");
        }
    }

    @After
    public void postCondition()
    {
        LOGGER.info("Finished SilkBDDPlugin");
        dbDriver.close();
    }

    @Test
    public void run()
    {
        String executionId = System.getProperty(DEFAULT_SILK_IDENTIFIER);
        checkLastReportById(dbDriver, executionId);
    }

    /**
     * Checks the latest report (chosen by id) and report the results to Silk Central via JUnit
     * 
     * @param dbDriver the database driver class
     * @param execId the id to find the respective scenario
     */
    private void checkLastReportById(DatabaseDriver dbDriver, String execId)
    {
        LOGGER.info("Fetch bdd report from last run");

        final String execIdTag =
            String.format(config.getProperty("silk.execution-id-name", DEFAULT_SILK_EXECUTION_ID_NAME), execId);
        
        try
        {
            //ReportDTO report = dbDriver.fetchLastByValue(ReportDTO.class, "features.scenarios.id", execId);
            // fetch the current bdd-report (applied to version and product) by id from the nosql database
            ReportDTO report = dbDriver.fetchLastByValue(ReportDTO.class, "features.scenarios.tags.name", execIdTag);

            for (FeatureDTO feature : report.getFeatures())
            {
                ScenarioDTO scenario;
                if ((scenario = feature.getScenarioByTag(execIdTag)) == null)
                {
                    // no matching scenario was found in the current feature file
                    // -> check the next one
                    continue;
                }
                
                // check if the last report data are not obsolete
                Calendar cal = report.getCalendar();
                cal.add(Calendar.DATE, Integer.valueOf(config.getProperty("report.obsolete-limit", "2")));
                
                if(cal.getTime().compareTo(new Date()) < 0)
                {
                    cal.add(Calendar.DATE, -Integer.valueOf(config.getProperty("report.obsolete-limit", "-2")));
                    
                    final String warning =
                        String.format("[Warning] The data of the last BDD run are obsolete (reportDate: \"%s\")", cal.getTime());
                    LOGGER.warn(warning);
                    Assert.fail(warning);
                }

                // we found a matching scenario. 
                // Now we have to notify JUnit about our status.
                if (isScenarioFailed(scenario))
                {
                    notifyJUnitFailed(report, scenario, execIdTag);
                }
                else
                {
                    notifyJUnitPassed(report, scenario, execIdTag);
                }
                return;
            }

            // if we get here, no matching scenario was found in the database
            // and we cannot complete the process
            final String warning =
                String.format("[Warning] No scenario with id: \"%s\" found in the database!", execIdTag);
            LOGGER.warn(warning);
            Assert.fail(warning);
        }
        catch (NoSuchElementException e)
        {
            final String error = String.format("[Error] No db entry found with id: \"%s\"!", execIdTag);
            LOGGER.error(error);
            Assert.fail(error);
        }
    }

    /**
     * Inform JUnit about success
     * 
     * @param report
     * @param scenario
     */
    private void notifyJUnitPassed(ReportDTO report, ScenarioDTO scenario, String idtentifier)
    {
        LOGGER.info("Test passed: (" + report.getDateDTO().get$date() + ") identifier: \"" + idtentifier + "\" - name: \""
            + scenario.getName()
            + "\" ("
            + scenario.getDescription() + ")");
    }

    /**
     * Inform JUnit about failure
     * 
     * @param report
     * @param scenario
     */
    private void notifyJUnitFailed(ReportDTO report, ScenarioDTO scenario, String idtentifier)
    {
        StringBuilder error = new StringBuilder();
        
        error.append("Test failed: (" + report.getDateDTO().get$date() + ") identifier: \"" + idtentifier
            + "\" - name: \""
            + scenario.getName()
            + " (" + scenario.getDescription() + ")" + NL + NL);

        error.append("  Scenario: " + scenario.getName() + NL);
        error.append("  {" + NL);

        for (StepDTO step : scenario.getSteps())
        {
            error.append("      StepName: " + step.getName() + NL);
            error.append("          Status: " + step.getResult().getStatus() + NL);
            if (step.getResult().getError_message() != null)
            {
                error.append("          Error-Message: " + step.getResult().getError_message() + NL);
            }
        }
        
        final String bddReportUrl = config.getProperty("bdd.report") + "/#/reports/" + collection + "/features/" + report.getDateDTO().get$date() + "/feature/" + (scenario.getId().split(";"))[0] + "?searchText=" + scenario.getName();
        error.append("      Url: " + bddReportUrl + NL);
        error.append("  }" + NL);

        LOGGER.error(error.toString());
        Assert.fail(error.toString());
    }

    private boolean isScenarioFailed(ScenarioDTO scenario)
    {
        return (scenario.getResult().getFailedStepCount() != null || scenario.getResult().getSkippedStepCount() != null);
    }

    @SuppressWarnings("unused")
    private void dumpProperties()
    {
        Properties props = System.getProperties();

        for (Entry<Object, Object> entry : props.entrySet())
        {
            LOGGER.info(entry.getKey() + "=" + entry.getValue());
        }
    }
}
