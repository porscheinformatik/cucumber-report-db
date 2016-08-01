package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import at.porscheinformatik.cucumber.mongodb.rest.CollectionAccessChecker;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/rest/query")
public class QueryController
{
    @Autowired
    private MongoOperations mongodb;

    //TODO maybe add response for failure
    @Secured(Roles.ROLE_ADMIN)
    @RequestMapping(value = "/{collection}/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteDocument(@PathVariable("collection") String collection,
        @PathVariable("id") String id)
    {
        if ("_ALL".equals(id))
        {
            dropWholeCollection(collection);
            return new ResponseEntity<Object>(HttpStatus.OK);
        }

        Query query = new Query(Criteria.where("_id").is(id));
        mongodb.remove(query, collection);
        if (mongodb.getCollection(collection).count() == 0)
        {
            dropWholeCollection(collection);
        }
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    private void dropWholeCollection(String collection)
    {
        System.out.println("Removing " + mongodb.getCollection(collection).count() + " objects from " + collection);
        mongodb.remove(new Query(Criteria.where("name").is(collection)), "products");
        mongodb.dropCollection(collection);
        System.out.println("Collection " + collection + " removed");
    }

    @RequestMapping(value = "/{collection}/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> find(
        @PathVariable("collection") String collection,
        @PathVariable("id") String id)
    {
        if (!CollectionAccessChecker.hasAccess(mongodb, collection))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));
        return ResponseEntity.ok(mongodb.getCollection(collection).find(query).toArray().toString());
    }

    @RequestMapping(value = "/{collection}/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> find(
        @PathVariable("collection") String collection,
        @RequestParam(value = "version", required = false) String version,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "limit", required = false) String limit,
        @RequestParam(value = "skip", required = false) String skip,
        @RequestParam(value = "sort", required = false) String sort,
        @RequestParam(value = "last", required = false) String last) throws IOException
    {
        if (!CollectionAccessChecker.hasAccess(mongodb, collection))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        DBCursor dbData = getDbCursor(collection, version, category);
        skipElements(collection, skip, last, dbData);
        limitResult(limit, dbData);
        sortResult(sort, dbData);

        return ResponseEntity.ok((dbData.toArray().toString()));
    }

    private DBCursor getDbCursor(final String collection, final String version, final String category)
    {
        DBCollection dbCollection = mongodb.getCollection(collection);

        BasicDBObject dbObject = new BasicDBObject();
        if (version != null)
        {
            dbObject.put("version", version);
        }
        if (category != null)
        {
            dbObject.put("category", category);
        }
        return dbCollection.find(dbObject);
    }

    private void skipElements(final String collection, final String skipValue, final String last, final DBCursor dbData)
    {
        if (skipValue != null)
        {
            dbData.skip(Integer.parseInt(skipValue));
        }
        else if (last != null)
        {
            int length = (int) mongodb.getCollection(collection).count();
            int nrOfSkips = skipToLast(length, Integer.valueOf(last));
            dbData.skip(nrOfSkips);
        }
    }

    int skipToLast(int cursorLength, int limit)
    {
        if (cursorLength > limit)
        {
            return cursorLength - limit;
        }
        return 0;
    }

    private void limitResult(final String limitValue, final DBCursor dbData)
    {
        if (limitValue != null)
        {
            dbData.limit(Integer.parseInt(limitValue));
        }
    }

    private void sortResult(final String sort, final DBCursor dbData)
    {
        if (sort != null && Boolean.parseBoolean(sort))
        {
            dbData.sort(new BasicDBObject("_id", -1));
        }
    }
}
