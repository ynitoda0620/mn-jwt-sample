package mn.jwt.sample;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.http.HttpResponse;
import mn.jwt.sample.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

@MicronautTest
public class UnsignedRefreshTokenTest {
    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    RefreshTokenRepository refreshTokenRepository;

    @Test
    public void Accessing_a_secured_URL_without_authenticating_returns_unauthorized() {
        String username = "sherlock";

        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, "password");
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken>  rsp1 = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
        String preToken = rsp1.body().getAccessToken();

        try {
            Thread.sleep(1000); // 10秒(1万ミリ秒)間だけ処理を止める
        } catch (InterruptedException e) {
        }

        HttpResponse<AccessRefreshToken> rsp2 = client.toBlocking().exchange(HttpRequest.POST("/oauth/access_token",
                new TokenRefreshRequest(rsp1.body().getAccessToken())), AccessRefreshToken.class);

        String curToken = rsp2.body().getAccessToken();

        Assertions.assertNotEquals(curToken, preToken);
    }
}
