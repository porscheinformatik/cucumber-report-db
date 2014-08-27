package at.porscheinformatik.cucumber.formatter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import cucumber.runtime.CucumberException;
import gherkin.formatter.NiceAppendable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.porscheinformatik.cucumber.nosql.db.MongoDB;
import at.porscheinformatik.cucumber.nosql.driver.DatabaseDriver;
import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

/**
 * @author Stefan Mayer (yms)
 */
public abstract class MongoDbFormatter extends AbstractJsonFormatter
{
    private static final Logger LOG = LoggerFactory.getLogger(MongoDbFormatter.class);

    private DatabaseDriver databaseDriver;

    protected abstract String getHost();
    protected abstract int getPort();
    protected abstract String getDbName();
    protected abstract String getCollection();
    
    public MongoDbFormatter()
    {
        super();
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
            LOG.warn("Could not connect to NoSQL database!");
        }
    }

    protected void dbInsertJson(String data)
    {
        try
        {
            databaseDriver.insertData(data);
        }
        catch (Exception e)
        {
            LOG.warn("Could not insert JSON data to NoSQL database!");
        }
    }

    protected void dbInsertMedia(String fileName, InputStream inputStream)
    {
        try
        {
            databaseDriver.insertMedia(fileName, inputStream);
        }
        catch (Exception e)
        {
            LOG.warn("Could not insert media data (" + fileName + ") to NoSQL database!");
        }
    }

    @Override
    protected String doEmbedding(String extension, byte[] data)
    {
        String fileName = new StringBuilder("embedded")
                .append(embeddedIndex++)
                .append("_")
                .append(getFormattedDate())
                .append(".")
                .append(extension).toString();

        dbInsertMedia(fileName, new ByteArrayInputStream(data));
        return fileName;
    }

    @Override
    public void close()
    {
        super.close();
        databaseDriver.close();
    }

    @Override
    protected NiceAppendable jsOut()
    {
        try
        {
            return new NiceAppendable(new OutputStreamWriter(new DbOutputStream(), "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CucumberException(e);
        }
    }

    public class DbOutputStream extends OutputStream
    {
        private static final String DATE_REGEX = "\"date\":\"(.*)\"";
        private static final String DATE_REPLACEMENT = "\"date\":\\{\"\\$date\":\"$1\"\\}";

        private StringBuilder output = new StringBuilder();

        @Override
        public void write(int b) throws IOException
        {
            output.append((char) b);
        }

        @Override
        public void close() throws IOException
        {
            final String preparedJsonForDb = output.toString().replaceAll(DATE_REGEX, DATE_REPLACEMENT);
            dbInsertJson(preparedJsonForDb);
        }
    }

}
