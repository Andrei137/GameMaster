package interfaces;

import static misc.Constants.MIN_PASSWORD_LENGTH;
import static misc.Constants.EMAIL_REGEX;
import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import exceptions.UniqueFieldException;
import models.User;
import services.HelperService;

import java.sql.SQLException;
import java.util.Map;

public interface ProfileEdit {
    default void editUsername() {
        HelperService.Validator validator = (value) -> {
            if (value.isEmpty()) {
                return false;
            }

            Map<Integer, User> users = DB.getUsers();
            for (User user : users.values()) {
                if (user.getUsername().toLowerCase().equals(value.toLowerCase())) {
                    throw new UniqueFieldException("The username " + value + " is already taken!");
                }
            }

            return true;
        };

        HelperService.editField(
            this,
            "username",
            validator,
            "Username cannot be empty!"
        );
    }

    default void editPassword() {
        HelperService.editField(
            this,
            "password", 
            (value) -> value.length() >= MIN_PASSWORD_LENGTH,
            "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long!"
        );
    }

    default void editEmail() {
        HelperService.Validator validator = (value) -> {
            if (!value.isEmpty() && !value.matches(EMAIL_REGEX)) {
                IO.pauseOutput("Invalid email format!");
                return false;
            }

            return true;
        };
        HelperService.editField(
            this,
            "email",
            validator,
            "Email cannot be empty!"
        );
    }
}
