package auth;

import java.sql.*;

public class ConnectionHandler {
    private static ConnectionHandler instance;
    private final Connection connection;
    private static final String connectionString = "jdbc:sqlite:authorization.db";

    private ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    public static void getConnectionHandler(Connection connection) {
        instance = new ConnectionHandler(connection);
    }

    public static void dbConnection() {
        if (instance == null) {
            try {
                Connection connection = DriverManager.getConnection(connectionString);
                getConnectionHandler(connection);
                System.out.println("Connection was created!");
            } catch (SQLException ex) {
                System.out.println("Connection was not created!");
                ex.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return instance.connection;
    }

    public static void closeConnection() {
        try {
            getConnection().close();
            System.out.println("Connection closed!");
        } catch (Exception ex) {
            System.out.println("Connection was not closed!");
            ex.printStackTrace();
        }
    }
}
