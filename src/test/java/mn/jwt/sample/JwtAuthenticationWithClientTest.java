package mn.jwt.sample;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import mn.jwt.sample.client.AppClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class JwtAuthenticationWithClientTest {

    @Inject
    AppClient client;

    @Test
    public void authorization_fails_401() {
        // 未認証のため401エラー
        try {
            client.home("");
        } catch (HttpClientResponseException e) {
            Assertions.assertEquals(e.getStatus(),  HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    public void authorization_succeeds_200() {
        String username = "sherlock";

        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, "password");
        BearerAccessRefreshToken loginRsp = client.login(creds);
        String accessToken = loginRsp.getAccessToken();

        Assertions.assertEquals(loginRsp.getUsername(),  username);

        // ログイン後に取得したアクセストークンで認証
        String msg = client.home("Bearer " + accessToken);

        Assertions.assertEquals(msg, username);
    }
}
