package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Arrays;
import java.sql.Statement;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Scheduler {
    private static final Logger logger = Logger.getLogger(Scheduler.class.getName());

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }


    private static void createPatient(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Create patient failed");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        try {
            Patient existingPatient = new Patient.PatientGetter(username).get();
            if (existingPatient != null) {
                System.out.println("Username taken, try again");
                return;
            }

            Patient newPatient = new Patient.PatientBuilder(username, password).build();
            newPatient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Create patient failed");
            // logger.log(Level.SEVERE, "SQL Exception in createPatient", e);
        }
    }
    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }


    private static void loginPatient(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Login patient failed");
            return;
        }

        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in, try again");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        try {
            Patient patient = new Patient.PatientGetter(username).get();
            if (patient == null) {
                System.out.println("Login patient failed");

                return;
            }

            byte[] salt = patient.getSalt();
            byte[] hash = Util.generateHash(password, salt);



            if (Arrays.equals(hash, patient.getHash())) {
                currentPatient = patient;
                System.out.println("Logged in as " + username);
            } else {
                System.out.println("Login patient failed");
                // logger.log(Level.INFO, "Hash mismatch for user: {0}", username);
            }
        } catch (SQLException e) {
            System.out.println("Login patient failed");
            // logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
    }
    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Please try again");
            return;
        }

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        String date = tokens[1];

        try {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            // Fetch available caregivers
            String queryCaregivers = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
            PreparedStatement stmtCaregivers = con.prepareStatement(queryCaregivers);
            stmtCaregivers.setDate(1, Date.valueOf(date));
            ResultSet rsCaregivers = stmtCaregivers.executeQuery();

            while (rsCaregivers.next()) {
                System.out.println(rsCaregivers.getString("Username"));
            }

            // Fetch available vaccines
            String queryVaccines = "SELECT Name, Doses FROM Vaccines ORDER BY Name";
            PreparedStatement stmtVaccines = con.prepareStatement(queryVaccines);
            ResultSet rsVaccines = stmtVaccines.executeQuery();

            while (rsVaccines.next()) {
                System.out.println(rsVaccines.getString("Name") + " " + rsVaccines.getInt("Doses"));
            }

            cm.closeConnection();
        } catch (SQLException e) {
            System.out.println("Please try again");
            logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
    }
    private static void reserve(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Please try again");
            return;
        }

        if (currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        String date = tokens[1];
        String vaccine = tokens[2];

        ConnectionManager cm = new ConnectionManager();
        Connection con = null;

        try {
            con = cm.createConnection();

            // Check if there are available caregivers for the specified date
            String queryCaregiver = "SELECT TOP 1 Username FROM Availabilities WHERE Time = ? ORDER BY Username";
            PreparedStatement psCaregiver = con.prepareStatement(queryCaregiver);
            psCaregiver.setDate(1, Date.valueOf(date));
            ResultSet rsCaregiver = psCaregiver.executeQuery();
            if (!rsCaregiver.next()) {
                System.out.println("No caregiver is available");
                return;
            }
            String caregiverUsername = rsCaregiver.getString("Username");

            // Check if there are enough vaccine doses available
            String queryVaccine = "SELECT Doses FROM Vaccines WHERE Name = ?";
            PreparedStatement psVaccine = con.prepareStatement(queryVaccine);
            psVaccine.setString(1, vaccine);
            ResultSet rsVaccine = psVaccine.executeQuery();
            if (!rsVaccine.next() || rsVaccine.getInt("Doses") < 1) {
                System.out.println("Not enough available doses");
                return;
            }

            // Insert appointment
            String insertAppointment = "INSERT INTO Appointments (date, caregiver_username, vaccine_name, patient_username) VALUES (?, ?, ?, ?)";
            PreparedStatement psInsert = con.prepareStatement(insertAppointment, Statement.RETURN_GENERATED_KEYS);
            psInsert.setDate(1, Date.valueOf(date));
            psInsert.setString(2, caregiverUsername);
            psInsert.setString(3, vaccine);
            psInsert.setString(4, currentPatient.getUsername());
            psInsert.executeUpdate();

            // Get the generated appointment ID
            ResultSet rsInsert = psInsert.getGeneratedKeys();
            if (rsInsert.next()) {
                int appointmentId = rsInsert.getInt(1);
                System.out.println("Appointment ID " + appointmentId + ", Caregiver username " + caregiverUsername);
            }

            // Decrease the number of available vaccine doses
            String updateVaccine = "UPDATE Vaccines SET Doses = Doses - 1 WHERE Name = ?";
            PreparedStatement psUpdate = con.prepareStatement(updateVaccine);
            psUpdate.setString(1, vaccine);
            psUpdate.executeUpdate();

            // Remove caregiver availability
            String deleteAvailability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";
            PreparedStatement psDelete = con.prepareStatement(deleteAvailability);
            psDelete.setDate(1, Date.valueOf(date));
            psDelete.setString(2, caregiverUsername);
            psDelete.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Please try again");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, "SQL Exception in reserve", e);
        } finally {
            if (con != null) {
                cm.closeConnection();
            }
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
            }
        }
        System.out.println("Doses updated successfully!");
    }

    private static void showAppointments(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        try {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String query;
            if (currentCaregiver != null) {
                query = "SELECT appointment_id, vaccine_name, date, patient_username FROM Appointments WHERE caregiver_username = ? ORDER BY appointment_id";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, currentCaregiver.getUsername());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    System.out.println(rs.getInt("appointment_id") + " " + rs.getString("vaccine_name") + " " + rs.getDate("date") + " " + rs.getString("patient_username"));
                }
            } else {
                query = "SELECT appointment_id, vaccine_name, date, caregiver_username FROM Appointments WHERE patient_username = ? ORDER BY appointment_id";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, currentPatient.getUsername());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    System.out.println(rs.getInt("appointment_id") + " " + rs.getString("vaccine_name") + " " + rs.getDate("date") + " " + rs.getString("caregiver_username"));
                }
            }

            cm.closeConnection();
        } catch (SQLException e) {
            System.out.println("Please try again");
            logger.log(Level.SEVERE, "SQL Exception in loginPatient", e);
        }
    }


    private static void logout(String[] tokens) {
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first");
            return;
        }

        currentCaregiver = null;
        currentPatient = null;
        System.out.println("Successfully logged out");
    }

}
