package at.porscheinformatik.cucumber.nosql.driver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.porscheinformatik.cucumber.nosql.db.MongoDB;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author Stefan Mayer (yms)
 */
@Service
public class MongoDbDriver implements DatabaseDriver
{
    @Autowired
    private MongoDB mongoDb;

    public MongoDbDriver()
    {
    }

    public MongoDbDriver(MongoDB mongoDb)
    {
        this.mongoDb = mongoDb;
    }

    @Override
    public void connect() throws Exception
    {
        this.mongoDb.connect();
    }

    public void connect(String dbName, String dbCollection) throws UnknownHostException
    {
        this.mongoDb.connect(dbName, dbCollection);
    }

    public void connect(String dbHost, int dbPort, String dbName, String dbCollection) throws UnknownHostException
    {
        this.mongoDb.connect(dbHost, dbPort, dbName, dbCollection);
    }

    @Override
    public void insertData(String jsonData)
    {
        this.mongoDb.insert(jsonData);
    }

    @Override
    public void insertFile(File jsonFile) throws IOException
    {
        @SuppressWarnings("resource")
        FileReader reader = new FileReader(jsonFile);

        char[] data = new char[(int) jsonFile.length()];
        reader.read(data);

        this.mongoDb.insert(new String(data));
    }

    @Override
    public <T> Collection<T> fetchByFromToDate(Class<T> dtoType, String fieldName, Date from, Date to)
    {
        DBObject dbObject = new BasicDBObject();
        dbObject.put(fieldName, BasicDBObjectBuilder.start("$gte", from).add("$lte", to).get());

        return mongoDb.fetch(dtoType, dbObject.toString());
    }

    @Override
    public <T> Collection<T> fetchByLastDay(Class<T> dtoType, String fieldName)
    {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, -1);
        from.set(Calendar.HOUR, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);

        Calendar to = Calendar.getInstance();
        to.add(Calendar.DAY_OF_MONTH, -1);
        to.set(Calendar.HOUR, 24);
        to.set(Calendar.MINUTE, 0);
        to.set(Calendar.SECOND, 0);

        return fetchByFromToDate(dtoType, fieldName, from.getTime(), to.getTime());
    }

    @Override
    public <T> Collection<T> fetchByDate(Class<T> dtoType, String fieldName, Date date)
    {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(fieldName, date);

        return mongoDb.fetch(dtoType, dbObject.toString());
    }

    @Override
    public <T> Collection<T> fetchByValue(Class<T> dtoType, String fieldName, String value)
    {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(fieldName, value);

        return mongoDb.fetch(dtoType, dbObject.toString());
    }

    @Override
    public <T> T fetchLastByValue(Class<T> dtoType, String fieldName, String value)
    {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(fieldName, value);

        return mongoDb.fetchLast(dtoType, dbObject.toString());
    }

    @Override
    public void close()
    {
        mongoDb.close();
    }

    @Override
    public void insertMedia(String name, InputStream data)
    {
        mongoDb.insertMedia(name, data);
    }

    @Override
    public Collection<InputStream> fetchMedia(String fileName)
    {
        return mongoDb.fetchMedia(fileName);
    }

    public GridFSDBFile fetchMediaFile(String fileName)
    {
        return mongoDb.getGridFS().findOne(fileName);
    }

    public Set<String> getCollectionNames()
    {
        return mongoDb.getDB().getCollectionNames();
    }

    public DBCollection getCollection()
    {
        return mongoDb.getDBCollection();
    }
}
