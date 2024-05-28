package services;

import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import exceptions.InvalidFieldException;
import exceptions.UniqueFieldException;
import interfaces.Crud;

import java.util.List;
import java.util.function.Predicate;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.sql.SQLException;

public final class HelperService {
    public static <T> List<T> filterByCondition(List<T> items, Predicate<T> condition) {
        return items.stream()
                    .filter(condition)
                    .collect(Collectors.toList());
    }

    public static <T> List<T> sortByCriteria(List<T> items, Comparator<T> comparator) {
         return items.stream()
                     .sorted(comparator)
                     .collect(Collectors.toList());
    }

    public static String getInput(Validator validator, String message) {
        try {
            String value = IO.getUserInput(message);

            if (!validator.validate(value)) {
                return null;
            }

            return value;
        }
        catch (UniqueFieldException | InvalidFieldException e) {
            IO.pauseOutput(e.getMessage());
            return null;
        }
    }

    public static <T> void editField(T object, String field, Validator validator, String errorMessage) {
        try {
            IO.printLogo();

            String newValue = IO.getUserInput(FORMAT.promptForField(field));

            if (!validator.validate(newValue)) {
                IO.pauseOutput(errorMessage);
                return;
            }

            ((Crud)object).update(field, newValue, "string");
            IO.pauseOutput("Successfully updated " + field.replace("_", " ") + "!");
        } 
        catch (UniqueFieldException | InvalidFieldException e) {
            IO.pauseOutput(e.getMessage());
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface Validator {
        boolean validate(String value) throws UniqueFieldException, InvalidFieldException;
    }
}
