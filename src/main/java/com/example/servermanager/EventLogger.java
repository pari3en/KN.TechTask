package com.example.servermanager;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EventLogger {
    private final File eventFile;

    public EventLogger(String filename) {
        this.eventFile = new File(filename);
        try {
            if (!eventFile.exists()) {
                boolean result = eventFile.createNewFile();
                System.out.println("Creation result is " + result + " for file " + filename);
            }
        } catch (IOException e) {
            System.err.print("Error creating event file: " + e.getMessage());
        }
    }

    public synchronized void logEvent(Event event) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(eventFile, true))) {
            writer.write(event.toCSV());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing event: " + e.getMessage());
        }
    }

    public synchronized List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Event event = Event.fromCSV(line);
                if (event != null) {
                    events.add(event);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading events: " + e.getMessage());
        }
        return events;
    }

    public List<Event> filterEvents(String status, LocalDateTime from, LocalDateTime to, boolean asc) {
        List<Event> events = getAllEvents();
        if (status != null) {
            String lowerStatus = status.toLowerCase();
            events = events.stream().filter(e -> e.status().equals(lowerStatus)).collect(Collectors.toList());
        }
        if (from != null) {
            events = events.stream().filter(e -> !e.timestamp().isBefore(from)).collect(Collectors.toList());
        }
        if (to != null) {
            events = events.stream().filter(e -> !e.timestamp().isAfter(to)).collect(Collectors.toList());
        }
        events.sort((e1, e2) -> asc ? e1.timestamp().compareTo(e2.timestamp()) : e2.timestamp().compareTo(e1.timestamp()));
        return events;
    }
}