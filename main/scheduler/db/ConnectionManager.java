package scheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private final String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final String connectionUrl = "jdbc:sqlserver://" + System.getProperty("Server") + 
            ":1433;database=" + System.getProperty("DBName") + ";encrypt=true;trustServerCertificate=false;";
    private final String userName = System.getProperty("UserID");
    private final String userPass = System.getProperty("Password");

    private Connection con = null;

    public ConnectionManager() {
        try {
            System.out.println("Server: " + System.getProperty("Server"));
            System.out.println("DBName: " + System.getProperty("DBName"));
            System.out.println("UserID: " + System.getProperty("UserID"));
            System.out.println("Password: " + System.getProperty("Password"));
            System.out.println(System.getProperty("java.class.path"));


            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            System.out.println(e.toString());
        }
    }

    public Connection createConnection() {
        try {
            con = DriverManager.getConnection(connectionUrl, userName, userPass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public void closeConnection() {
        try {
            this.con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
