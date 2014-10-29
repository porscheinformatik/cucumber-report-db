package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/rest/query")
public class QueryController
{
    @Autowired
    private MongoOperations mongodb;

    @RequestMapping(value = "/{collection}/{id}", method = RequestMethod.DELETE)
    public void deleteDocument(@PathVariable("collection") String collection, @PathVariable("id") String id)
    {
        Query query = new Query(Criteria.where("_id").is(id));
        mongodb.remove(query, collection);
        if (mongodb.getCollection(collection).count() == 0)
        {
            mongodb.dropCollection(collection);
        }
    }

    @RequestMapping(value = "/{collection}/", method = RequestMethod.GET)
    @ResponseBody
    public void find(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
        final String limitValue = request.getParameter("limit");
        final String skipValue = request.getParameter("skip");
        final String field = request.getParameter("field");
        final String value = request.getParameter("value");
        final String sort = request.getParameter("sort");

        DBCollection dbCollection = mongodb.getCollection(collection);
        DBCursor dbData;
        DBObject sortBy = new BasicDBObject("_id", -1);

        if (field == null || value == null)
        {
            dbData = dbCollection.find();
        }
        else
        {
            dbData = findByValue(dbCollection, field, value);
        }

        if (skipValue != null)
        {
            dbData.skip(Integer.parseInt(skipValue));
        }

        if (limitValue != null)
        {
            dbData.limit(Integer.parseInt(limitValue));
        }

        if (sort != null && Boolean.parseBoolean(sort))
        {
            dbData.sort(sortBy);
        }

        // TODO check maybe Spring can handle DBCursor automatically
        response.setContentType("application/json");
        response.getWriter().write(formatJson(dbData));
    }

    private DBCursor findByValue(DBCollection dbCollection, String field, String value)
    {
        Object val;
        try
        {
            val = Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            try
            {
                Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(value);
                val = cal.getTime();
            }
            catch (IllegalArgumentException e1)
            {
                val = value;
            }
        }

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(field, val);
        return dbCollection.find(dbObject);
    }

    private static String formatJson(DBCursor cursor)
    {
        StringBuilder buf = new StringBuilder();

        buf.append("[");
        while (cursor.hasNext())
        {
            JSON.serialize(cursor.next(), buf);
            buf.append(",");
        }

        if (buf.length() > 1)
        {
            buf.setCharAt(buf.length() - 1, ']');
        }
        else
        {
            buf.append("]");
        }
        cursor.close();
        
        return buf.toString();
    }
}
