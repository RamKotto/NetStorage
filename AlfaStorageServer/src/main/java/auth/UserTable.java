package auth;

import models.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static auth.ConnectionHandler.getConnection;
import static java.lang.String.format;

public class UserTable {
    private static final String USER_TABLE = "users";
    private static final String LOGIN = "login";
    private static final String PASS = "pass";
    private static final String CREATE_USER = "INSERT INTO users (login, pass) " +
            "VALUES ('%s', '%s');";
    private static final String GET_USER_LIST = "select * from users where login = '%s' and " +
            "pass = '%s';";

    public static void createUser(String login, String password) {
        try {
            getConnection().setAutoCommit(false);
            Statement stmt = getConnection().createStatement();
            stmt.execute(format(CREATE_USER, login, password));
            getConnection().commit();
            System.out.println("User entity with name: " + login + " created!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("User entity with name: " + login + " was not created!");
            try {
                getConnection().rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Can't getConnection().rollback() in createUser method.");
                rollbackEx.printStackTrace();
            }
        }
    }

    public static List<User> getUserList(String login, String password) {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(format(GET_USER_LIST, login, password));
            while (resultSet.next()) {
                String userLogin = resultSet.getString("login");
                String userPass = resultSet.getString("pass");
                User user = new User(userLogin, userPass);
                user.setIsAuthorized(true);
                users.add(user);
            }
            System.out.println("getUserList with params " + login + " " + password + " method completed!");
            return users;
        } catch (SQLException ex) {
            System.out.println("getUserList was not completed!");
            ex.printStackTrace();
            return null;
        }
    }

    public static void createUserTableIfNotExists() {
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

    private static void executeUpdate(String query) throws SQLException {
        Statement statement = getConnection().createStatement();
        // Для Insert, Update, Delete
        statement.executeUpdate(query);
    }
}

