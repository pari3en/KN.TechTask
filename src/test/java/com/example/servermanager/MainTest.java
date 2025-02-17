package com.example.servermanager;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MainTest {

    @Mock
    private ServerManager serverManager;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
        Main.setServerManager(serverManager);
    }

    @Test
    void testStatusCommand() {
        simulateUserInput("status\nexit\n");
        Main.main(new String[]{});
        
        verify(serverManager).status();
        verifyNoMoreInteractions(serverManager);
    }

    @Test
    void testUpCommand() {
        simulateUserInput("up --before server1\nexit\n");
        Main.main(new String[]{});
        
        verify(serverManager).up("server1");
        verifyNoMoreInteractions(serverManager);
    }

    @Test
    void testDownCommand() {
        simulateUserInput("down\nexit\n");
        Main.main(new String[]{});
        
        verify(serverManager).down();
        verifyNoMoreInteractions(serverManager);
    }

    @Test
    void testHistoryCommand() {
        simulateUserInput("history --from 2024-01-01 --to 2024-01-31 --sort desc --status UP\nexit\n");
        Main.main(new String[]{});
        
        verify(serverManager).history("2024-01-01", "2024-01-31", "desc", "UP");
        verifyNoMoreInteractions(serverManager);
    }

    @Test
    void testUnsupportedCommand() {
        simulateUserInput("invalidcommand\nexit\n");
        Main.main(new String[]{});
        
        String output = outputStream.toString();
        assertTrue(output.contains("Unsupported command: invalidcommand"));
        verifyNoMoreInteractions(serverManager);
    }

    private void simulateUserInput(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(System.in);
    }
} 