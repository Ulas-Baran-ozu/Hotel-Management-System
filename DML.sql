#dummy data insertion
/*
#insert hotel
INSERT INTO Hotel VALUES (1,"Merit Royale","Cyprus",4);
INSERT INTO Hotel VALUES (2,"Hilton","New York",5);

#insert room types
INSERT INTO RoomType VALUES (1,"Luxury Suite","A luxury experience with an amazing view and extreme confort.",300);
INSERT INTO RoomType VALUES (2,"Single Bed", "Suitable for solo travellers.",150);
INSERT INTO RoomType VALUES (3,"Family Room", "Enjoy our family room with one king size bed and two single beds for your vacation.",350);
INSERT INTO RoomType VALUES (4, "Penthouse Suite", "A spacious suite with panoramic views, a private terrace, and luxury amenities.", 500);
INSERT INTO RoomType VALUES (5, "Double Room", "Perfect for couples or friends, with a comfortable queen-sized bed and modern facilities.", 220);
INSERT INTO RoomType VALUES (6, "Standard Room", "A cozy room with all essential amenities for a comfortable stay.", 120);
INSERT INTO RoomType VALUES (7, "Deluxe Room", "A well-furnished room with added comfort and a beautiful city view.", 250);
INSERT INTO RoomType VALUES (8, "Studio Room", "An open-plan room with a kitchenette, ideal for longer stays or those seeking more independence.", 180);

#insert room
INSERT INTO Room (hotelID,typeID) VALUES (1,1);
INSERT INTO Room (hotelID,typeID) VALUES (1,1);
INSERT INTO Room (hotelID,typeID) VALUES (1,1);
INSERT INTO Room (hotelID,typeID) VALUES (1,2);
INSERT INTO Room (hotelID,typeID) VALUES (1,2);
INSERT INTO Room (hotelID,typeID) VALUES (1,3);
INSERT INTO Room (hotelID,typeID) VALUES (1,3);
INSERT INTO Room (hotelID,typeID) VALUES (1,4);
INSERT INTO Room (hotelID,typeID) VALUES (1,5);

#insert admin
INSERT INTO User VALUES ("12345678901","Ulaş","Baran","05559992222","ulas@gmail.com","2004-06-26","Administrator",62000,1);
INSERT INTO User VALUES ("12325678401","Sedna","Yildiz","05559492222","sedna@gmail.com","2004-03-15","Administrator",62000,1);

#insert housekeeping
INSERT INTO User VALUES ("12222678901","Ayşe","Kartal","05553992222","ayse@gmail.com","1989-04-26","HouseKeeping",42000,1);
INSERT INTO User VALUES ("13345678701","Murat","Beşik","05539991112","murat@gmail.com","1984-05-26","HouseKeeping",42000,1);

#insert receptionist
INSERT INTO User VALUES ("99995678701","Zeynel","Piyaz","05539991000","zeynel@gmail.com","1980-05-26","Receptionist",52000,1);

#insert guest
INSERT INTO User VALUES ("00000000001","Mahmut","Başak","05539221000","mahmut@gmail.com","1980-05-26","Guest",NULL,1);
INSERT INTO User VALUES ("00000000002","Selen","Başak","05139221000","selen@gmail.com","1980-05-26","Guest",NULL,1);

#insert booking
CALL InsertBooking('00000000001', '2025-01-03', '2025-01-07', 1, 2, 2);
CALL InsertBooking('00000000001', '2025-04-10', '2025-04-15', 1, 3, 2);
CALL InsertBooking('00000000002', '2025-12-10', '2025-12-15', 1, 1, 2);
CALL InsertBooking('00000000002', '2025-10-10', '2025-10-15', 1, 3, 2);
*/

#GUEST MENU
#Insert a new booking into the Booking table
/*
DELIMITER $$

CREATE PROCEDURE InsertBooking(
    IN guestID VARCHAR(11),
    IN startDate DATE,
    IN endDate DATE,
    IN hotelID INT,
    IN roomTypeID INT,
    IN people INT
)
BEGIN
    DECLARE roomID INT;

    #Check for a suitable room
    SELECT r.roomID
    INTO roomID
    FROM Room r
    JOIN RoomType rt ON r.typeID = rt.typeID
    WHERE 
		#Look for rooms that are available and from selected hotel and room type
        r.status = 'Available'
        AND r.hotelID = hotelID
        AND rt.typeID = roomTypeID
        AND NOT EXISTS (
            SELECT 1
            FROM Booking b
            WHERE 
                b.roomID = r.roomID
                AND b.status != 'Checked-Out'
                AND (
                    startDate BETWEEN b.startTime AND b.endTime OR
                    endDate BETWEEN b.startTime AND b.endTime OR
                    b.startTime BETWEEN startDate AND endDate OR
                    b.endTime BETWEEN startDate AND endDate OR
                    startDate = b.startTime
                )
        )
    ORDER BY r.roomID ASC
    LIMIT 1;

    -- Raise an error if no suitable room is found
    IF roomID IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No suitable room is available for the specified criteria.';
    END IF;

    -- Insert the booking if a room is found
    INSERT INTO Booking (roomID, guestID, startTime, endTime, status, people)
    VALUES (roomID, guestID, startDate, endDate, 'Pending', people);

END$$

DELIMITER ;
*/

