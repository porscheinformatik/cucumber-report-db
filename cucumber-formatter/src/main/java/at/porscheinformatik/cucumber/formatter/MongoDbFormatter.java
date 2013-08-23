package at.porscheinformatik.cucumber.formatter;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.cucumber.nosql.db.MongoDB;
import at.porscheinformatik.cucumber.nosql.driver.DatabaseDriver;
import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

/**
 * @author Stefan Mayer (yms)
 */
public abstract class MongoDbFormatter extends AbstractDbFormatter
{
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbFormatter.class);

    private DatabaseDriver databaseDriver;

    protected abstract String getHost();
    protected abstract int getPort();
    protected abstract String getDbName();
    protected abstract String getCollection();
    
    public MongoDbFormatter(File htmlReportDir)
    {
        super(htmlReportDir);
        connect();
    }
    
    public void connect()
    {
        try
        {
            MongoDB mongoDB = new MongoDB(getHost(), getPort(), getDbName(), getCollection());
            databaseDriver = new MongoDbDriver(mongoDB);
            databaseDriver.connect();
        }
        catch (Exception e)
        {
            //app-check workaround (yms): 
            //throw new CucumberException(e);
            LOG.warn("Could not connect to NoSQL database!");
        }
    }

    @Override
    public void close()
    {
        super.close();
        databaseDriver.close();
    }

    @Override
    protected void dbInsertJson(String data)
    {
        try
        {
            databaseDriver.insertData(data);
        }
        catch (Exception e)
        {
            //app-check workaround (yms):
            LOG.warn("Could not insert JSON data to NoSQL database!");
        }
    }

    @Override
    protected void dbInsertMedia(String fileName, InputStream inputStream)
    {
        try
        {
            databaseDriver.insertMedia(fileName, inputStream);
        }
        catch (Exception e)
        {
            //app-check workaround (yms):
            LOG.warn("Could not insert media data (" + fileName + ") to NoSQL database!");
        }
    }

}
