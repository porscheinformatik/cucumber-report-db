package at.porscheinformatik.cucumber.mongodb.rest.db;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import at.porscheinformatik.cucumber.mongodb.rest.DatabaseConfig;

/**
 * @author Stefan Mayer (yms)
 */
@Service
public class MongoDB
{
    @Autowired
    private MongoClient mongoClient;

    public <T> T fetchLast(Class<T> type, String collectionName, String fieldName, String fieldValue)
    {
        DB database = mongoClient.getDB(DatabaseConfig.getDatabase());
        if (!database.collectionExists(collectionName))
        {
            throw new RuntimeException("The collection: \"" + collectionName + "\" does not exist!");
        }
        DBCollection collection = database.getCollection(collectionName);
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(fieldName, fieldValue);

        DBObject sortBy = new BasicDBObject("_id", -1);
        DBObject excludeId = new BasicDBObject("_id", 0);

        Iterator<DBObject> fetchResults = collection.find(dbObject, excludeId).sort(sortBy).limit(1).iterator();

        return new Gson().fromJson(fetchResults.next().toString(), type);
    }

}
