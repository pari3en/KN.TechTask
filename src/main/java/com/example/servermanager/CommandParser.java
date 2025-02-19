package com.example.servermanager;

import lombok.Getter;
import org.apache.commons.cli.*;

@Getter
public class CommandParser {
    private String command;
    private CommandLine cmdLine;
    
    private static final Options options = new Options();
    
    static {
        // Define command line options
        options.addOption(Option.builder("before")
                .longOpt("before")
                .hasArg()
                .desc("Schedule shutdown before specified date/time")
                .build());
                
        options.addOption(Option.builder("from")
                .longOpt("from")
                .hasArg()
                .desc("Filter events from date")
                .build());
                
        options.addOption(Option.builder("to")
                .longOpt("to")
                .hasArg()
                .desc("Filter events to date")
                .build());
                
        options.addOption(Option.builder("sort")
                .longOpt("sort")
                .hasArg()
                .desc("Sort order (asc/desc)")
                .build());
                
        options.addOption(Option.builder("status")
                .longOpt("status")
                .hasArg()
                .desc("Filter by status")
                .build());
    }

    public CommandParser(String input) {
        parse(input);
    }

    private void parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) {
            return;
        }

        command = tokens[0].toLowerCase();
        
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

        CommandLineParser parser = new DefaultParser();
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error parsing command: " + e.getMessage());
        }
    }

    public String getParam(String key) {
        if (cmdLine == null) {
            return null;
        }
        return cmdLine.getOptionValue(key);
    }

}