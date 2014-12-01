package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller
@RequestMapping("/rest/roles")
@Secured({Roles.ROLE_ADMIN, Roles.ROLE_USER})
public class RoleController
{
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public @ResponseBody List<String> getRole()
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
        return roles;
    }
}
