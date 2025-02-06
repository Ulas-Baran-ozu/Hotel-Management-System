import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class TerminalMenu {
    String[] roles = {"Guest", "Receptionist", "Housekeeping", "Administrator"};
    Scanner input = new Scanner(System.in);
    private int userChoice;
    private int hotelChoice;
    String role;
    Connection conn;
    UserDAO user;

    //Constructor to initialize the connection
    public TerminalMenu(Connection conn) {
        this.conn = conn;
    }

    public void hotelSelectionMenu() throws ParseException, SQLException {
        //This is the first menu to appear
        System.out.println("        WELCOME TO THE HOTEL MANAGEMENT SYSTEM\n");
        System.out.println("Select hotel:");

        //Hotels are fetched from database and user is asked to choose
        String query = "SELECT hotelName, hotelID FROM Hotel";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String hotelName = rs.getString("hotelName");
                    int hotelID = rs.getInt("hotelID");

                    System.out.printf("%s-%s%n", hotelID, hotelName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching hotel data: " + e.getMessage());
        }
        hotelChoice = input.nextInt();
        //After choosing a hotel, user is sent to userSelectionMenu
        userSelectionMenu();
    }

    public void userSelectionMenu() throws ParseException, SQLException {
        //This is the second menu that asks the user type
        System.out.println("Select user type:");
        System.out.println("1-Guest");
        System.out.println("2-Receptionist");
        System.out.println("3-Housekeeping");
        System.out.println("4-Administrator");
        userChoice = input.nextInt();

        //Role is determined
        role = roles[userChoice - 1];
        if (userChoice != 1 && userChoice != 2 && userChoice != 3 && userChoice != 4) {
            System.out.println("Please enter a valid choice!");
            userSelectionMenu();
        }
        loginMenu(userChoice);
    }


    public void loginMenu(int userChoice) throws ParseException, SQLException {
        //After user select the user type they are
        // asked if they want to log in or create an account
        int logsignChoice;
        System.out.printf("     %s Menu ",role);
        System.out.println();
        System.out.println("Select what you want to do:");
        System.out.println("1-Login");
        if(userChoice != 4) {
            //If the user type is admin they cant create an account
            System.out.println("2-Create an account");
        }
        logsignChoice = input.nextInt(); // Log-in/Sign_in choice
        input.nextLine(); //Consume the line

        //login account
        if (logsignChoice == 1) {
            //User object is created from ssn
            String ssn;
            System.out.println("Please enter your SSN to login: ");
            ssn = input.nextLine();
            user = new UserDAO(ssn, conn);
        }

        //create an account
        if (logsignChoice == 2) {
            //New user inserted into the database and user object created
            insertUser(userChoice);
        }

        //Users are navigated to chosen menu
        switch (userChoice) {
            case (1):
                guestMenu();
                break;
            case(2):
                receptionistMenu();
                break;
            case(3):
                housekeepingMenu();
                break;
            case(4):
                administratorMenu();
        }

    }
    public void insertUser(int userChoice) throws ParseException {
        String ssn;
        String firstName;
        String lastName;
        String phone;
        String email;
        java.sql.Date birthDate;
        int salary = 0;

        //Get the inputs
        System.out.println("Please enter your information:");
        System.out.println("SSN: ");
        ssn = input.nextLine();
        System.out.println("First Name: ");
        firstName = input.nextLine();
        System.out.println("Last Name: ");
        lastName = input.nextLine();
        System.out.println("Phone Number: ");
        phone = input.nextLine();
        System.out.println("Email: ");
        email = input.nextLine();
        System.out.println("Birth Date(MM-DD-YYYY): ");
        String tempDate = input.nextLine();
        //If user is guest, then salary is not important
        if(userChoice != 1) {
            System.out.println("Salary:");
            salary = input.nextInt();
        }

        //Parse the date
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
        java.util.Date date = sdf1.parse(tempDate);
        birthDate = new java.sql.Date(date.getTime());

        String sql;
        if(userChoice ==1) { //User is guest
            user = new UserDAO(ssn, firstName, lastName, phone, email, birthDate, this.role, 0, this.hotelChoice);
            sql = "INSERT INTO User (ssn, firstName, lastName, phone, email, birthDate, role, salary, hotelID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NULL , ?)";
        }
        else { //User is not guest
            user = new UserDAO(ssn, firstName, lastName, phone, email, birthDate, this.role, salary, this.hotelChoice);
            sql = "INSERT INTO User (ssn, firstName, lastName, phone, email, birthDate, role, salary, hotelID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ? , ?)";
        }


        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set values into the query
            stmt.setString(1, ssn);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.setDate(6, birthDate);
            stmt.setString(7, this.role);
            if(userChoice==1) //If guest
                stmt.setInt(8, this.hotelChoice);
            else{ //If not guest
                stmt.setInt(8,salary);
                stmt.setInt(9,this.hotelChoice);
            }

            // Execute the insert operation
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User added successfully!");
            } else {
                System.out.println("Failed to add user.");
            }
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }

    public void guestMenu() throws SQLException, ParseException {
        int choice; //operation choice
        String choice2; //back or quit choice
        boolean running = true;

        while (running) {
            System.out.println("      WELCOME");
            System.out.println("What would you like to do?");
            System.out.println("1-View available rooms");
            System.out.println("2-Add Booking");
            System.out.println("3-Delete Booking");
            System.out.println("4-View my bookings");
            System.out.println("5-Check In");
            System.out.println("6-Check Out");
            System.out.println("0-Quit");

            // Safely read user input
            try {
                choice = input.nextInt();
                input.nextLine(); // Clear the buffer
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                input.nextLine(); //Consume the line
                continue;
            }

            switch (choice) {
                case 1:
                    user.viewAvailableRooms(hotelChoice);
                    break;
                case 2:
                    user.addBooking();
                    break;
                case 3:
                    user.deleteBooking();
                    break;
                case 4:
                    user.viewBookingOfUser();
                    break;
                case 5:
                    user.checkIn();
                    break;
                case 6:
                    user.checkOut();
                    break;
                case 0:
                    //0 means quit so break out the while loop
                    System.out.println("Exiting guest menu. Goodbye!");
                    running = false; // Exit the loop
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }

            // After each operation, ask user to go back or quit
            if (running && choice != 0) { // Don't prompt if quitting
                System.out.println("Type 'b' to go back to the menu or 'q' to quit.");
                choice2 = input.nextLine();

                if (choice2.equals("q")) {
                    System.out.println("Exiting guest menu. Goodbye!");
                    running = false; // Exit the menu loop
                }
            }
        }
    }

    public void receptionistMenu() throws SQLException, ParseException {
        int choice; //operation choice
        String choice2; //back or quit choice
        boolean running = true;

        while(running){
            System.out.println("      WELCOME");
            System.out.println("What would you like to do?");
            System.out.println("1-Add new booking");
            System.out.println("2-Modify booking");
            System.out.println("3-Confirm Booking");
            System.out.println("4-Delete booking");
            System.out.println("5-View bookings");
            System.out.println("6-Process payment");
            System.out.println("7-Assign housekeeping task");
            System.out.println("8-View all housekeepers records and their availability");
            System.out.println("0-Quit");

            // Safely read user input
            try {
                choice = input.nextInt();
                input.nextLine(); // Clear the buffer
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                input.nextLine(); //Consume the line
                continue;
            }

            switch (choice){
                case(1):
                    user.addBooking();
                    break;
                case(2):
                    user.modifyBooking();
                    break;
                case(3):
                    user.confirmBooking();
                    break;
                case(4):
                    user.deleteBooking();
                    break;
                case(5):
                    user.viewBookingOfHotel();
                    break;
                case(6):
                    user.processPayment();
                    break;
                case(7):
                    user.assignHousekeepingTask();
                    break;
                case(8):
                    user.viewAllHousekeepers();
                    break;
                case(0):
                    //0 means quit so break out the while loop
                    System.out.println("Exiting receptionist menu. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }

            if (running && choice != 0) { // Don't prompt if quitting
                System.out.println("Type 'b' to go back to the menu or 'q' to quit.");
                choice2 = input.nextLine();

                if (choice2.equals("q")) {
                    System.out.println("Exiting receptionist menu. Goodbye!");
                    running = false; // Exit the menu loop
                }
            }
        }
    }
    public void housekeepingMenu(){
        int choice; //operation choice
        String choice2; //back or quit choice
        boolean running = true;

        while(running){
            System.out.println("      WELCOME");
            System.out.println("What would you like to do?");
            System.out.println("1-View Pending Housekeeping Tasks");
            System.out.println("2-View Completed Housekeeping Tasks");
            System.out.println("3-Update Task Status to Completed");
            System.out.println("4-View My Cleaning Schedule");
            System.out.println("0-Exit");

            // Safely read user input
            try {
                choice = input.nextInt();
                input.nextLine(); // Clear the buffer
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                input.nextLine(); //Consume the line
                continue;
            }

            switch (choice) {
                case (1):
                    user.viewPendingTasks();
                    break;
                case (2):
                    user.viewCompletedTasks();
                    break;
                case (3):
                    user.updateTaskStatusToCompleted();
                    break;
                case (4):
                    user.viewCleaningSchedule();
                    break;
                case (0):
                    //0 means quit so break out the while loop
                    System.out.println("Exiting housekeeping menu. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
            if (running && choice != 0) { // Don't prompt if quitting
                System.out.println("Type 'b' to go back to the menu or 'q' to quit.");
                choice2 = input.nextLine();

                if (choice2.equals("q")) {
                    System.out.println("Exiting housekeeping menu. Goodbye!");
                    running = false; // Exit the menu loop
                }
            }
        }
    }
    public void administratorMenu() {
        int choice; //operation choice
        String choice2; //back or quit choice
        boolean running = true;

        while (running) {
            System.out.println("      WELCOME");
            System.out.println("What would you like to do?");
            System.out.println("1-Add Room");
            System.out.println("2-Delete Room");
            System.out.println("3-Manage Room Status");
            System.out.println("4-Generate Revenue Report");
            System.out.println("5-Add User Account");
            System.out.println("6-View User Accounts");
            System.out.println("7-View All Booking Records");
            System.out.println("8-View All Housekeeping Records");
            System.out.println("9-View Most Booked Room Types");
            System.out.println("10-View All Employees By Their Role");
            System.out.println("11-View Occupancy Rate");
            System.out.println("0-Exit");

            // Safely read user input
            try {
                choice = input.nextInt();
                input.nextLine(); // Clear the buffer
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                input.nextLine(); //Consume next line
                continue;
            }

            switch (choice) {
                case (1):
                    user.addRoom(hotelChoice);
                    break;
                case (2):
                    user.deleteRoom(hotelChoice);
                    break;
                case (3):
                    user.manageRoomStatus(hotelChoice);
                    break;
                case (4):
                    user.generateRevenueReport(hotelChoice);
                    break;
                case(5):
                    user.addUserAccount();
                    break;
                case(6):
                    user.viewUserAccounts(hotelChoice);
                    break;
                case(7):
                    user.viewAllBookings(hotelChoice);
                    break;
                case(8):
                    user.viewAllHousekeepingRecords(hotelChoice);
                    break;
                case(9):
                    user.viewMostBookedRoomTypes(hotelChoice);
                    break;
                case(10):
                    user.viewAllEmployeesWithRole(hotelChoice);
                    break;
                case(11):
                    user.viewOccupancyRate(hotelChoice);
                    break;
                case (0):
                    //0 means quit so break out the while loop
                    System.out.println("Exiting administrator menu. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }

            if (running && choice != 0) { // Don't prompt if quitting
                System.out.println("Type 'b' to go back to the menu or 'q' to quit.");
                choice2 = input.nextLine();

                if (choice2.equals("q")) {
                    System.out.println("Exiting administrator menu. Goodbye!");
                    running = false; // Exit the menu loop
                }
            }

        }
    }
}