/*
CALL InsertBooking(
    '00000000002',  -- Guest's SSN
    '2024-12-10',   -- Start date
    '2024-12-15',   -- End date
    1,              -- Hotel ID
    3,              -- Room Type ID
    2               -- Number of people
);
*/
/*
DELIMITER $$

CREATE TRIGGER prevent_overbooking
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN
    DECLARE room_availability INT;

    -- Check if there are any overlapping bookings for the selected room
    SELECT COUNT(*) INTO room_availability
    FROM Booking b
    WHERE b.roomID = NEW.roomID
      AND (
          (NEW.startTime BETWEEN b.startTime AND b.endTime) OR
          (NEW.endTime BETWEEN b.startTime AND b.endTime) OR
          (b.startTime BETWEEN NEW.startTime AND NEW.endTime) OR
          (b.endTime BETWEEN NEW.startTime AND NEW.endTime)
      )
      AND b.status != 'Checked-Out'; -- Only consider bookings that are not checked out

    -- If an overlap exists, prevent the booking and raise an error
    IF room_availability > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'This room is already booked for the selected dates.';
    END IF;
END$$

DELIMITER ;
*/


#View Available Rooms
/*
SELECT r.roomID, r.status, rt.roomName, rt.price, h.hotelName, h.location
FROM Room r
JOIN RoomType rt ON r.typeID = rt.typeID
JOIN Hotel h ON r.hotelID = h.hotelID
WHERE r.status = 'Available'
  AND r.hotelID = 1            -- Replace with the guest's selected hotelID
  AND rt.typeID = 1            -- Replace with the guest's selected room type ID
ORDER BY r.roomID ASC;
*/

#view my bookings
/*
SELECT b.bookingID, b.roomID, b.startTime, b.endTime, b.status, b.people, rt.roomName, h.hotelName, h.location
FROM Booking b
JOIN Room r ON b.roomID = r.roomID
JOIN RoomType rt ON r.typeID = rt.typeID
JOIN Hotel h ON r.hotelID = h.hotelID
WHERE b.guestID = '00000000002';  -- Replace with the actual guest's SSN
*/

# delete booking BUT controls if you have already checked in or out
/*
DELETE FROM Booking
WHERE bookingID = 1
  AND status NOT IN ('Checked-In', 'Checked-Out');
*/



#ADMIN MENU
#add room(only by admin)
/*
DELIMITER $$

CREATE PROCEDURE AddRoom(
    IN adminSSN VARCHAR(11),
    IN hotelID INT,
    IN typeID INT
)
BEGIN
    DECLARE adminExists INT;

    -- Check if the admin SSN exists in the User table and belongs to the given hotel
    SELECT COUNT(*) INTO adminExists
    FROM User
    WHERE ssn = adminSSN
      AND role = 'Administrator'
      AND hotelID = hotelID;

    -- If the admin does not exist, raise an error
    IF adminExists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'The provided SSN does not belong to an Administrator in the specified hotel.';
    END IF;

    -- If validation passes, insert the new room
    INSERT INTO Room (status, hotelID, typeID)
    VALUES ('Available', hotelID, typeID);
END$$

DELIMITER ;
*/
/*
CALL AddRoom(
    '00012000002', -- Administrator's SSN
    2,             -- Room type ID
    1              -- Hotel ID
);
*/

#delete room
/*
DELIMITER $$

CREATE PROCEDURE DeleteRoom(IN user_ssn VARCHAR(11), IN room_id INT)
BEGIN
    -- Check if the user is an Administrator
    DECLARE user_role ENUM('Guest', 'Receptionist', 'Administrator', 'Housekeeping');
    
    -- Get the role of the user
    SELECT role INTO user_role
    FROM User
    WHERE ssn = user_ssn;
    
    -- If the user is an Administrator, delete the room
    IF user_role = 'Administrator' THEN
        DELETE FROM Room
        WHERE roomID = room_id;
    ELSE
        -- Raise an error if the user is not an Administrator
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Only an administrator can delete a room.';
    END IF;
END$$

DELIMITER ;
*/
/*
CALL DeleteRoom('12345678901', 8);
*/

#manage room status
/*
DELIMITER $$

CREATE PROCEDURE ManageRoomStatus(IN user_ssn VARCHAR(11), IN room_id INT, IN new_status ENUM('Available', 'Occupied'))
BEGIN
    -- Declare a variable to store the user's role
    DECLARE user_role ENUM('Guest', 'Receptionist', 'Administrator', 'Housekeeping');
    
    -- Get the role of the user
    SELECT role INTO user_role
    FROM User
    WHERE ssn = user_ssn;
    
    -- If the user is an Administrator, update the room's status
    IF user_role = 'Administrator' THEN
        UPDATE Room
        SET status = new_status
        WHERE roomID = room_id;
    ELSE
        -- Raise an error if the user is not an Administrator
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Only an administrator can change the room status.';
    END IF;
END$$

DELIMITER ;

CALL ManageRoomStatus('12345678901', 2, 'Occupied');
*/

