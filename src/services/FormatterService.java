package services;

import java.sql.Date;

public final class FormatterService {
    // Singleton instance
    private static FormatterService INSTANCE = null;


    // Constructor
    private FormatterService() {}


    // Getters
    public static FormatterService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FormatterService();
        }
        return INSTANCE;
    }


    // Helpers
    public static String dateForClass(String date) {
        String[] dateParts = date.split("-");
        return dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
    }

    public static java.sql.Date dateForDB(String date) {
        String[] dateParts = date.split("\\.");
        String formattedDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
        return java.sql.Date.valueOf(formattedDate);
    }

    public static String addOneYear(String date) {
        String[] dateParts = date.split("\\.");
        String year = dateParts[2];
        String nextYear = Integer.toString(Integer.parseInt(year) + 1);
        return dateParts[0] + "." + dateParts[1] + "." + nextYear;
    }

    public static String promptForField(String field) {
        String[] fieldParts = field.split("_");
        StringBuilder formattedField = new StringBuilder();
        formattedField.append("Edit");
        for (String part : fieldParts) {
            formattedField.append(" " + part);
        }
        formattedField.append(": ");
        return formattedField.toString();
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
