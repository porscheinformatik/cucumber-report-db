package at.porscheinformatik.cucumber.mongodb.rest;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.authentication.UserCredentials;
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
    public static final String SYSTEM_PROPERTY_MONGO_USERNAME = "cucumber.report.db.mongo.username";
    public static final String SYSTEM_PROPERTY_MONGO_PASSWORD = "cucumber.report.db.mongo.password";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    @Override
    protected String getDatabaseName()
    {
        return getDatabase();
    }

    @Override
    public Mongo mongo() throws UnknownHostException
    {
        String dbUri = getUri();
        LOGGER.info("Connecting to mongo {}", dbUri);
        return new MongoClient(new MongoClientURI(dbUri));
    }

    @Override
    protected UserCredentials getUserCredentials()
    {
        String mongoUsername = System.getProperty(SYSTEM_PROPERTY_MONGO_USERNAME);
        String mongoPassword = System.getProperty(SYSTEM_PROPERTY_MONGO_PASSWORD);
        if (mongoUsername == null)
        {
            //no authentication
            return null;
        }
        return new UserCredentials(mongoUsername, mongoPassword);
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
