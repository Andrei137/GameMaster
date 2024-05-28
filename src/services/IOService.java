package services;

import static misc.Constants.PAUSE_DURATION;
import misc.Logo;

import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.io.Console;

public class IOService {
    // Singleton instance
    private static IOService INSTANCE = null;


    // Constructor
    private IOService() {}


    // Getters
    public static IOService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new IOService();
        }
        return INSTANCE;
    }


    // Helpers
    private final static String formatOptions(String[] options) {
        /*
            For example, the options ["Start, "Settings", "Quit"] become
            [1] Start
            [2] Settings
            [0] Quit

            ->
        */
        StringBuilder formattedOptions = new StringBuilder();
        int length = options.length;
        for (int i = 0; i < length; ++i) {
            formattedOptions.append("[").append((i + 1) % length).append("] ").append(options[i]);
        }
        formattedOptions.append("\n-> ");
        return formattedOptions.toString();
    }

    public final static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } 
            else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } 
        catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Input
    public final static int getValidInput(String[] options) {
        String formattedOptions = formatOptions(options);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            clearScreen();
            System.out.println(Logo.INSTANCE.getLogo() + "\n\n\n< Choose an option >");
            System.out.print(formattedOptions);
            if (!scanner.hasNextInt()) {
                pauseOutput("Invalid option!");
                scanner.next();
                continue;
            }
            int option = scanner.nextInt();
            if (option >= 0 && option < options.length) {
                return option;
            }
            pauseOutput("Invalid option!");
            clearScreen();
        }
    }

    public final static String getUserInput(String title) {
        if (title.contains("Password") || title.contains("password")) {
            return new String(System.console().readPassword(title));
        }
        System.out.print(title);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public final static void pressAnyKeyToContinue() {
        getUserInput("\nPress anything to continue...");
    }

    public <T> T selectFromOptions(List<T> items, String messageForEmpty) {
        if (items.isEmpty()) {
            System.out.println(messageForEmpty);
            pressAnyKeyToContinue();
            return null;
        }

        String[] options = new String[items.size() + 1];
        for (T item : items) {
            options[items.indexOf(item)] = item.toString().replace(" \n", "\n    ");
        }
        options[items.size()] = "Go back\n";

        int index = getValidInput(options);
        if (index == 0) {
            return null;
        }
        return items.get(index - 1);
    }


    // Output
    public final static void printLogo() {
        clearScreen();
        System.out.println(Logo.INSTANCE.getLogo() + "\n\n");
    }

    public final static void pauseOutput(String message) {
        try {
            System.out.println(message);
            Thread.sleep(PAUSE_DURATION);
            clearScreen();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public <T> void printItems(List<T> items, String messageForEmpty) {
        if (items.isEmpty()) {
            System.out.println(messageForEmpty);
            pressAnyKeyToContinue();
            return;
        }

        items.forEach(item -> System.out.println(item));
        pressAnyKeyToContinue();
    }
}
