package com.example.servermanager;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CommandParser {
    @Getter
    private String command;
    private final Map<String, String> params;


    public CommandParser(String input) {
        params = new HashMap<>();
        parse(input);
    }

    private void parse(String input) {
        if (input == null || input.isEmpty()) return;
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) return;
        command = tokens[0].toLowerCase();

        // Parse parameters in the form --param value
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("--")) {
                String key = token.substring(2).toLowerCase();
                if (i + 1 < tokens.length && !tokens[i + 1].startsWith("--")) {
                    params.put(key, tokens[i + 1]);
                    i++;
                } else {
                    // Parameter provided without a value
                    params.put(key, null);
                }
            }
        }
    }

    public String getParam(String key) {
        return params.get(key.toLowerCase());
    }
}