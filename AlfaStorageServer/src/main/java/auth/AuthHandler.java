package auth;

import java.sql.*;

public class AuthHandler {
    private Connection con;
    public final String USER_TABLE = "users";
    public final String LOGIN = "login";
    public final String PASS = "pass";

    public AuthHandler() {
        try {
            String createDB = "CREATE TABLE IF NOT EXISTS " + USER_TABLE + "\n" +
                    "(\n" +
                    LOGIN + " TEXT NOT NULL,\n" +
                    PASS + " TEXT NOT NULL,\n" +
                    "UNIQUE(" + LOGIN + ", " + PASS + ", " + ")\n" +
                    ");";
            PreparedStatement prSt = null;
            prSt = getConnection().prepareStatement(createDB);
            prSt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            String connectionString = "jdbc:sqlite:authorization.db";
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public User getOrCreateUser(String login, String password) {
        User user = null;
        try {
            ResultSet rs = null;
            String connect = "SELECT * FROM " + USER_TABLE + " WHERE login = ? AND pass = ?";
            PreparedStatement prSt = getConnection().prepareStatement(connect);
            prSt.setString(1, login);
            prSt.setString(2, password);
            rs = prSt.executeQuery();
            if (rs != null) {
                user = new User(login, password);
                user.setIsAuthorized(true);
            } else {
                String insert = "INSERT OR IGNORE INTO " + USER_TABLE +
                        " (" + LOGIN + ", " + PASS + ", " + ") " +
                        "VALUES(?,?,?)";
                PreparedStatement insertStatement = getConnection().prepareStatement(insert);
                insertStatement.setString(1, login);
                insertStatement.setString(2, password);
                insertStatement.addBatch();
                insertStatement.executeBatch();
                user = new User(login, password);
                user.setIsAuthorized(true);
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}