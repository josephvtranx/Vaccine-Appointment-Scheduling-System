package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Patient {
    private static final Logger logger = Logger.getLogger(Patient.class.getName());

    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Patient(PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Patient(PatientGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String insertPatient = "INSERT INTO Patient (Username, Salt, Hash) VALUES (?, ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(insertPatient);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            // logger.log(Level.SEVERE, "SQL Exception in saveToDB", e);
            throw new SQLException(e);
        } finally {
            cm.closeConnection();
        }
    }

    public static class PatientBuilder {
        private final String username;
        private byte[] salt;
        private byte[] hash;

        public PatientBuilder(String username, String password) {
            this.username = username;
            this.salt = Util.generateSalt();
            this.hash = Util.generateHash(password, salt);
        }

        public Patient build() {
            return new Patient(this);
        }
    }

    public static class PatientGetter {
        private final String username;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username) {
            this.username = username;
        }

        public Patient get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getPatient = "SELECT Username, Salt, Hash FROM Patient WHERE Username = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getPatient);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    this.salt = resultSet.getBytes("Salt");
                    this.hash = resultSet.getBytes("Hash");

                    return new Patient(this);
                }
                return null;
            } catch (SQLException e) {

                throw new SQLException(e);
            } finally {
                cm.closeConnection();
            }
        }
    }
}

