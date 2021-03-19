package mn.jwt.sample.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

import mn.jwt.sample.entity.User;

@Controller
public class HomeController {

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Produces(MediaType.APPLICATION_JSON)
    @Get
    public User index(Principal principal) {
        return new User(principal.getName());
    }

    @Secured(SecurityRule.IS_ANONYMOUS) // 不特定多数がアクセス可能
    @Produces(MediaType.TEXT_PLAIN)
    @Get("/anonymous")
    public String anonymous() {
        return "anonymous";
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/url-map-anonymous")
    public String urlMapAnonymous() {
        return "url map anonymous";
    }

}
