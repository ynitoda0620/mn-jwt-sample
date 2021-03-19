package mn.jwt.sample.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Controller
public class HomeController {

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public String index(Principal principal) {
        return principal.getName();
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
