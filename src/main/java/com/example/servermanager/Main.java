package com.example.servermanager;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Main {
    private static final String EXIT_COMMAND = "exit";
    private static final String STATUS_COMMAND = "status";
    private static final String UP_COMMAND = "up";
    private static final String DOWN_COMMAND = "down";
    private static final String HISTORY_COMMAND = "history";
    private static final String BEFORE_PARAM = "before";
    private static final String FROM_PARAM = "from";
    private static final String TO_PARAM = "to";
    private static final String SORT_PARAM = "sort";
    private static final String STATUS_PARAM = "status";
    private static final String PROMPT = "> ";
    private static final String WELCOME_MESSAGE = "Server Manager Application. Enter commands (type 'exit' to quit):";
    private static final String UNSUPPORTED_COMMAND_MESSAGE = "Unsupported command: ";

    public static void main(String[] args) {
        ServerManager serverManager = new ServerManager();
        Scanner scanner = new Scanner(System.in);
        System.out.print(WELCOME_MESSAGE);

        while (true) {
            System.out.print(PROMPT);
            String input = scanner.nextLine();
            if (EXIT_COMMAND.equalsIgnoreCase(input.trim())) {
                break;
            }
            CommandParser parser = new CommandParser(input);
            String command = parser.getCommand();
            if (command == null) {
                continue;
            }
            switch (command) {
                case STATUS_COMMAND:
                    serverManager.status();
                    break;
                case UP_COMMAND:
                    String before = parser.getParam(BEFORE_PARAM);
                    serverManager.up(before);
                    break;
                case DOWN_COMMAND:
                    serverManager.down();
                    break;
                case HISTORY_COMMAND:
                    String from = parser.getParam(FROM_PARAM);
                    String to = parser.getParam(TO_PARAM);
                    String sort = parser.getParam(SORT_PARAM);
                    String status = parser.getParam(STATUS_PARAM);
                    serverManager.history(from, to, sort, status);
                    break;
                default:
                    System.err.println(UNSUPPORTED_COMMAND_MESSAGE + command);
            }
        }
        scanner.close();
    }
}