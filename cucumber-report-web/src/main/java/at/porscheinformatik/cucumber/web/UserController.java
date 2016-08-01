package at.porscheinformatik.cucumber.web;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController
{
    @RequestMapping("/current/")
    public Object currentUser()
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        
        return securityContext.getAuthentication();
    }
}
