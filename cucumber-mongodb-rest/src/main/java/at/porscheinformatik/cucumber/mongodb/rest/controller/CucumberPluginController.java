package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Controller
@RequestMapping("/rest/cucumberplugin")
public class CucumberPluginController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberPluginController.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoDbFactory dbFactory;

    @Autowired
    private MongoConverter converter;

    @Secured("ROLE_FORMATTER")
    @RequestMapping(value = "/{product}/{version}/{category}/report", method = RequestMethod.POST)
    public ResponseEntity<Object> insertData(@PathVariable("product") String product,
        @PathVariable("version") String version, @PathVariable("category") String category,
        @RequestBody String reportString)
    {
        Gson gsonInstance = new Gson();
        JsonObject collectionEntry = new JsonObject();
        JsonObject reportJson = gsonInstance.fromJson(reportString, JsonObject.class);
        collectionEntry.addProperty("version", version);
        collectionEntry.addProperty("category", category);
        collectionEntry.add("report", reportJson);

        if (!mongoTemplate.collectionExists(product))
        {
            LOGGER.info("create new entry in products for {}", product);
            JsonObject productJson = new JsonObject();
            JsonArray rightsJson = new JsonArray();

            productJson.addProperty("name", product);
            productJson.add("rights", rightsJson);
            mongoTemplate.insert(productJson.toString(), "products");
        }

        LOGGER.info("insert report into collection {}, {}", product, version);
        mongoTemplate.insert(collectionEntry.toString(), product);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    //supports the empty default category
    @Secured("ROLE_FORMATTER")
    @RequestMapping(value = "/{product}/{version}/report", method = RequestMethod.POST)
    public ResponseEntity<Object> insertData(@PathVariable("product") String product,
        @PathVariable("version") String version, @RequestBody String reportString)
    {
        return insertData(product, version, "", reportString);
    }

    //TODO update to work like insertReport
    @Secured("ROLE_FORMATTER")
    @RequestMapping(value = "/{collection}/{version}/media", method = RequestMethod.POST)
    public ResponseEntity<?> insertMedia(@PathVariable("collection") String collection,
        @PathVariable("version") String version, @RequestParam(value = "filename") String filename,
        HttpServletRequest httpServletRequest) throws IOException
    {
        LOGGER.info("insert binary into collection {}, {}", collection, version);
        String contentType = httpServletRequest.getHeader("content-type");
        GridFsOperations gridfs = new GridFsTemplate(dbFactory, converter, collection + '_' + version);
        gridfs.store(httpServletRequest.getInputStream(), filename, contentType);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }
}
