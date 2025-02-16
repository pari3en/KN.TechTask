package com.example.servermanager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerManager {
    private static final String EVENT_FILE = "events.log";
    private static final String STATUS_UP = "up";
    private static final String STATUS_DOWN = "down";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_STARTING = "starting";
    private static final String STATUS_STOPPING = "stopping";
    private static final int MIN_PAUSE_MS = 3000;
    private static final int ADDITIONAL_PAUSE_RANGE = 7000;
    private static final String DATE_TIME_PATTERN = "T00:00:00";
    private static final String DATE_TIME_PATTERN_END = "T23:59:59";
    private static final String SORT_DESC = "desc";
    private final Random random;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected EventLogger eventLogger;

    public ServerManager() {
        eventLogger = new EventLogger(EVENT_FILE);
        random = new Random();
    }

    public void status() {
        List<Event> events = eventLogger.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        Event lastEvent = events.getLast();
        System.out.println("Last event: " + lastEvent.status() + " at " + lastEvent.timestamp());
        if (STATUS_UP.equalsIgnoreCase(lastEvent.status())) {
            // Calculate uptime from the last "up" event to now
            LocalDateTime upTime = lastEvent.timestamp();
            Duration uptime = Duration.between(upTime, LocalDateTime.now());
            long hours = uptime.toHours();
            long minutes = uptime.toMinutes() % 60;
            long seconds = uptime.getSeconds() % 60;
            System.out.printf("Uptime: %02d:%02d:%02d%n", hours, minutes, seconds);
        }
    }

    public void up(String before) {
        // Check if the server is already up
        List<Event> events = eventLogger.getAllEvents();
        if (!events.isEmpty()) {
            Event lastEvent = events.getLast();
            if (STATUS_UP.equalsIgnoreCase(lastEvent.status())) {
                System.out.println("Already up");
                return;
            }
        }

        // If the --before parameter is provided, schedule auto shutdown
        if (before != null) {
            try {
                LocalDateTime shutdownTime = LocalDateTime.parse(before);
                long delay = Duration.between(LocalDateTime.now(), shutdownTime).toSeconds();
                
                if (delay <= 0) {
                    System.out.println("Shutdown time must be in the future");
                    return;
                }

                scheduler.schedule(() -> {
                    System.out.println("Executing scheduled shutdown...");
                    down();
                }, delay, TimeUnit.SECONDS);
                
                System.out.println("Auto shutdown scheduled at " + shutdownTime);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format for --before. Use yyyy-MM-ddThh:mm");
                return;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Event startingEvent = new Event(STATUS_STARTING, now);
        eventLogger.logEvent(startingEvent);
        System.out.println("Starting…");

        // Simulate random pause (3-10 seconds)
        randomPause();

        // Randomly decide if server goes up or fails to start
        String result = random.nextBoolean() ? STATUS_UP : STATUS_FAILED;
        Event resultEvent = new Event(result, LocalDateTime.now());
        eventLogger.logEvent(resultEvent);
        if (STATUS_UP.equalsIgnoreCase(result)) {
            System.out.println("Started");
        } else {
            System.out.println("Failed");
        }
    }

    public void down() {
        // Check if the server is already down
        List<Event> events = eventLogger.getAllEvents();
        if (!events.isEmpty()) {
            Event lastEvent = events.getLast();
            if (STATUS_DOWN.equalsIgnoreCase(lastEvent.status())) {
                System.out.println("Already down");
                return;
            }
        }
        // Log "stopping" event
        LocalDateTime now = LocalDateTime.now();
        Event stoppingEvent = new Event(STATUS_STOPPING, now);
        eventLogger.logEvent(stoppingEvent);
        System.out.println("Stopping…");

        // Simulate random pause (3-10 seconds)
        randomPause();

        // Randomly decide if server goes down or fails to stop
        String result = random.nextBoolean() ? STATUS_DOWN : STATUS_FAILED;
        Event resultEvent = new Event(result, LocalDateTime.now());
        eventLogger.logEvent(resultEvent);
        if (STATUS_DOWN.equalsIgnoreCase(result)) {
            shutdown();
            System.out.println("Stopped");
        } else {
            System.out.println("Failed");
        }
    }

    public void history(String fromStr, String toStr, String sort, String statusFilter) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            if (fromStr != null) {
                from = LocalDateTime.parse(fromStr + DATE_TIME_PATTERN);
            }
            if (toStr != null) {
                to = LocalDateTime.parse(toStr + DATE_TIME_PATTERN_END);
            }
        } catch (Exception e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return;
        }
        boolean asc = !SORT_DESC.equalsIgnoreCase(sort);
        List<Event> filtered = eventLogger.filterEvents(statusFilter, from, to, asc);
        if (filtered.isEmpty()) {
            System.out.println("No events found");
        } else {
            for (Event event : filtered) {
                System.out.println(event.status() + " at " + event.timestamp());
            }
        }
    }

    protected void randomPause() {
        int pause = MIN_PAUSE_MS + random.nextInt(ADDITIONAL_PAUSE_RANGE);
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}