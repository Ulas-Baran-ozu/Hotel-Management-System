import java.sql.Connection;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//This is a Data Access Object. There will be methods inside this that are used by users.
public class UserDAO {
    private String ssn;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private final java.sql.Date birthDate;
    private String role;
    private int salary;
    private int hotelID;
    Connection conn;
    Scanner input = new Scanner(System.in);

    //Constructor with all the attributes
    public UserDAO(String ssn, String firstName, String lastName, String phone, String email, Date birthDate, String role, int salary, int hotelID) {
        this.ssn = ssn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.role = role;
        this.salary = salary;
        this.hotelID = hotelID;
    }

    //Constructor that fetches the other attributes from database with the given ssn
    public UserDAO(String inputSsn, Connection conn) {
        this.conn = conn;

        // SQL query to retrieve user information based on SSN
        String sql = "SELECT ssn, firstName, lastName, phone, email, birthDate, role, salary, hotelID " +
                "FROM User WHERE ssn = ?";


        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //Set the SSN parameter
            pstmt.setString(1, inputSsn);

            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
                // Check if a user was found
                if (rs.next()) {
                    // Populate the current object's fields with fetched data
                    this.ssn = rs.getString("ssn");
                    this.firstName = rs.getString("firstName");
                    this.lastName = rs.getString("lastName");
                    this.phone = rs.getString("phone");
                    this.email = rs.getString("email");
                    this.birthDate = rs.getDate("birthDate");
                    this.role = rs.getString("role");
                    this.salary = rs.getInt("salary");
                    this.hotelID = rs.getInt("hotelID");
                } else {
                    // Throw an exception if no user is found with the given SSN
                    throw new UserNotFoundException("No user found with SSN: " + inputSsn);
                }
            }
        } catch (SQLException e) {
            // Log the error and rethrow or handle as appropriate
            System.err.println("Database error occurred: " + e.getMessage());
            throw new RuntimeException("Error retrieving user information", e);
        }
    }

    // Custom exception for when no user is found
    static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public void addBooking() throws SQLException, ParseException {
        viewRoomTypeByHotel(); // Display available room types for the hotel

        // Inputs
        int typeId;
        String startDatestr;
        String endDatestr;
        java.sql.Date startDate;
        java.sql.Date endDate;
        int people;

        System.out.println("Please enter ID of the room type you want to book:");
        typeId = input.nextInt();
        input.nextLine(); //Consume the buffer

        System.out.println("Please enter the start date of your visit (MM-DD-YYYY):");
        startDatestr = input.nextLine();

        System.out.println("Please enter the end date of your visit (MM-DD-YYYY):");
        endDatestr = input.nextLine();

        System.out.println("Please enter how many people you are booking for:");
        people = input.nextInt();

        // Parse Dates
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");

        java.util.Date startUtilDate = sdf1.parse(startDatestr);
        startDate = new java.sql.Date(startUtilDate.getTime());

        java.util.Date endUtilDate = sdf1.parse(endDatestr);
        endDate = new java.sql.Date(endUtilDate.getTime());

        // SQL query
        String query = """
        CALL InsertBooking(
            ?,  -- Guest's SSN
            ?,  -- Start date
            ?,  -- End date
            ?,  -- Hotel ID
            ?,  -- Room Type ID
            ?   -- Number of people
        );
    """;

        // Execute query
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1, ssn);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            stmt.setInt(4, hotelID);
            stmt.setInt(5, typeId);
            stmt.setInt(6, people);

            //Check if the query executed correctly
            int rowsAffected = stmt.executeUpdate(); //executeUpdate() method returns number of affected rows
            if (rowsAffected > 0) {
                System.out.println("Booking added successfully!");
            } else {
                System.out.println("Failed to add booking.");
            }

        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
            throw e;
        }
    }

    public void viewAvailableRooms(int hotelID){
        //SQL query that shows all room information of a given hotel
        String query = "SELECT r.roomID, r.status, rt.roomName, rt.price, h.hotelName, h.location\n" +
                "FROM Room r\n" +
                "JOIN RoomType rt ON r.typeID = rt.typeID\n" +
                "JOIN Hotel h ON r.hotelID = h.hotelID\n" +
                "WHERE r.status = 'Available'\n" +
                "  AND r.hotelID = ?            -- Replace with the guest's selected hotelID\n" +
                "ORDER BY r.roomID ASC;";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter to Hotel ID
            stmt.setInt(1, hotelID);
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Here are the available rooms:");
                System.out.println("  Room Name | Price | Hotel | Location | Status");
                //Get all rows
                while (rs.next()) {
                    //Create and assign attributes to print to terminal
                    int roomID = rs.getInt("roomId");
                    String status = rs.getString("status");
                    String roomName = rs.getString("roomName");
                    int price = rs.getInt("price");
                    String hotelName = rs.getString("hotelName");
                    String location = rs.getString("location");

                    //Print the information
                    System.out.printf("%s- %s %s %s %s %s%n",roomID,roomName,price,hotelName,location,status);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user accounts: " + e.getMessage());
        }
    }

    public void viewRoomTypeByHotel(){
        String sql = """
        SELECT DISTINCT rt.typeID, rt.roomName, rt.description, rt.price
        FROM RoomType rt
        JOIN Room r ON rt.typeID = r.typeID
        WHERE r.hotelID = ?
        ORDER BY rt.typeID ASC
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //Set the parameter to Hotel ID
            stmt.setInt(1, hotelID);
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Room Types for Hotel ID: " + hotelID);
                System.out.println("-------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-10s %-20s %-30s %-10s\n", "TypeID", "Room Name", "Description", "                               Price");
                System.out.println("-------------------------------------------------------------------------------------------------------------");

                //This variable is set to false and assigned
                // to true if at least one row is returned
                boolean hasRoomTypes = false;
                //Get all rows
                while (rs.next()) {
                    hasRoomTypes = true;
                    //Create and assign attributes to print to terminal
                    int typeID = rs.getInt("typeID");
                    String roomName = rs.getString("roomName");
                    String description = rs.getString("description");
                    int price = rs.getInt("price");

                    System.out.printf("%-10d %-20s %-30s %-10d\n", typeID, roomName, description, price);
                }

                //Executes if no rows returned and hasRoomTypes stayed false
                if (!hasRoomTypes) {
                    System.out.println("No room types available for the specified hotel.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving room types: " + e.getMessage());
        }
    }

    public void viewBookingOfUser(){
        String query = "SELECT bookingID, roomId, startTime, endTime, status, people FROM Booking WHERE guestId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter to ssn
            stmt.setString(1, ssn);
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Your Bookings:");
                //Get all rows
                while (rs.next()) {
                    //Create and assign attributes to print to terminal
                    int bookingId = rs.getInt("bookingID");
                    int roomId = rs.getInt("roomId");
                    java.sql.Date startTime = rs.getDate("startTime");
                    java.sql.Date endTime = rs.getDate("endTime");
                    String status = rs.getString("status");
                    int people = rs.getInt("people");

                    System.out.printf("Booking ID: %s   Room ID: %s   Start Date: %s   End Date: %s   Status: %s   People: %s%n",bookingId,roomId,startTime,endTime,status,people);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user bookings: " + e.getMessage());
        }
    }

    public void deleteBooking() throws SQLException {
        int deletionChoice; //Booking id of booking to be deleted
        this.viewBookingOfUser(); //Show the bookings of user so user knows the booking ids

        System.out.println("Please enter the booking id of the booking you want to delete:");
        deletionChoice = input.nextInt();

        String query = "DELETE FROM Booking WHERE bookingID = ? ;";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, deletionChoice);

            //Execute the query and check
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Booking deleted successfully!");
            } else {
                System.out.println("Failed to delete booking.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            throw e;
        }
    }

    public void modifyBooking() throws ParseException {
        int choiceBooking;
        int choice; //what type of modifying is being done

        System.out.println("Please enter the booking id of the booking you want to modify:");
        choiceBooking = input.nextInt();

        System.out.println("What do you want to modify?");
        System.out.println("1-Change room of the booking");
        System.out.println("2-Change the guest of the booking");
        System.out.println("3-Change the time of the booking");
        choice = input.nextInt();

        switch (choice){
            case(1):
                //Change the room of the booking
                int newRoomId;
                System.out.println("Please enter the new room id:");
                newRoomId = input.nextInt();
                String query1 = "UPDATE Booking SET roomID = ? WHERE bookingID = ?";

                //Prepare statement
                try (PreparedStatement stmt = conn.prepareStatement(query1)) {
                    stmt.setInt(1, newRoomId); // Set new roomID
                    stmt.setInt(2, choiceBooking); // Set bookingID

                    // Execute update and check
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Booking updated successfully! Room ID changed to: " + newRoomId);
                    } else {
                        System.out.println("Failed to update booking. Make sure the Booking ID exists.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            case(2):
                //Change the guest of the booking
                input.nextLine(); //consume the next line
                String newGuestSsn;
                System.out.println("Please enter the new guest ssn:");
                newGuestSsn = input.nextLine();

                String query2 = "UPDATE Booking SET guestID = ? WHERE bookingID = ?";

                // Prepare statement
                try (PreparedStatement stmt = conn.prepareStatement(query2)) {
                    stmt.setString(1, newGuestSsn);
                    stmt.setInt(2, choiceBooking);

                    // Execute update and check
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Booking updated successfully! Guest ID changed to: " + newGuestSsn);
                    } else {
                        System.out.println("Failed to update booking. Make sure the SSN exists.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            case(3):
                //Change the time of the booking
                //Inputs
                String newStartDateStr;
                String newEndDateStr;
                java.sql.Date startDate;
                java.sql.Date endDate;
                input.nextLine(); //Consume the next line

                //Get the inputs
                System.out.println("Please enter the new start date(MM-dd-yyyy):");
                newStartDateStr = input.nextLine();
                System.out.println("Please enter the new end date(MM-dd-yyyy):");
                newEndDateStr = input.nextLine();

                //Parse dates
                SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
                java.util.Date date = sdf1.parse(newStartDateStr);
                startDate = new java.sql.Date(date.getTime());
                java.util.Date date1 = sdf1.parse(newEndDateStr);
                endDate = new java.sql.Date(date1.getTime());

                String query3 = "UPDATE Booking SET startTime = ?, endTime = ? WHERE bookingID = ?";

                // Prepare statement
                try (PreparedStatement stmt = conn.prepareStatement(query3)) {
                    stmt.setDate(1, startDate);
                    stmt.setDate(2, endDate);
                    stmt.setInt(3,choiceBooking);

                    // Execute update and check
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Dates updated successfully!");
                    } else {
                        System.out.println("Failed to update dates.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    public void confirmBooking(){
        //Get the input
        int bookingId;
        System.out.println("Please enter the booking id of booking you want to confirm:");
        bookingId = input.nextInt();

        String query = "UPDATE Booking SET status = 'Confirmed' WHERE bookingID = ?";

        //Prepare statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1,bookingId);

            // Execute update and check
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Booking confirmed successfully!");
            } else {
                System.out.println("Failed to confirm booking.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void viewBookingOfHotel() {
        String sql = "SELECT b.bookingID, b.roomID, b.guestID, b.startTime, b.endTime, b.status, b.people " +
                "FROM Booking b " +
                "JOIN Room r ON b.roomID = r.roomID " +
                "WHERE r.hotelID = ?";

        // Prepare statement
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hotelID); // Set the hotel ID in the query

            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                // Check if any results were returned
                if (!rs.isBeforeFirst()) { //isBeforeFirst returns false if the result set contains no rows
                    System.out.println("No bookings found for this hotel.");
                    return;
                }
                // Print header
                System.out.printf("%-10s %-10s %-15s %-15s %-15s %-10s %-10s\n", "BookingID", "RoomID", "GuestID", "StartTime", "EndTime", "Status", "People");
                System.out.println("---------------------------------------------------------------------------------------");

                // Iterate through the result set and display bookings
                while (rs.next()) {
                    //Create and assign attributes to print to terminal
                    int bookingID = rs.getInt("bookingID");
                    int roomID = rs.getInt("roomID");
                    String guestID = rs.getString("guestID");
                    String startTime = rs.getString("startTime");
                    String endTime = rs.getString("endTime");
                    String status = rs.getString("status");
                    int people = rs.getInt("people");

                    System.out.printf("%-10d %-10d %-15s %-15s %-15s %-10s %-10d\n",
                            bookingID, roomID, guestID, startTime, endTime, status, people);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void processPayment(){
        String[] paymentMethods = {"Cash", "Credit Card", "Apple Pay", "PayPal"};

        //Inputs
        int bookingId;
        String paymentMethod;
        int choice;

        System.out.println("Please enter the booking of the payment to be made:");
        bookingId = input.nextInt();

        System.out.println("Please choose the payment method:");
        System.out.println("1-Cash");
        System.out.println("2-Credit Card");
        System.out.println("3-Apple Pay");
        System.out.println("4-PayPal");
        choice = input.nextInt();
        paymentMethod = paymentMethods[choice-1];

        //Get the current date
        java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());

        String query = "UPDATE Booking SET paymentStatus = 'Completed' , paymentMethod = ? , paymentDate = ? WHERE bookingID = ?";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1,paymentMethod);
            stmt.setDate(2,date);
            stmt.setInt(3,bookingId);

            // Execute update and check
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Payment processed successfully!");
            } else {
                System.out.println("Failed to process payment.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignHousekeepingTask() throws ParseException {
        //Inputs
        String workerId;
        int roomId;
        String dateStr;
        java.sql.Date dateSql;

        System.out.println("Please enter the SSN of the Housekeeper you want to assign:");
        workerId = input.nextLine();

        System.out.println("Please enter the room id you want to assign to:");
        roomId = input.nextInt();
        input.nextLine(); //Consume the next line

        System.out.println("Please enter the date you want to assign(MM-dd-yyyy):");
        dateStr = input.nextLine();

        //Parse the date
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
        java.util.Date date = sdf1.parse(dateStr);
        dateSql = new java.sql.Date(date.getTime());

        String query = "INSERT HouseKeeping(housekeeperID,roomID,taskDate) VALUES ( ? , ? , ? );";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1,workerId);
            stmt.setInt(2,roomId);
            stmt.setDate(3,dateSql);

            // Execute update and check
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Housekeeping task assigned successfully!");
            } else {
                System.out.println("Failed to assign housekeeping task.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void viewAllHousekeepers() {
        // SQL Query to check tasks for the current day
        String query = "SELECT u.ssn, u.firstName, u.lastName, u.phone, u.email, " +
                "CASE WHEN EXISTS ( " +
                "    SELECT 1 FROM HouseKeeping h " +
                "    WHERE h.housekeeperID = u.ssn AND h.taskDate = CURDATE() " +
                ") THEN 'Unavailable' ELSE 'Available' END AS availability " +
                "FROM User u " +
                "WHERE u.role = 'Housekeeping'";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Execute the query
            ResultSet rs = stmt.executeQuery();

            System.out.println("Housekeeper Records (Today):");
            System.out.printf("%-15s %-15s %-15s %-15s %-25s %-15s\n",
                    "SSN", "First Name", "Last Name", "Phone", "Email", "Availability");
            System.out.println("-------------------------------------------------------------------------------------------");

            //Get all rows
            while (rs.next()) {
                //Create and assign attributes to print to terminal
                String ssn = rs.getString("ssn");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String availability = rs.getString("availability");

                System.out.printf("%-15s %-15s %-15s %-15s %-25s %-15s\n",
                        ssn, firstName, lastName, phone, email, availability);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching housekeeper records: " + e.getMessage());
        }
    }

    public void addRoom(int hotelID) {
        System.out.println("\nAdd Room:");

        int typeID;
        viewRoomTypeByHotel(); //Show admin the room types to choose from
        System.out.print("Enter Room Type ID: ");
        typeID = input.nextInt();
        input.nextLine(); //Consume the line


        String query = "INSERT INTO Room (hotelID, typeID, status) VALUES (?, ?, 'Available')";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setInt(1, hotelID);
            stmt.setInt(2, typeID);

            //Execute query and check
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Room added successfully!");
            } else {
                System.out.println("Failed to add room.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
        }
    }

    public void deleteRoom(int hotelID) {
        System.out.println("\nDelete Room:");

        int roomID;
        System.out.print("Enter Room ID to delete: ");
        roomID = input.nextInt();
        input.nextLine();

        //Check query returns number of active bookings with the given room
        String checkQuery = "SELECT COUNT(*) AS activeBookings FROM Booking WHERE roomID = ? AND status != 'Checked-Out'";
        String deleteQuery = "DELETE FROM Room WHERE roomID = ? AND hotelID = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            //Set the parameter
            checkStmt.setInt(1, roomID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt("activeBookings") > 0) {
                    System.out.println("Cannot delete room with active bookings or pending reservations.");
                    return;
                }
            }

            //Set the parameters
            deleteStmt.setInt(1, roomID);
            deleteStmt.setInt(2, hotelID);

            //Execute query and check
            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Room deleted successfully!");
            } else {
                System.out.println("Room not found or does not belong to this hotel.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
        }
    }

    public void manageRoomStatus(int hotelID) {
        System.out.println("\nManage Room Status:");

        int roomId;
        String newStatus;

        System.out.print("Enter Room ID: ");
        roomId = input.nextInt();
        input.nextLine();

        System.out.print("Enter New Status (Available, Occupied): ");
        newStatus = input.nextLine();

        String query = "UPDATE Room SET status = ? WHERE roomID = ? AND hotelID = ?";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1, newStatus);
            stmt.setInt(2, roomId);
            stmt.setInt(3, hotelID);

            //Execute the query and check
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Room status updated successfully!");
            } else {
                System.out.println("Room not found or does not belong to this hotel.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating room status: " + e.getMessage());
        }
    }

    public void generateRevenueReport(int hotelID) {
        System.out.println("\nGenerating Revenue Report...");
        String query = """
                SELECT h.hotelName, COUNT(b.bookingID) AS TotalBookings,
                       SUM(rt.price * DATEDIFF(b.endTime, b.startTime)) AS TotalRevenue
                FROM Booking b
                JOIN Room r ON b.roomID = r.roomID
                JOIN RoomType rt ON r.typeID = rt.typeID
                JOIN Hotel h ON r.hotelID = h.hotelID
                WHERE r.hotelID = ? AND b.paymentStatus = 'Completed'
                GROUP BY h.hotelName;
                """;

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the hotel id paremeter
            stmt.setInt(1, hotelID);
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("Hotel: %s | Total Bookings: %d | Total Revenue: %.2f%n",
                            rs.getString("hotelName"), rs.getInt("TotalBookings"), rs.getDouble("TotalRevenue"));
                } else {
                    System.out.println("No revenue data found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating revenue report: " + e.getMessage());
        }
    }

    public void addUserAccount() {
        System.out.println("\nAdd User Account:");
        System.out.println("Enter SSN: ");
        String ssn = input.next();

        System.out.println("Enter First Name: ");
        String firstName = input.next();

        System.out.println("Enter Last Name: ");
        String lastName = input.next();

        System.out.println("Enter Phone: ");
        String phone = input.next();

        System.out.println("Enter Email: ");
        String email = input.next();

        System.out.println("Enter Birth Date (YYYY-MM-DD): ");
        String birthDate = input.next();

        System.out.println("Enter Role (Guest, Receptionist, Housekeeping, Administrator): ");
        String role = input.next();

        //Salary is equal to 0 if user is guest
        int salary = 0;

        //If the user is not guest, salary info is retrieved
        if (!role.equalsIgnoreCase("Guest")) {
            System.out.println("Enter Salary: ");
            salary = input.nextInt();
        }

        String query = "INSERT INTO User (ssn, firstName, lastName, phone, email, birthDate, role, salary, hotelID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1, ssn);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, phone);
            stmt.setString(5, email);
            stmt.setString(6, birthDate);
            stmt.setString(7, role);
            stmt.setInt(8, salary);
            stmt.setInt(9, hotelID);

            //Execute the query and check
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User account added successfully!");
            } else {
                System.out.println("Failed to add user account.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding user account: " + e.getMessage());
        }
    }

    public void viewUserAccounts(int hotelID) {
        System.out.println("\nUser Accounts:");
        String query = "SELECT ssn, firstName, lastName, role FROM User WHERE hotelID = ?";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelID);
            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("SSN: %s | Name: %s %s | Role: %s%n",
                            rs.getString("ssn"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing user accounts: " + e.getMessage());
        }
    }
    public void viewAllBookings(int hotelChoice) {
        System.out.println("\nView All Booking Records for Hotel ID: " + hotelChoice);

        String query = """
    SELECT b.bookingID, b.roomID, b.startTime, b.endTime, b.status, b.people
    FROM Booking b
    JOIN Room r ON b.roomID = r.roomID
    WHERE r.hotelID = ?
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelChoice);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-15s %-15s %-10s %-10s\n",
                        "BookingID", "RoomID", "Start Date", "End Date", "Status", "People");

                boolean hasResults = false; //This variable is initially set to zero and if number
                                            //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf("%-10d %-10d %-15s %-15s %-10s %-10d\n",
                            rs.getInt("bookingID"),
                            rs.getInt("roomID"),
                            rs.getDate("startTime"),
                            rs.getDate("endTime"),
                            rs.getString("status"),
                            rs.getInt("people"));
                }
                if (!hasResults) {
                    System.out.println("No bookings found for this hotel.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing bookings: " + e.getMessage());
        }
    }

    public void viewAllHousekeepingRecords(int hotelChoice) {
        System.out.println("\nView All Housekeeping Records for Hotel ID: " + hotelChoice);

        String query = """
    SELECT h.taskID, h.housekeeperID, h.roomID, h.taskDate, h.status
    FROM HouseKeeping h
    JOIN Room r ON h.roomID = r.roomID
    WHERE r.hotelID = ?
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelChoice);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-15s %-10s %-15s %-10s\n",
                        "TaskID", "Housekeeper SSN", "RoomID", "Task Date", "Status");

                boolean hasResults = false; //This variable is initially set to zero and if number
                                            //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf("%-10d %-15s %-10d %-15s %-10s\n",
                            rs.getInt("taskID"),
                            rs.getString("housekeeperID"),
                            rs.getInt("roomID"),
                            rs.getDate("taskDate"),
                            rs.getString("status"));
                }
                if (!hasResults) {
                    System.out.println("No housekeeping records found for this hotel.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing housekeeping records: " + e.getMessage());
        }
    }

    public void viewMostBookedRoomTypes(int hotelChoice) {
        System.out.println("\nView Most Booked Room Types for Hotel ID: " + hotelChoice);

        String query = """
    SELECT rt.roomName, COUNT(b.bookingID) AS TotalBookings
    FROM RoomType rt
    JOIN Room r ON rt.typeID = r.typeID
    JOIN Booking b ON r.roomID = b.roomID
    WHERE r.hotelID = ?
    GROUP BY rt.roomName
    ORDER BY TotalBookings DESC
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelChoice);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-20s %-10s\n", "Room Name", "Bookings");

                boolean hasResults = false;  //This variable is initially set to zero and if number
                                             // of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf("%-20s %-10d\n",
                            rs.getString("roomName"),
                            rs.getInt("TotalBookings"));
                }
                if (!hasResults) {
                    System.out.println("No room booking data found for this hotel.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing most booked room types: " + e.getMessage());
        }
    }

    public void viewAllEmployeesWithRole(int hotelChoice) {
        System.out.println("\nView All Employees with Their Roles for Hotel ID: " + hotelChoice);

        String query = """
    SELECT ssn, firstName, lastName, role, phone, email, salary
    FROM User
    WHERE hotelID = ? AND role != 'Guest'
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelChoice);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s %-10s\n",
                        "SSN", "First Name", "Last Name", "Role", "Phone", "Email", "Salary");

                boolean hasResults = false; //This variable is initially set to zero and if number
                                            //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasResults = true;
                    System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s %-10d\n",
                            rs.getString("ssn"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("role"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getInt("salary"));
                }
                if (!hasResults) {
                    System.out.println("No employees found for this hotel.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing employees: " + e.getMessage());
        }
    }


    //extra: Allows administrators to see the percentage of occupied rooms compared to available rooms in the hotel
    public void viewOccupancyRate(int hotelID) {
        String query = """
    SELECT 
        (SUM(CASE WHEN status = 'Occupied' THEN 1 ELSE 0 END) / COUNT(*)) * 100 AS OccupancyRate
    FROM Room
    WHERE hotelID = ?
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setInt(1, hotelID);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double rate = rs.getDouble("OccupancyRate");
                    System.out.printf("Current Occupancy Rate for Hotel ID %d: %.2f%%\n", hotelID, rate);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating occupancy rate: " + e.getMessage());
        }
    }

    public void viewPendingTasks() {
        System.out.println("Pending Housekeeping Tasks:");

        String query = """
    SELECT taskID, roomID, taskDate, status
    FROM HouseKeeping
    WHERE housekeeperID = ? AND status = 'Pending'
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameter
            stmt.setString(1, ssn);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-15s %-10s\n", "Task ID", "Room ID", "Task Date", "Status");
                boolean hasTasks = false; //This variable is initially set to zero and if number
                                          //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasTasks = true;
                    System.out.printf("%-10d %-10d %-15s %-10s\n",
                            rs.getInt("taskID"),
                            rs.getInt("roomID"),
                            rs.getDate("taskDate"),
                            rs.getString("status"));
                }
                if (!hasTasks) {
                    System.out.println("No pending tasks found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending tasks: " + e.getMessage());
        }
    }

    public void viewCompletedTasks() {
        System.out.println("Completed Housekeeping Tasks:");

        String query = """
    SELECT taskID, roomID, taskDate, status
    FROM HouseKeeping
    WHERE housekeeperID = ? AND status = 'Completed'
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the paramter
            stmt.setString(1, ssn);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-15s %-10s\n", "Task ID", "Room ID", "Task Date", "Status");
                boolean hasTasks = false; //This variable is initially set to zero and if number
                                          //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasTasks = true;
                    System.out.printf("%-10d %-10d %-15s %-10s\n",
                            rs.getInt("taskID"),
                            rs.getInt("roomID"),
                            rs.getDate("taskDate"),
                            rs.getString("status"));
                }
                if (!hasTasks) {
                    System.out.println("No completed tasks found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching completed tasks: " + e.getMessage());
        }
    }

    public void updateTaskStatusToCompleted() {
        System.out.println("Update Task Status to Completed:");
        System.out.print("Enter Task ID: ");
        int taskID = input.nextInt();

        String query = """
    UPDATE HouseKeeping
    SET status = 'Completed'
    WHERE taskID = ? AND housekeeperID = ? AND status = 'Pending'
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setInt(1, taskID);
            stmt.setString(2, ssn);

            //Execute query and check
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Task status updated to 'Completed' successfully!");
            } else {
                System.out.println("Task not found, already completed, or does not belong to you.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
        }
    }

    public void viewCleaningSchedule() {
        System.out.println("Your Cleaning Schedule:");

        String query = """
    SELECT taskID, roomID, taskDate, status
    FROM HouseKeeping
    WHERE housekeeperID = ?
    ORDER BY taskDate ASC
""";

        //Prepare the statement
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //Set the parameters
            stmt.setString(1, ssn);

            //Execute the query
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-10s %-10s %-15s %-10s\n", "Task ID", "Room ID", "Task Date", "Status");
                boolean hasTasks = false; //This variable is initially set to zero and if number
                                          //of returned rows are at lest one it gets assigned to true
                while (rs.next()) {
                    hasTasks = true;
                    System.out.printf("%-10d %-10d %-15s %-10s\n",
                            rs.getInt("taskID"),
                            rs.getInt("roomID"),
                            rs.getDate("taskDate"),
                            rs.getString("status"));
                }
                if (!hasTasks) {
                    System.out.println("No tasks assigned to you.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching your cleaning schedule: " + e.getMessage());
        }
    }

    public void checkIn() throws SQLException {
        System.out.println("Enter your Booking ID to check-in:");
        int bookingID = input.nextInt();
        input.nextLine(); // Consume the line

        //Check if booking exist and confirmed
        String checkQuery = "SELECT status FROM Booking WHERE bookingID = ?";

        //Prepare the statement
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            //Set the parameter
            checkStmt.setInt(1, bookingID);

            //Execute the query
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");

                    // Check if booking is confirmed
                    if (!"Confirmed".equalsIgnoreCase(status)) {
                        System.out.println("Booking is not confirmed. Cannot proceed with check-in.");
                        return;
                    }

                    // Check if already checked in
                    if ("Checked-In".equalsIgnoreCase(status.trim())) {
                        System.out.println("You have already checked in.");
                        return;
                    }

                    //Update check-in status
                    String updateQuery = "UPDATE Booking SET status = 'Checked-In' WHERE bookingID = ?";
                    //Prepare the statement
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        //Set the parameter
                        updateStmt.setInt(1, bookingID);

                        //Execute the query and check
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Check-in successful! Enjoy your stay.");
                        } else {
                            System.out.println("Failed to update check-in status. Please try again.");
                        }
                    }
                } else {
                    System.out.println("No booking found with the provided ID.");
                }
            }
        }

    }

    public void checkOut() throws SQLException {
        System.out.println("Enter your Booking ID to check-out:");
        int bookingID = input.nextInt();
        input.nextLine(); // Consume leftover newline

        //Checked if payment is completed, already checked-in or checked-out
        String checkQuery = "SELECT paymentStatus, status FROM Booking WHERE bookingID = ?";

        //Prepare the statement
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            //Set the parameters
            checkStmt.setInt(1, bookingID);

            //Execute the query
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String paymentStatus = rs.getString("paymentStatus");
                    String status = rs.getString("status");

                    //Check if the user is checked in
                    if (!"Checked-In".equalsIgnoreCase(status)) {
                        System.out.println("You cannot check out because you have not checked in yet.");
                        return;
                    }

                    //Check if payment is completed
                    if (!"Completed".equalsIgnoreCase(paymentStatus)) {
                        System.out.println("Payment is not completed. Please complete payment before checking out.");
                        return;
                    }

                    //Check if already checked out
                    if ("Checked-Out".equalsIgnoreCase(status.trim())) {
                        System.out.println("You have already checked out.");
                        return;
                    }

                    //Update check-out status
                    String updateQuery = "UPDATE Booking SET status = 'Checked-Out' WHERE bookingID = ?";

                    //Prepare the statement
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        //Set the parameter
                        updateStmt.setInt(1, bookingID);

                        //Execute the query and check
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Check-out successful! Thank you for staying with us.");
                        } else {
                            System.out.println("Failed to update check-out status. Please try again.");
                        }
                    }
                } else {
                    System.out.println("No booking found with the provided ID.");
                }
            }
        }
    }
}