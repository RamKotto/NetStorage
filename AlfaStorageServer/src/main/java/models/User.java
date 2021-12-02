package models;

public class User {
    private int id;
    private String login;
    private String password;
    private boolean isAuthorized;

    public User() {
        this.isAuthorized = false;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.isAuthorized = false;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isIsAuthorized() {
        return isAuthorized;
    }

    public void setIsAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }
}
