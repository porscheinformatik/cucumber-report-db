package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import at.porscheinformatik.cucumber.mongodb.rest.CollectionAccessChecker;

/**
 * Handles requests for the list of all products and for general information about a product
 */
@Controller
@RequestMapping("rest/collection")
public class CollectionController
{
    @Autowired
    private MongoOperations mongodb;

    /**
     * @return List<String> a list of names of all products the user has access to
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getCollections() throws IOException
    {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(SecurityContextHolder.getContext()
            .getAuthentication().getAuthorities());
        List<String> roles = Lists.transform(authorities, new Function<GrantedAuthority, String>()
        {
            @Override
            public String apply(final GrantedAuthority grantedAuthority)
            {
                return grantedAuthority.getAuthority();
            }
        });

        Query query = new Query();
        if (!roles.contains(Roles.ROLE_ADMIN))
        {
            Criteria crit = Criteria.where("rights").in(roles);
            query.addCriteria(crit);
        }
        query.fields().include("name");
        ArrayList<String> collectionNames = new ArrayList<String>();
        List<NameObject> productsResult = mongodb.find(query, NameObject.class, "products");
        for (NameObject product : productsResult)
        {
            collectionNames.add(product.getName());
        }
        return collectionNames;
    }

    /**
     * Alias for requests to "/"
     * @return List<String> a list of names of all products the user has access to
     */
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getProducts() throws IOException
    {
        return getCollections();
    }

    /**
     * Queries for the different categories the reports in the specified collection are of
     * @param collection This specifies the desired collection
     * @return ResponseEntity<String> a response with a list of categories in the collection or "forbidden" status when the user doesn't have a required role
     */
    @RequestMapping(value = "/{collection}/categories", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getCategories(
        @PathVariable("collection") String collection)
    {
        if (!CollectionAccessChecker.hasAccess(mongodb, collection))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(mongodb.getCollection(collection).distinct("category").toString());
    }

    /**
     * Queries for the different versions the reports in the specified collection are made on
     * @param collection This specifies the desired collection
     * @return ResponseEntity<String> a response with a list of versions in the collection or "forbidden" status when the user doesn't have a required role
     */
    @RequestMapping(value = "/{collection}/versions", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getVersions(
        @PathVariable("collection") String collection)
    {
        if (!CollectionAccessChecker.hasAccess(mongodb, collection))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(mongodb.getCollection(collection).distinct("version").toString());
    }
    
    /**
     * Queries for the number of reports in the specified collection
     * @param collection This specifies the desired collection
     * @return ResponseEntity<?> a response with the number of reports or "forbidden" status when the user doesn't have a required role
     */
    @RequestMapping(value = "/{collection}/count", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getReportCount(
        @PathVariable("collection") String collection)
    {
        if (!CollectionAccessChecker.hasAccess(mongodb, collection))
        {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(mongodb.getCollection(collection).count());
    }
}
