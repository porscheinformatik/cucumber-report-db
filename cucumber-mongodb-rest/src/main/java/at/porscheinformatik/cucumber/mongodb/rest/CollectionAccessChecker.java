package at.porscheinformatik.cucumber.mongodb.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import at.porscheinformatik.cucumber.mongodb.rest.controller.Roles;

public abstract class CollectionAccessChecker
{
    public static boolean hasAccess(MongoOperations mongodb, String collectionName)
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

        if (roles.contains(Roles.ROLE_ADMIN))
        {
            return true;
        }

        Query query = new Query();
        Criteria crit = Criteria.where("name").is(collectionName).and("rights").in(roles);
        query.addCriteria(crit);
        query.fields().include("name");
        return mongodb.exists(query, "products");
    }
}
