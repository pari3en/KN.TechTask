package com.example.servermanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Event(String status, LocalDateTime timestamp) {
    public Event(String status, LocalDateTime timestamp) {
        this.status = status.toLowerCase();
        this.timestamp = timestamp;
    }

    public String toCSV() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return status + "," + timestamp.format(formatter);
    }

    public static Event fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return new Event(parts[0], LocalDateTime.parse(parts[1], formatter));
    }

    @Override
    public String toString() {
        return "Event{" +
                "status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
