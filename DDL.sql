
CREATE TABLE Hotel (
	hotelID int NOT NULL,
    hotelName varchar(50) NOT NULL,
    location varchar(100) NOT NULL,
    stars int,
    PRIMARY KEY (hotelID)
);

CREATE TABLE RoomType (
    typeID int NOT NULL,
    roomName varchar(50),
    description varchar(200),
    price int NOT NULL CHECK (price>0),
    PRIMARY KEY (typeID)
);

CREATE TABLE Room (
    roomID int AUTO_INCREMENT NOT NULL,
    status ENUM('Available', 'Occupied') NOT NULL DEFAULT "Available",
    hotelID int NOT NULL,
    typeID int NOT NULL,
    PRIMARY KEY (roomID),
    FOREIGN KEY (hotelID) REFERENCES Hotel(hotelID) ON DELETE CASCADE,
    FOREIGN KEY (typeID) REFERENCES RoomType(typeID) ON DELETE CASCADE
);

CREATE TABLE User (
    ssn varchar(11) NOT NULL,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    phone varchar(11) NOT NULL,
    email varchar(50),
    birthDate date NOT NULL,
    role ENUM('Guest', 'Receptionist', 'Administrator', 'Housekeeping') NOT NULL,
    salary int,
    hotelID int,
    FOREIGN KEY (hotelID) REFERENCES Hotel(HotelID),
    PRIMARY KEY (ssn)
);

CREATE TABLE Booking (
    bookingID int AUTO_INCREMENT NOT NULL,
    roomID int,
    guestID varchar(11) NOT NULL,
    startTime date NOT NULL,
    endTime date NOT NULL,
    status ENUM('Pending', 'Confirmed', 'Checked-In', 'Checked-Out') NOT NULL DEFAULT "Pending",
    people int,
    paymentStatus enum("Pending","Completed","Failed") DEFAULT "Pending" NOT NULL,
	paymentMethod enum("Cash","Credit Card","Apple Pay","PayPal") DEFAULT NULL,    
	paymentDate date DEFAULT NULL,
    PRIMARY KEY (bookingID),
    FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE SET NULL,
    FOREIGN KEY (guestID) REFERENCES User(ssn) ON DELETE CASCADE,
    CHECK (endTime > startTime)
);

CREATE TABLE HouseKeeping (
    taskID int AUTO_INCREMENT,
    housekeeperID varchar(11),
    roomID int NOT NULL,
    taskDate DATE NOT NULL,
    status enum("Pending","Completed") DEFAULT "Pending",
    PRIMARY KEY (taskID),
    FOREIGN KEY (housekeeperID) REFERENCES User(ssn) ON DELETE SET NULL,
    FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE CASCADE
);

