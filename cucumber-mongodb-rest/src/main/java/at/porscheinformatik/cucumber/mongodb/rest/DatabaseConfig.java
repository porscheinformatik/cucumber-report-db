package at.porscheinformatik.cucumber.mongodb.rest;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
public class DatabaseConfig extends AbstractMongoConfiguration
{
    private static final String DEFAULT_MONGO_DB_HOST = "localhost";
    private static final String DEFAULT_MONGO_DB_PORT = "27017";
    private static final String DEFAULT_DATABASE_NAME = "bddReports";

    @Override
    protected String getDatabaseName()
    {
        return getDatabase();
    }

    @Override
    public Mongo mongo() throws UnknownHostException
    {
        String dbHost = getHost();
        int dbPort = getPort();
        return new MongoClient(dbHost, dbPort);
    }

    public static int getPort()
    {
        return Integer.parseInt(System.getProperty("mongodb.port", DEFAULT_MONGO_DB_PORT));
    }

    public static String getHost()
    {
        return System.getProperty("mongodb.host", DEFAULT_MONGO_DB_HOST);
    }

    public static String getDatabase()
    {
        return System.getProperty("mongodb.database", DEFAULT_DATABASE_NAME);
    }
}
