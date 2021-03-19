package mn.jwt.sample;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import mn.jwt.sample.entity.User;
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
        HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();

        Assertions.assertEquals(rsp.getStatus(),  HttpStatus.OK);
        Assertions.assertEquals(bearerAccessRefreshToken.getUsername(),  username);

        // ログイン後に取得したアクセストークンで認証
        String accessToken = bearerAccessRefreshToken.getAccessToken();
        HttpRequest<Object> requestWithAuthorization = HttpRequest.GET("/")
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(accessToken);
        HttpResponse<User> response = client.toBlocking().exchange(requestWithAuthorization, User.class);

        Assertions.assertEquals(response.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(response.body().getUsername(), username);
    }

    @Test
    public void generate_refresh_token() {
        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "password");
        HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/login", creds);
        BearerAccessRefreshToken rsp = client.toBlocking().retrieve(request, BearerAccessRefreshToken.class);

        // リフレッシュトークンを取得
        String refreshToken = rsp.getRefreshToken();

        // リフレッシュトークンが存在することを確認
        Assertions.assertNotNull(refreshToken);
    }

    @Test
    public void generate_access_refresh_token() {
        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "password");
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken>  rsp1 = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        // ログイン後に取得したアクセストークン
        String preAccessToken = rsp1.body().getAccessToken();

        // 1秒(1万ミリ秒)間だけ処理を止める
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // アクセストークンをリフレッシュするリクエストを出すためのトークン
        String refreshToken = rsp1.body().getRefreshToken();

        // アクセストークンをリフレッシュするリクエスト
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(rsp1.body().getRefreshToken());

        // /oauth/access_token へアクセストークンをリフレッシュするリクエストをラップしたPOSTリクエスト
        HttpRequest<TokenRefreshRequest> postTokenRefreshRequest = HttpRequest.POST("/oauth/access_token",
                tokenRefreshRequest);

        // postTokenRefreshRequest を実行
        HttpResponse<AccessRefreshToken> rsp2 = client.toBlocking().exchange(postTokenRefreshRequest, AccessRefreshToken.class);

        // リフレッシュされたアクセストークンを取得
        String curAccessToken = rsp2.body().getAccessToken();

        // アクセストークンが変更されていることを確認
        Assertions.assertNotEquals(curAccessToken, preAccessToken);
    }

    @Test
    public void authorization_succeeds_200_with_access_refresh_token() {
        // ユーザー名
        String username = "sherlock";

        // ログイン
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, "password");
        HttpRequest request = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken>  rsp1 = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);

        // 1秒(1万ミリ秒)間だけ処理を止める
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // アクセストークンをリフレッシュするリクエストを出すためのトークン
        String refreshToken = rsp1.body().getRefreshToken();

        // アクセストークンをリフレッシュするリクエスト
        TokenRefreshRequest tokenRefreshRequest = new TokenRefreshRequest(rsp1.body().getRefreshToken());

        // /oauth/access_token へアクセストークンをリフレッシュするリクエストをラップしたPOSTリクエスト
        HttpRequest<TokenRefreshRequest> postTokenRefreshRequest = HttpRequest.POST("/oauth/access_token",
                tokenRefreshRequest);

        // postTokenRefreshRequest を実行
        HttpResponse<AccessRefreshToken> rsp2 = client.toBlocking().exchange(postTokenRefreshRequest, AccessRefreshToken.class);

        // リフレッシュされたアクセストークンを取得
        String accessRefreshToken = rsp2.body().getAccessToken();

        // リフレッシュされたアクセストークンで認証
        HttpRequest<Object> requestWithAuthorization = HttpRequest.GET("/")
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(accessRefreshToken);
        HttpResponse<User> response = client.toBlocking().exchange(requestWithAuthorization, User.class);

        Assertions.assertEquals(response.getStatus(), HttpStatus.OK);
        Assertions.assertEquals(response.body().getUsername(), username);
    }
}
