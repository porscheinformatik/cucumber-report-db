package at.porscheinformatik.cucumber.nosql.db;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import at.porscheinformatik.cucumber.nosql.exception.DatabaseException;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

/**
 * @author Stefan Mayer (yms)
 */
@Service
public class MongoDB implements Database
{
    private static final String DEFAULT_MONGO_DB_HOST = "localhost";
    private static final int DEFAULT_MONGO_DB_PORT = 27017;

    private String dbName;
    private String dbCollection;
    private String dbHost;
    private int dbPort;

    private MongoClient connection;
    private DB database;
    private DBCollection collection;

    public MongoDB()
    {
        this.dbHost = DEFAULT_MONGO_DB_HOST;
        this.dbPort = DEFAULT_MONGO_DB_PORT;
    }

    public MongoDB(String dbHost, int dbPort, String dbName, String dbCollection)
    {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbCollection = dbCollection;
    }

    @Override
    public void connect() throws UnknownHostException
    {
        connection = new MongoClient(dbHost, dbPort);
        this.database = connection.getDB(dbName);
        this.collection = database.getCollection(dbCollection);
    }

    public void connect(String dbName, String dbCollection) throws UnknownHostException
    {
        connect(dbHost, dbPort, dbName, dbCollection);
    }

    public void connect(String dbHost, int dbPort, String dbName, String dbCollection) throws UnknownHostException
    {
        connection = new MongoClient(dbHost, dbPort);
        this.database = connection.getDB(dbName);
        this.collection = database.getCollection(dbCollection);
    }

    @Override
    public void insert(String jsonData)
    {
        DBObject dbObject = (DBObject) JSON.parse(jsonData);
        collection.insert(dbObject);
    }

    @Override
    public void insertMedia(String name, InputStream data)
    {
        GridFS gfsPhoto = new GridFS(database, collection.getName());
        GridFSInputFile gfsFile;

        gfsFile = gfsPhoto.createFile(data);

        gfsFile.setFilename(name);
        gfsFile.save();
    }

    @Override
    public Collection<InputStream> fetchMedia(String fileName)
    {
        Collection<InputStream> resuls = new ArrayList<InputStream>();
        GridFS gridFS = new GridFS(database, collection.getName());
        List<GridFSDBFile> imagesForOutput = gridFS.find(fileName);

        for (GridFSDBFile gridFSDBFile : imagesForOutput)
        {
            resuls.add(gridFSDBFile.getInputStream());
        }

        return resuls;
    }

    @Override
    public <T> Collection<T> fetch(Class<T> type, String query)
    {
        if (!database.collectionExists(collection.getName()))
        {
            throw new DatabaseException("The collection: \"" + collection.getName() + "\" does not exist!");
        }

        Collection<T> results = new ArrayList<T>();
        DBObject dbObject = (DBObject) JSON.parse(query);
        Iterator<DBObject> fetchResults = collection.find(dbObject).iterator();

        while (fetchResults.hasNext())
        {
            T result = new Gson().fromJson(fetchResults.next().toString(), type);
            results.add(result);
        }

        return results;
    }

    @Override
    public <T> T fetchLast(Class<T> type, String query)
    {
        if (!database.collectionExists(collection.getName()))
        {
            throw new DatabaseException("The collection: \"" + collection.getName() + "\" does not exist!");
        }

        DBObject dbObject = (DBObject) JSON.parse(query);
        DBObject sortBy = new BasicDBObject("_id", -1);
        DBObject excludeId = new BasicDBObject("_id", 0);

        Iterator<DBObject> fetchResults = collection.find(dbObject, excludeId).sort(sortBy).limit(1).iterator();

        return new Gson().fromJson(fetchResults.next().toString(), type);
    }

    @Override
    public void close()
    {
        connection.close();
    }

    public DB getDB()
    {
        return database;
    }

    public DBCollection getDBCollection()
    {
        return collection;
    }

    public GridFS getGridFS()
    {
        return new GridFS(database, collection.getName());
    }
}
