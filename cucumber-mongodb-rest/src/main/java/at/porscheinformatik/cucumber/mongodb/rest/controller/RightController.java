package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;

@Controller
@RequestMapping("/rest/rights")
public class RightController
{
    @Autowired
    private MongoOperations mongodb;

    @Secured(Roles.ROLE_ADMIN)
    @RequestMapping(value = "/{product}", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getRight(@PathVariable(value = "product") String product) throws IOException
    {
        BasicDBObject query = new BasicDBObject();
        query.put("name", product);

        @SuppressWarnings("unchecked")
        List<String> products = mongodb.getCollection("products").distinct("rights", query);
        return products;
    }

    @Secured(Roles.ROLE_ADMIN)
    @RequestMapping(value = "/{product}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> postRight(@PathVariable(value = "product") String product,
        @RequestBody String newGroup) throws IOException
    {
        Query query = new Query();
        Criteria criteria = Criteria.where("name").is(product);
        query.addCriteria(criteria);

        Update update = new Update();
        update.addToSet("rights", newGroup);

        if (mongodb.updateFirst(query, update, "products").isUpdateOfExisting())
        {
            return new ResponseEntity<Object>(HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<Object>(HttpStatus.NOT_MODIFIED);
        }
    }

    @Secured(Roles.ROLE_ADMIN)
    @RequestMapping(value = "/{product}/{group}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> deleteRight(@PathVariable(value = "product") String product,
        @PathVariable(value = "group") String group) throws IOException
    {
        Query query = new Query();
        Criteria criteria = Criteria.where("name").is(product);
        query.addCriteria(criteria);

        Update update = new Update();
        update.pull("rights", group);

        if (mongodb.updateFirst(query, update, "products").isUpdateOfExisting())
        {
            return new ResponseEntity<Object>(HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<Object>(HttpStatus.NOT_MODIFIED);
        }
    }
}