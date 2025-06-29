package com.example.airlinesmanagement1;

import java.time.LocalDate;

public class Flight {
    private int flightId;
    private String flightNumber;
    private String fromCity;
    private String toCity;
    private LocalDate flightDate;
    private String departureTime;
    private double price;
    private String status; // on-time, delayed, cancelled
    private int capacity; // total seat capacity
    private int bookedSeats; // number of booked seats

    // Constructor for creating new flights
    public Flight(String flightNumber, String fromCity, String toCity, LocalDate flightDate, String departureTime, double price) {
        this.flightNumber = flightNumber;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.flightDate = flightDate;
        this.departureTime = departureTime;
        this.price = price;
        this.status = "on-time"; // default status
        this.capacity = 28; // default capacity (7 rows * 4 seats)
        this.bookedSeats = 0; // default booked seats
    }

    // Constructor for loading from database
    public Flight(int flightId, String flightNumber, String fromCity, String toCity, LocalDate flightDate, String departureTime, double price, String status, int capacity, int bookedSeats) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.flightDate = flightDate;
        this.departureTime = departureTime;
        this.price = price;
        this.status = status;
        this.capacity = capacity;
        this.bookedSeats = bookedSeats;
    }

    // Getters
    public int getFlightId() {
        return flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public int getAvailableSeats() {
        return capacity - bookedSeats;
    }

    public String getLoadFactor() {
        if (capacity == 0) return "0%";
        double percentage = (double) bookedSeats / capacity * 100;
        return String.format("%.1f%%", percentage);
    }

    // Setters for editing
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public void setFlightDate(LocalDate flightDate) {
        this.flightDate = flightDate;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    @Override
    public String toString() {
        return flightNumber + " (" + fromCity + " → " + toCity + ")";
    }
}
