package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.CommandResult;

/**
 * @author Stefan Mayer (yms)
 */
@Controller
@RequestMapping("/rest/collection")
public class CollectionController
{
    @Autowired
    private MongoOperations mongodb;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> getCollections() throws IOException
    {
        return mongodb.getCollectionNames();
    }

    @RequestMapping(value = "/{collection}", method = RequestMethod.GET)
    @ResponseBody
    public CommandResult getCollectionData(@PathVariable(value = "collection") String collection) throws IOException
    {
        return mongodb.getCollection(collection).getStats();
    }
}
