package com.example.servermanager;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class ServerManagerTest {
    private static final String TEST_EVENT_FILE = "test_events.log";
    private EventLogger eventLogger;
    private ServerManager serverManager;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setup() {
        // Delete test event file if it exists
        File file = new File(TEST_EVENT_FILE);
        if (file.exists()) {
            boolean result = file.delete();
            System.out.println("Deletion result is " + result + " for file " + TEST_EVENT_FILE);
        }
        eventLogger = new EventLogger(TEST_EVENT_FILE);
        // Create a ServerManager that uses the test file and overrides randomPause
        serverManager = new ServerManager() {
            {
                this.eventLogger = new EventLogger(TEST_EVENT_FILE);
            }

            @Override
            protected void randomPause() {
                // Skip sleeping during tests
            }
        };
        
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }

    @AfterEach
    void cleanup() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        outputStream.reset();
        serverManager.shutdown();
    }

    @Test
    void testStatusWithNoEvents() {
        serverManager.status();
        assertTrue(outputStream.toString().contains("No events found"));
    }

    @Test
    void testStatusWithUpEvent() {
        eventLogger.logEvent(new Event("up", LocalDateTime.now().minusMinutes(5)));
        serverManager.status();
        String output = outputStream.toString();
        assertTrue(output.contains("Last event: up"));
        assertTrue(output.contains("Uptime:"));
    }

    @Test
    void testStatusWithDownEvent() {
        eventLogger.logEvent(new Event("down", LocalDateTime.now()));
        serverManager.status();
        String output = outputStream.toString();
        assertTrue(output.contains("Last event: down"));
        assertFalse(output.contains("Uptime:"));
    }

    @Test
    void testUpCommandInitialStart() {
        serverManager.up(null);
        List<Event> events = eventLogger.getAllEvents();
        assertEquals(2, events.size());
        assertEquals("starting", events.get(0).status());
        assertTrue(events.get(1).status().equals("up") ||
                  events.get(1).status().equals("failed"));
    }

    @Test
    void testUpCommandWhenAlreadyUp() {
        eventLogger.logEvent(new Event("up", LocalDateTime.now()));
        serverManager.up(null);
        assertTrue(outputStream.toString().contains("Already up"));
    }

    @Test
    void testUpCommandWithValidScheduledShutdown() {
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(2);
        serverManager.up(futureTime.toString());

        // Verify the scheduled message
        assertTrue(outputStream.toString().contains("Auto shutdown scheduled at"));

        // Wait for the scheduled shutdown to complete
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> {
                    List<Event> events = eventLogger.getAllEvents();
                    return events.stream()
                            .anyMatch(e -> e.status().equals("down") || e.status().equals("failed"));
                });

        // Verify the shutdown occurred
        List<Event> events = eventLogger.getAllEvents();
        assertTrue(events.stream().anyMatch(e -> e.status().equals("down") || e.status().equals("failed")));
    }

    @Test
    void testUpCommandWithInvalidScheduledShutdown() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        serverManager.up(pastTime.toString());
        assertTrue(outputStream.toString().contains("Shutdown time must be in the future"));
    }

    @Test
    void testUpCommandWithInvalidDateFormat() {
        serverManager.up("invalid-date");
        assertTrue(outputStream.toString().contains("Invalid date format"));
    }

    @Test
    void testDownCommandFromUpState() {
        eventLogger.logEvent(new Event("up", LocalDateTime.now()));
        serverManager.down();
        List<Event> events = eventLogger.getAllEvents();
        assertTrue(events.size() >= 3);
        assertEquals("stopping", events.get(events.size() - 2).status());
        assertTrue(events.getLast().status().equals("down") ||
                  events.getLast().status().equals("failed"));
    }

    @Test
    void testDownCommandWhenAlreadyDown() {
        eventLogger.logEvent(new Event("down", LocalDateTime.now()));
        serverManager.down();
        assertTrue(outputStream.toString().contains("Already down"));
    }

    @Test
    void testHistoryWithNoEvents() {
        serverManager.history(null, null, null, null);
        assertTrue(outputStream.toString().contains("No events found"));
    }

    @Test
    void testHistoryWithInvalidDateFormat() {
        serverManager.history("invalid-date", null, null, null);
        assertTrue(outputStream.toString().contains("Invalid date format"));
    }

    @Test
    void testHistoryWithValidDateRange() {
        LocalDateTime now = LocalDateTime.now();
        eventLogger.logEvent(new Event("up", now.minusHours(2)));
        eventLogger.logEvent(new Event("down", now.minusHours(1)));
        
        String fromDate = now.minusDays(1).toLocalDate().toString();
        String toDate = now.toLocalDate().toString();
        
        serverManager.history(fromDate, toDate, "desc", null);
        String output = outputStream.toString();
        assertTrue(output.contains("down at"));
        assertTrue(output.contains("up at"));
    }

    @Test
    void testHistoryWithStatusFilter() {
        LocalDateTime now = LocalDateTime.now();
        eventLogger.logEvent(new Event("up", now.minusHours(2)));
        eventLogger.logEvent(new Event("down", now.minusHours(1)));
        
        serverManager.history(null, null, null, "up");
        String output = outputStream.toString();
        assertTrue(output.contains("up at"));
        assertFalse(output.contains("down at"));
    }

    @Test
    void testHistoryWithAscendingSort() {
        LocalDateTime now = LocalDateTime.now();
        eventLogger.logEvent(new Event("up", now.minusHours(2)));
        eventLogger.logEvent(new Event("down", now.minusHours(1)));
        
        serverManager.history(null, null, "asc", null);
        String output = outputStream.toString();
        int upIndex = output.indexOf("up at");
        int downIndex = output.indexOf("down at");
        assertTrue(upIndex < downIndex);
    }

    @Test
    void testHistoryWithDescendingSort() {
        LocalDateTime now = LocalDateTime.now();
        eventLogger.logEvent(new Event("up", now.minusHours(2)));
        eventLogger.logEvent(new Event("down", now.minusHours(1)));
        
        serverManager.history(null, null, "desc", null);
        String output = outputStream.toString();
        int upIndex = output.indexOf("up at");
        int downIndex = output.indexOf("down at");
        assertTrue(downIndex < upIndex);
    }
}