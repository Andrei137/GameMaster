package services;

import static misc.Constants.LOG_FOLDER;
import static misc.Constants.ACTIONS_FILE;
import static misc.Constants.LIBRARIES_FILE;
import static misc.Constants.DATE_FORMAT;
import static misc.Constants.TIME_FORMAT;
import models.Client;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AuditService {
    // Singleton instance
    private static AuditService INSTANCE = null;


    // Other members
    private static final String actionsFile = LOG_FOLDER + "/" + ACTIONS_FILE;
    private static final String librariesFile = LOG_FOLDER + "/" + LIBRARIES_FILE;
    private static File actions;
    private static File libraries;


    // Initialization
    static {
        try {
            actions = new File(actionsFile);
            libraries = new File(librariesFile);

            if (!actions.exists()) {
                FileWriter fileWriter = new FileWriter(actions, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                bufferedWriter.write("Date, Hour, Action");
                bufferedWriter.newLine();

                bufferedWriter.close();
                fileWriter.close();
            }

            if (!libraries.exists()) {
                FileWriter fileWriter = new FileWriter(libraries, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                bufferedWriter.write("Date, Hour, Username, No. games, No. dlcs, Money spent");
                bufferedWriter.newLine();

                bufferedWriter.close();
                fileWriter.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Constructor
    private AuditService() {}


    // Getters
    public static AuditService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuditService();
        }

        return INSTANCE;
    }

    private static String getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        return String.format(
            DATE_FORMAT, 
            now.getDayOfMonth(), 
            now.getMonthValue(), 
            now.getYear()
        );
    }

    private static String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return String.format(
            TIME_FORMAT, 
            now.getHour(), 
            now.getMinute(),
            now.getSecond()
        );
    }


    // Helpers
    private static void showCSV(File file) throws IOException {
        java.awt.Desktop.getDesktop().open(file);
    }

    private static void resetCSV(File file) throws IOException {
        file.delete();

        FileWriter fileWriter = new FileWriter(file, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        if (file.equals(actions)) {
            bufferedWriter.write("Date, Hour, Action");
        }
        else {
            bufferedWriter.write("Date, Hour, Username, No. games, No. dlcs, Money spent");
        }

        bufferedWriter.newLine();

        bufferedWriter.close();
        fileWriter.close();
    }

    public static void writeToActionsCSV(String action, String table) {
        try {
            if (actions == null) {
                return;
            }

            FileWriter fileWriter = new FileWriter(actions, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            StringBuilder line = new StringBuilder();
            line.append(getCurrentDate()).append(", ");
            line.append(getCurrentTime()).append(", ");
            line.append(action + " -> " + table);

            bufferedWriter.write(line.toString());
            bufferedWriter.newLine();

            bufferedWriter.close();
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToLibrariesCSV(Integer clientID) {
        try {
            if (libraries == null) {
                return;
            }

            List<String> clientInfo = Client.getInfo(clientID);

            FileWriter fileWriter = new FileWriter(libraries, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            StringBuilder line = new StringBuilder();
            line.append(getCurrentDate()).append(", ");
            line.append(getCurrentTime()).append(", ");
            line.append(clientInfo.get(0)).append(", ");
            line.append((clientInfo.get(1).equals("0")) ? "None" : clientInfo.get(1)).append(", ");
            line.append((clientInfo.get(2).equals("0")) ? "None" : clientInfo.get(2)).append(", ");
            line.append(clientInfo.get(3) + " RON");

            bufferedWriter.write(line.toString());
            bufferedWriter.newLine();

            bufferedWriter.close();
            fileWriter.close();
        }
        catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void showActionsCSV() throws IOException {
        showCSV(actions);
    }

    public static void showLibrariesCSV() throws IOException {
        showCSV(libraries);
    }

    public static void resetActionsCSV() throws IOException {
        resetCSV(actions);
    }

    public static void resetLibrariesCSV() throws IOException {
        resetCSV(libraries);
    }
}