#add user account
#INSERT INTO User (ssn, firstName, lastName, phone, email, birthDate, role, hotelID)
#VALUES ('11122334455', 'Alice', 'Smith', '9876543210', 'alice@example.com', '1995-05-15', 'Guest', 1);

#view users
/*
SELECT ssn, firstName, lastName, phone, email, birthDate, role, hotelID
FROM User
WHERE role != 'Administrator'  -- Exclude admin users from the list
  AND hotelID = 1;  -- Replace '1' with the logged-in admin's hotelID
*/

#Generate Revenue Report
/*
SELECT 
    H.hotelName AS Hotel,
    H.location AS Location,
    COUNT(B.bookingID) AS TotalBookings,
    SUM(RT.price * DATEDIFF(B.endTime, B.startTime)) AS TotalRevenue
FROM 
    Booking B
JOIN 
    Room R ON B.roomID = R.roomID
JOIN 
    RoomType RT ON R.typeID = RT.typeID
JOIN 
    Hotel H ON R.hotelID = H.hotelID
WHERE 
    B.status IN ('Confirmed', 'Checked-In', 'Checked-Out') -- Only count revenue-generating bookings
    AND B.paymentStatus = 'Completed' -- Only include bookings with completed payments
GROUP BY 
    H.hotelID, H.hotelName, H.location
ORDER BY 
    TotalRevenue DESC;
*/

#view all booking records
#SELECT * FROM Booking;

#view all housekeeping records
#SELECT * FROM HouseKeeping;

#view most booked room types
/*
SELECT 
    rt.typeID,
    rt.roomName AS RoomType,
    COUNT(b.bookingID) AS TotalBookings
FROM 
    Room r
JOIN 
    RoomType rt ON r.typeID = rt.typeID
JOIN 
    Booking b ON r.roomID = b.roomID
WHERE 
    b.status IN ('Confirmed', 'Checked-In', 'Checked-Out') -- Include only valid bookings
GROUP BY 
    rt.typeID, rt.roomName
ORDER BY 
    TotalBookings DESC; -- Show the most booked room types first
*/

#view all the employees with their role
/*
SELECT 
    ssn AS EmployeeID,
    CONCAT(firstName, ' ', lastName) AS FullName,
    role AS Role,
    phone AS ContactNumber,
    email AS Email,
    salary AS Salary,
    hotelID AS HotelID
FROM 
    User
WHERE 
    role IN ('Receptionist', 'Administrator', 'Housekeeping'); -- Ensures only employees are listed
*/

#RECEPTIONIST MENU
#add new booking
#INSERT INTO Booking(roomID,guestID,startTime,endTime,people)
#VALUES (input_room, guest_ssn, startTime, endTime, people)

#modify booking
#UPDATE Booking
#SET bookingID = input_bookingID, roomID = input_roomID, guestID = input_guestID, startTime = input_startTime, 
#    endTime = input_endTime, status = input_status, people = new_people, paymentStatus = input_paymentStatus, 
#    paymentMethod =input_paymentMethod, paymentDate = input_paymentDate;
#WHERE bookingID = selected_bookingID

#confirm booking
#UPDATE Booking
#SET status = "Confirmed"
#WHERE bookingID = 7;

#delete booking
#DELETE FROM Booking WHERE bookingID = selected_bookingID;

#view bookings
#SELECT * FROM Booking;

#process payment
#UPDATE Booking 
#SET paymentStatus = "Completed"
#WHERE bookingID = 7;

#assign housekeeping task
#INSERT HouseKeeping(housekeeperID,roomID,taskDate) VALUES (selected_housekeeperID,selected_roomID,selected_taskDate);

#View All Housekeepers Records and Their Availability
/*SELECT 
    U.ssn AS HousekeeperSSN,
    U.firstName AS FirstName,
    U.lastName AS LastName,
    U.phone AS Phone,
    U.email AS Email,
    U.salary AS Salary,
    U.hotelID AS HotelID,
    CASE 
        WHEN HK.taskID IS NULL OR HK.taskDate != CURDATE() THEN 'Available'
        ELSE 'Assigned'
    END AS Availability
FROM 
    User U
LEFT JOIN 
    HouseKeeping HK ON U.ssn = HK.housekeeperID
WHERE 
    U.role = 'Housekeeping'
ORDER BY 
    U.lastName, U.firstName;
*/


#HOUSEKEEPING MENU
#view pending housekeeping tasks
#SELECT * FROM HouseKeeping WHERE status="Pending";

#view completed housekeeping tasks
#SELECT * FROM HouseKeeping WHERE status="Completed";

#update task status to completed
#UPDATE HouseKeeping
#SET status="Completed"
#WHERE taskID=selected_taskID;

#view my cleaning schedule
#SELECT * FROM Housekeeping WHERE housekeeperID=selected_housekeeperID AND status="Pending";
