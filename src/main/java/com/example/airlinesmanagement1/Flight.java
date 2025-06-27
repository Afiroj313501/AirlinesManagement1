package com.example.airlinesmanagement1;

import java.time.LocalDate;

public class Flight {
    private final String flightNumber;
    private final String fromCity;
    private final String toCity;
    private final LocalDate flightDate;
    private final String departureTime;
    private final double price;

    public Flight(String flightNumber, String fromCity, String toCity, LocalDate flightDate, String departureTime, double price) {
        this.flightNumber = flightNumber;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.flightDate = flightDate;
        this.departureTime = departureTime;
        this.price = price;
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
}
