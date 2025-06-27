package com.example.airlinesmanagement1;

import java.time.LocalDate;

public class Ticket {
    private String flightId;
    private String seat;
    private LocalDate bookingDate;
    private double price;

    public Ticket(String flightId, String seat, LocalDate bookingDate, double price) {
        this.flightId = flightId;
        this.seat = seat;
        this.bookingDate = bookingDate;
        this.price = price;
    }

    public String getFlightId() {
        return flightId;
    }

    public String getSeat() {
        return seat;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public double getPrice() {
        return price;
    }
}