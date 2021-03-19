package mn.jwt.sample.entity;

public class User {
    private String username;

    // client.toBlocking().exchange(requestWithAuthorization, User.class); でリクエスト時に、
    // Userクラスのデフォルトコンストラクタがないとエラーになる
    public User() {}

    public  User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
