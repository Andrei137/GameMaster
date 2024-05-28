package models;

import interfaces.Formattable;
import services.HelperService;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

public abstract class User implements Formattable {
    // Fields
    protected Integer ID;
    protected String username;
    protected String password;
    protected String email;
    protected Boolean isBanned;
    protected String type;


    // Other members
    public static Integer nextID = 0;


    // Constructors
    public User(Integer ID, 
                String username, 
                String password, 
                String email, 
                Boolean isBanned, 
                String type) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isBanned = isBanned;
        this.type = type;

        nextID = Integer.max(nextID, ID);
    }

    public User(User user) {
        this.ID = user.getID();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.isBanned = user.getIsBanned();
        this.type = user.getType();
    }


    // Imortant methods
    @Override
    public String toString() {
        return username + " (" + type + (this.isBanned ? " | banned" : "") + ")\n";
               
    }

    @Override
    public int hashCode() {
        return ID;
    }


    // Getters
    public final Integer getID() {
        return ID;
    }

    public final String getUsername() {
        return username;
    }

    public final String getPassword() {
        return password;
    }

    public final String getEmail() {
        return email;
    }

    public final Boolean getIsBanned() {
        return isBanned;
    }

    public final String getType() {
        return type;
    }


    // Formattable interface
    public String format() {
        return "Username: " + username +
               "\nPassword: " + password +
               "\nEmail: " + ((email == null || email.isEmpty()) ? "N/A" : email);
    }


    // Filters
    public static List<User> filterByType(List<User> users, String type) {
        if (type != "developer" && type != "publisher") {
            Predicate<User> condition = user -> user.getType().equals(type);
            return HelperService.filterByCondition(users, condition);
        }
        return Provider.filterByType(users, type);
    }

    // Menus
    public abstract void menu() throws SQLException;
}
