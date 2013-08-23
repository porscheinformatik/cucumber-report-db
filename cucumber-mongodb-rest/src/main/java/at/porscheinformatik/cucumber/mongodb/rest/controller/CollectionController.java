package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import at.porscheinformatik.cucumber.nosql.driver.MongoDbDriver;

import com.mongodb.util.JSON;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/collection")
public class CollectionController
{
    @Autowired
    private MongoDbDriver mongoDbDriver;

    @RequestMapping(value = "/{dbName}/", method = RequestMethod.GET)
    @ResponseBody
    public void getCollections(
        HttpServletRequest request,
        @PathVariable(value = "dbName") String dbName,
        HttpServletResponse response) throws IOException
    {
        mongoDbDriver.connect(dbName, "");
        response.getWriter().write(JSON.serialize(mongoDbDriver.getCollectionNames()));
        response.setContentType("application/json");
    }

    @RequestMapping(value = "/{dbName}/{collection}", method = RequestMethod.GET)
    @ResponseBody
    public void getCollectionData(
        HttpServletRequest request,
        @PathVariable(value = "dbName") String dbName,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
        mongoDbDriver.connect(dbName, collection);
        response.getWriter().write(JSON.serialize(mongoDbDriver.getCollection().getStats()));
        response.setContentType("application/json");
    }
}
