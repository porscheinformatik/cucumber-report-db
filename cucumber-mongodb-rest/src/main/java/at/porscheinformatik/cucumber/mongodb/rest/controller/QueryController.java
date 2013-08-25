package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import at.porscheinformatik.cucumber.mongodb.service.UtilService;
import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/query")
public class QueryController
{
    @Autowired
    private MongoDbDriver mongoDbDriver;

    @Autowired
    private UtilService utilService;

    @RequestMapping(value = "/{dbName}/{collection}/", method = RequestMethod.GET)
    @ResponseBody
    public void find(
        HttpServletRequest request,
        @PathVariable(value = "dbName") String dbName,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
        final String limitValue = request.getParameter("limit");
        final String skipValue = request.getParameter("skip");
        final String field = request.getParameter("field");
        final String value = request.getParameter("value");
        final String sort = request.getParameter("sort");

        mongoDbDriver.connect(dbName, collection);
        DBCollection dbCollection = mongoDbDriver.getCollection();
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

        response.setContentType("application/json");
        response.getWriter().write(utilService.formatJson(dbData));
    }

    private DBCursor findByValue(DBCollection dbCollection, String field, String value)
    {
        Object val = null;
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
}
