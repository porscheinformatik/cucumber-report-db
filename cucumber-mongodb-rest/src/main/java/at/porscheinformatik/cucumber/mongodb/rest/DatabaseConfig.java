package at.porscheinformatik.cucumber.mongodb.rest;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
public class DatabaseConfig extends AbstractMongoConfiguration
{
    private static final String DEFAULT_MONGO_DB_URI = "mongodb://localhost:27017/";
    private static final String DEFAULT_DATABASE_NAME = "bddReports";
    public static final String SYSTEM_PROPERTY_MONGO_URI = "cucumber.report.db.mongo.uri";
    public static final String SYSTEM_PROPERTY_MONGO_DATABASE = "cucumber.report.db.mongo.database";

    @Override
    protected String getDatabaseName()
    {
        return getDatabase();
    }

    @Override
    public Mongo mongo() throws UnknownHostException
    {
        String dbUri = getUri();
        return new MongoClient(new MongoClientURI(dbUri));
    }

    public static String getUri()
    {
        return System.getProperty(SYSTEM_PROPERTY_MONGO_URI, DEFAULT_MONGO_DB_URI);
    }

    public static String getDatabase()
    {
        return System.getProperty(SYSTEM_PROPERTY_MONGO_DATABASE, DEFAULT_DATABASE_NAME);
    }
}
