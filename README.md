# ✈️ Airlines Management System

A desktop-based **Airlines Management System** designed to simplify and manage common airline operations such as flight scheduling, passenger management, ticket booking, and flight information management.

The project provides a user-friendly interface for managing airline-related data and demonstrates the use of **Java, JavaFX, and database-driven application development**.

---

## 📌 Features

### 👤 Passenger Management

* Add new passenger information
* Update passenger details
* Search passenger records
* View passenger information
* Manage passenger records

### ✈️ Flight Management

* Add and manage flights
* Update flight information
* Search available flights
* View flight schedules
* Manage source and destination information

### 🎫 Ticket / Booking Management

* Book flight tickets
* Store booking information
* Search booking records
* View passenger and flight details
* Manage ticket information

### 🛫 Airline Operations

* Manage aircraft/flight information
* Track flight schedules
* Manage departure and arrival information
* Maintain airline-related records

### 🔐 System Management

* User authentication/login
* Database-backed record management
* Input validation
* Search and update operations

---

## 🛠️ Technologies Used

| Technology                             | Purpose                      |
| -------------------------------------- | ---------------------------- |
| **Java**                               | Core application development |
| **JavaFX**                             | Desktop GUI                  |
| **FXML**                               | UI structure                 |
| **CSS**                                | UI styling                   |
| **MySQL**                              | Database management          |
| **JDBC**                               | Database connectivity        |
| **IntelliJ IDEA / Eclipse / NetBeans** | Development                  |

> Update the technology list according to the exact technologies used in your implementation.

---

## 🏗️ System Architecture

```text
┌─────────────────────────────┐
│        JavaFX UI            │
│                             │
│  Login │ Flights │ Booking  │
│  Passengers │ Management    │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│      Application Logic      │
│                             │
│ Authentication              │
│ Flight Management           │
│ Passenger Management        │
│ Booking Management          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│       JDBC Layer            │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│          MySQL              │
│                             │
│ Users                       │
│ Passengers                  │
│ Flights                     │
│ Bookings                    │
└─────────────────────────────┘
```

---

## 📂 Project Structure

```text
Airlines-Management-System/
│
├── src/
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── FlightController.java
│   │   ├── PassengerController.java
│   │   └── BookingController.java
│   │
│   ├── model/
│   │   ├── Flight.java
│   │   ├── Passenger.java
│   │   └── Booking.java
│   │
│   ├── database/
│   │   └── DatabaseConnection.java
│   │
│   └── Main.java
│
├── resources/
│   ├── fxml/
│   ├── css/
│   └── images/
│
├── database/
│   └── airline.sql
│
├── README.md
└── pom.xml
```

> Adjust the structure to match your actual repository.

---

## 🗄️ Database

The system uses a relational database to store and manage airline information.

Example entities include:

```text
Users
  │
  └── Authentication

Passengers
  │
  └── Passenger Information

Flights
  │
  ├── Flight Number
  ├── Source
  ├── Destination
  ├── Departure
  └── Arrival

Bookings
  │
  ├── Passenger
  ├── Flight
  ├── Ticket
  └── Booking Information
```

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
```

### 2. Open the project

Open the project using your preferred Java IDE.

Recommended:

* IntelliJ IDEA
* Eclipse
* NetBeans

### 3. Configure Java

Make sure Java and JavaFX are installed/configured according to the project's requirements.

Check your Java version:

```bash
java -version
```

### 4. Configure MySQL

Create a MySQL database:

```sql
CREATE DATABASE airlines_management;
```

Import the provided SQL file if your repository contains one:

```text
database/airline.sql
```

### 5. Configure database credentials

Update the database connection configuration with your local credentials:

```java
String url = "jdbc:mysql://localhost:3306/airlines_management";
String username = "root";
String password = "your_password";
```

### 6. Run the application

Run:

```text
Main.java
```

or use the IDE's Run option.

---

## 🖥️ Application Workflow

```text
Login
  │
  ▼
Dashboard
  │
  ├── Passenger Management
  │
  ├── Flight Management
  │
  ├── Ticket Booking
  │
  └── Flight Information
```

---

## 🎯 Project Objectives

The main objectives of this project are to:

* Develop a functional airline management application
* Automate basic airline-related operations
* Manage passenger and flight records efficiently
* Implement database-driven CRUD operations
* Provide a graphical desktop interface
* Demonstrate object-oriented programming concepts
* Practice Java database connectivity
* Understand the design of a real-world management system

---

## 🔑 Key Concepts Demonstrated

* Object-Oriented Programming
* MVC-style application structure
* JavaFX GUI development
* Event-driven programming
* CRUD operations
* Relational database design
* JDBC
* SQL queries
* Input validation
* Authentication
* Software project organization

---

## 📸 Screenshots

Add screenshots of the application here.

Example:

```text
screenshots/
├── login.png
├── dashboard.png
├── flights.png
├── passengers.png
└── booking.png
```

### Login

*Add screenshot here.*

### Dashboard

*Add screenshot here.*

### Flight Management

*Add screenshot here.*

### Passenger Management

*Add screenshot here.*

### Booking

*Add screenshot here.*

---

## 🔮 Future Improvements

Possible future improvements include:

* Online flight booking
* Payment gateway integration
* Email/SMS booking notifications
* PDF ticket generation
* Seat selection
* Advanced flight search and filtering
* Role-based access control
* Admin analytics dashboard
* Flight status tracking
* REST API integration
* Cloud database support
* Mobile/web version

---

## 👨‍💻 Developer

**Abdullah Firoj**

Computer Science & Engineering
United International University

### Connect

* GitHub: `https://github.com/Afiroj313501`
* LinkedIn: `https://www.linkedin.com/in/abdullah-firoj-900697375/`

---

## 📄 License

This project was developed for educational and portfolio purposes.

If you plan to reuse or modify the project, please provide appropriate attribution.
