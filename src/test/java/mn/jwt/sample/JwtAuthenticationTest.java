package mn.jwt.sample;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import org.junit.jupiter.api.Assertions;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class JwtAuthenticationTest {
    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    public void authorization_fails_401() {
        // 未認証のため401エラー
        try {
            client.toBlocking().exchange(HttpRequest.GET("/"));
        } catch (HttpClientResponseException e) {
            Assertions.assertEquals(e.getStatus(),  HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void authorization_succeeds_200() {
        String username = "sherlock";

        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, "password");
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();

        Assertions.assertEquals(rsp.getStatus(),  HttpStatus.OK);
        Assertions.assertEquals(bearerAccessRefreshToken.getUsername(),  username);

        // ログイン後に取得したアクセストークンで認証
        String accessToken = bearerAccessRefreshToken.getAccessToken();
        HttpRequest requestWithAuthorization = HttpRequest.GET("/")
                .accept(MediaType.TEXT_PLAIN)
                .bearerAuth(accessToken);
        HttpResponse<String> response = client.toBlocking().exchange(requestWithAuthorization, String.class);

        Assertions.assertEquals(response.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(response.body(), username);
    }
}
