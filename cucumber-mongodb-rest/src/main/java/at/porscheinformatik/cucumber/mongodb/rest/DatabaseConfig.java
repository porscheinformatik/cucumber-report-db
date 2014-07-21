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

    @Override
    protected String getDatabaseName()
    {
        return "bddReports";
    }
    
    @Override
    public Mongo mongo() throws UnknownHostException
    {
        String dbHost = System.getProperty("mongodb.host", DEFAULT_MONGO_DB_HOST);
        int dbPort = Integer.parseInt(System.getProperty("mongodb.port", DEFAULT_MONGO_DB_PORT));
        return new MongoClient(dbHost, dbPort);
    }
}
