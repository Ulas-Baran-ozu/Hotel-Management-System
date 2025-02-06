import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        //Connecting to database
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connection to the database established!\n");

            //Creating a menu object and starting the terminal
            TerminalMenu menu = new TerminalMenu(conn);
            menu.hotelSelectionMenu();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}