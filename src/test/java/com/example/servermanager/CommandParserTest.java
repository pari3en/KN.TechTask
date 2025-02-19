package com.example.servermanager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommandParserTest {

    @Test
    void testEmptyInput() {
        CommandParser parser = new CommandParser("");
        assertNull(parser.getCommand());
        assertNull(parser.getParam("any"));
    }

    @Test
    void testNullInput() {
        CommandParser parser = new CommandParser(null);
        assertNull(parser.getCommand());
        assertNull(parser.getParam("any"));
    }

    @Test
    void testCommandOnly() {
        CommandParser parser = new CommandParser("start");
        assertEquals("start", parser.getCommand());
        assertNull(parser.getParam("any"));
    }

    @Test
    void testParameterWithoutValue() {
        CommandParser parser = new CommandParser("stop --force");
        assertEquals("stop", parser.getCommand());
        assertNull(parser.getParam("force"));
    }

    @Test
    void testGetCommandLine() {
        CommandParser parser = new CommandParser("status --from 2024-01-01");
        assertNotNull(parser.getCmdLine());
        assertTrue(parser.getCmdLine().hasOption("from"));
    }

    @Test
    void testEmptyTokens() {
        // Create input that will result in empty tokens after splitting
        CommandParser parser = new CommandParser("   ");
        assertNull(parser.getCommand());
        assertNull(parser.getCmdLine());
    }

    @Test
    void testParseError() {
        // Test with invalid option format to trigger parse error
        CommandParser parser = new CommandParser("status --invalid-format");
        assertNotNull(parser.getCommand());
        assertNull(parser.getCmdLine());
    }

}