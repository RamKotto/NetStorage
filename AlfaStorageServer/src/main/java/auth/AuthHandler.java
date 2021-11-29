package auth;

import java.sql.*;

public class AuthHandler {
    private static Connection connection;
    private final String USER_TABLE = "users";
    private final String LOGIN = "login";
    private final String PASS = "pass";

    public AuthHandler() {
        try {
            String createDB = "CREATE TABLE IF NOT EXISTS " + USER_TABLE + "\n" +
                    "(\n" +
                    LOGIN + " TEXT NOT NULL,\n" +
                    PASS + " TEXT NOT NULL,\n" +
                    "UNIQUE(" + LOGIN + ", " + PASS + ")\n" +
                    ");";
            executeUpdate(createDB);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            String connectionString = "jdbc:sqlite:authorization.db";
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(connectionString);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public User getOrCreateUser(String login, String password) {
        User user = null;
        try {
            String connect = "SELECT * FROM " + USER_TABLE + " WHERE login = ? AND pass = ?";
            PreparedStatement prSt = getConnection().prepareStatement(connect);
            prSt.setString(1, login);
            prSt.setString(2, password);
            prSt.execute();
            ResultSet resultSet = prSt.getResultSet();
            if (resultSet != null) {
                System.out.println("Getting!!!!");
                user = new User(login, password);
                user.setIsAuthorized(true);
                return user;
            } else  {
                System.out.println("Creating!!!!");
                String insert = "INSERT OR IGNORE INTO " + USER_TABLE +
                        " (" + LOGIN + ", " + PASS + ") " +
                        "VALUES(" + login + "," + password + ")";
                executeUpdate(insert);
                user = new User(login, password);
                user.setIsAuthorized(true);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Wa are here!!");
        return user;
    }

    private void executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        // Для Insert, Update, Delete
        statement.executeUpdate(query);
    }
}