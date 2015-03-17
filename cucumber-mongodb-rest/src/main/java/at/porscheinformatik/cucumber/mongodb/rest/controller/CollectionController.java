package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
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
        Set<String> collectionNames = mongodb.getCollectionNames();
        return Sets.filter(collectionNames, new Predicate<String>()
        {
            @Override
            public boolean apply(final String s)
            {
                return isValidCollectionName(s) && isNoChunk(s) && isNoFile(s);
            }
        });
    }

    private boolean isValidCollectionName(final String s)
    {
        return Pattern.compile(".*_.*").matcher(s).matches();
    }

    private boolean isNoChunk(final String s)
    {
        return !s.endsWith(".chunks");
    }

    private boolean isNoFile(final String s)
    {
        return !s.endsWith(".files");
    }

    @RequestMapping(value = "/{collection}", method = RequestMethod.GET)
    @ResponseBody
    public CommandResult getCollectionData(@PathVariable(value = "collection") String collection) throws IOException
    {
        return mongodb.getCollection(collection).getStats();
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> getProducts() throws IOException
    {
        Set<String> collections = getCollections();
        return ImmutableSet.copyOf(Collections2.transform(collections, new Function<String, String>()
        {
            @Override
            public String apply(final String s)
            {
                return s.split("_")[0];
            }
        }));
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    @ResponseBody
    public Set<String> getCategories() throws IOException
    {
        Set<String> collections = getCollections();
        return ImmutableSet.copyOf(Collections2.transform(collections, new Function<String, String>()
        {
            @Override
            public String apply(final String s)
            {
                // e.g. "APPNAME_3.x-SNAPSHOT (Integration)" for integration tests
                String[] split = s.split(" ");
                return split.length == 1 ? "" : split[1].replace(")", "");
            }
        }));
    }
}
