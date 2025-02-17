package com.example.servermanager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    void testCommandWithSingleParameter() {
        CommandParser parser = new CommandParser("start --port 8080");
        assertEquals("start", parser.getCommand());
        assertEquals("8080", parser.getParam("port"));
    }

    @Test
    void testCommandWithMultipleParameters() {
        CommandParser parser = new CommandParser("deploy --app myapp --version 1.0 --env prod");
        assertEquals("deploy", parser.getCommand());
        assertEquals("myapp", parser.getParam("app"));
        assertEquals("1.0", parser.getParam("version"));
        assertEquals("prod", parser.getParam("env"));
    }

    @Test
    void testParameterWithoutValue() {
        CommandParser parser = new CommandParser("stop --force");
        assertEquals("stop", parser.getCommand());
        assertNull(parser.getParam("force"));
    }

    @Test
    void testParameterCaseInsensitivity() {
        CommandParser parser = new CommandParser("START --Port 8080");
        assertEquals("start", parser.getCommand());
        assertEquals("8080", parser.getParam("PORT"));
        assertEquals("8080", parser.getParam("port"));
    }

    @Test
    void testExtraSpaces() {
        CommandParser parser = new CommandParser("  start   --port    8080  ");
        assertEquals("start", parser.getCommand());
        assertEquals("8080", parser.getParam("port"));
    }
} 