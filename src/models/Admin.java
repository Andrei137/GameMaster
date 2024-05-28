package models;

import static misc.Constants.IO;
import static misc.Constants.DB;
import misc.Enums.AdminMenu;
import misc.Enums.AdminAccounts;
import misc.Enums.AdminEdit;
import interfaces.Crud;
import interfaces.ProfileEdit;
import interfaces.ProfileMenu;
import services.HelperService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public final class Admin extends User implements Crud<Admin>, ProfileEdit, ProfileMenu {
    // Fields
    private double cutPercentage;


    // Other members
    private final String[] filters = {
        "client", 
        "developer", 
        "publisher"
    };
    private Integer filterIndex = 0;


    // Constructors
    public Admin(Integer ID, 
                 String username, 
                 String password, 
                 String email, 
                 Boolean isBanned, 
                 String type, 
                 double cutPercentage) {
        super(ID, username, password, email, isBanned, type);
        this.cutPercentage = cutPercentage;
    }

    public Admin(Admin admin) {
        super(admin);
        this.cutPercentage = admin.getCutPercentage();
    }


    // Important methods
    @Override
    public Admin clone() {
        return new Admin(this);
    }


    // Getters
    public double getCutPercentage() {
        return cutPercentage;
    }

    public static Map<Integer, Admin> getAdmins() throws SQLException {
        Map<Integer, Admin> admins = new HashMap<>();

        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT admin.user_id, username, password, email, is_banned, type, cut_percentage" +
                "\nFROM user" +
                "\nJOIN admin ON user.user_id = admin.user_id"
            );

        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            admins.put(
                rs.getInt("user_id"),
                new Admin(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getBoolean("is_banned"),
                    rs.getString("type"),
                    rs.getDouble("cut_percentage")
                )
            );
        }

        return admins;
    }


    // Crud Interface
    @Override
    public int create() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO user VALUES (?, ?, ?, ?, ?, ?)"
            );

        preparedStatement.setInt(1, ID);
        preparedStatement.setString(2, username);
        preparedStatement.setString(3, password);
        preparedStatement.setString(4, email);
        preparedStatement.setBoolean(5, isBanned);
        preparedStatement.setString(6, type);

        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO admin VALUES (?, ?)"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.setDouble(2, cutPercentage);

        DB.modifyAdmins(this, "create");

        return preparedStatement.executeUpdate();
    }

    @Override
    public Admin read() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT admin.user_id, username, password, email, is_banned, type, cut_percentage" +
                "\nFROM user" +
                "\nJOIN admin ON user.user_id = admin.user_id" +
                "\nWHERE admin.user_id = ?"
            );

        preparedStatement.setInt(1, ID);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return new Admin(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getBoolean("is_banned"),
                rs.getString("type"),
                rs.getDouble("cut_percentage")
            );
        }
        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        PreparedStatement preparedStatement;

        if (column.equals("cut_percentage")) {
            preparedStatement = DB.getConnection()
                .prepareStatement(
                    "UPDATE admin" +
                    "\nSET " + column + " = ?" +
                    "\nWHERE user_id = ?"
                );
        }
        else {
            preparedStatement = DB.getConnection()
                .prepareStatement(
                    "UPDATE user" +
                    "\nSET " + column + " = ?" +
                    "\nWHERE user_id = ?"
                );
        }

        if (type.equals("double")) {
            preparedStatement.setDouble(1, Double.parseDouble(value));
        }
        else if (type.equals("boolean")) {
            preparedStatement.setBoolean(1, Boolean.parseBoolean(value));
        }
        else {
            preparedStatement.setString(1, value);
        }
        preparedStatement.setInt(2, ID);
        
        int response = preparedStatement.executeUpdate();

        DB.modifyAdmins(this.read(), "update");

        return response;
    }

    @Override
    public int delete() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM admin" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM user" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);

        DB.modifyAdmins(this, "delete");

        return preparedStatement.executeUpdate();
    }


    // Custom edit methods
    public void editCutPercentage() {
        HelperService.editField(
            this,
            "cut_percentage", 
            (value) -> true, 
            ""
        );
    }

    private void editBanStatus(List<User> users, Boolean ban) throws SQLException {
        List<User> viableUsers = new ArrayList<>();
        users
            .stream()
            .filter(user -> (ban && !user.getIsBanned()) || (!ban && user.getIsBanned()))
            .forEach(viableUsers::add);

        User user = IO.selectFromOptions(
            viableUsers, 
            "No " + (ban ? "un" : "") + "banned accounts!"
        );
        if (user == null) {
            return;
        }
        if (user.getType().equals("client")) {
            Client client = (Client)user;
            client.update("is_banned", ban.toString(), "boolean");
        }
        else if (user.getType().equals("provider")) {
            Provider provider = (Provider)user;
            provider.update("is_banned", ban.toString(), "boolean");
        }
    }


    // Formattable interface
    public String format() {
        return super.format()
               + "\nCut percentage: " + cutPercentage + "%";
    }


    // Menu Helpers
    private void resetMenu() {
        filterIndex = 0;
    }


    // Menus
    private void accountsMenu() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Show " + filters[filterIndex] + "s\n", 
            "Add " + filters[filterIndex] + '\n', 
            "Ban " + filters[filterIndex] + '\n', 
            "Unban " + filters[filterIndex] + '\n', 
            "Remove " + filters[filterIndex] + '\n', 
            "Change filter\n", 
            "Go back\n"
        };
        AdminAccounts option = AdminAccounts.values()[IO.getValidInput(options)];

        if (option == AdminAccounts.GO_BACK) {
            return;
        }
        if (option == AdminAccounts.CHANGE_FILTER) {
            filterIndex = (filterIndex + 1) % filters.length;
            accountsMenu();
            return;
        }

        List<User> users = new ArrayList<>(DB.getUsers().values());
        IO.printLogo();

        if (option == AdminAccounts.SHOW_ALL) {
            IO.printItems(
                users, 
                "No accounts to show!"
            );
            accountsMenu();
            return;
        }

        users = User.filterByType(users, filters[filterIndex]);
        if (option == AdminAccounts.SHOW) {
            IO.printItems(
                users, 
                "No accounts to show!"
            );
        }
        else if (option == AdminAccounts.ADD) {
            if (filters[filterIndex].equals("client")) {
                Client client = Client.getFromInput();
                if (client == null) {
                    accountsMenu();
                    return;
                }

                client.create();
            }
            else {
                Provider provider = Provider.getFromInput(filters[filterIndex]);
                if (provider == null) {
                    accountsMenu();
                    return;
                }

                provider.create();
            }
        }
        else if (option == AdminAccounts.BAN) {
            editBanStatus(users, true);
        }
        else if (option == AdminAccounts.UNBAN) {
            editBanStatus(users, false);
        }
        else if (option == AdminAccounts.REMOVE) {
            User user = IO.selectFromOptions(
                users, 
                "No accounts to show!"
            );
            if (user == null) {
                accountsMenu();
                return;
            }

            if (user.getType().equals("client")) {
                Client client = (Client)user;
                client.delete();
            }
            else {
                Provider provider = (Provider)user;
                provider.delete();
            }
        }

        accountsMenu();
    }

    private void editMenu() throws SQLException {
        String[] options = {
            "Edit username\n", 
            "Edit password\n", 
            "Edit email\n", 
            "Change cut percetage\n", 
            "Go back\n"
        };
        AdminEdit option = AdminEdit.values()[IO.getValidInput(options)];

        if (option == AdminEdit.GO_BACK) {
            profileMenu(this::editMenu);
            return;
        }

        if (option == AdminEdit.EDIT_USERNAME) {
            editUsername();
        }
        else if (option == AdminEdit.EDIT_PASSWORD) {
            editPassword();
        }
        else if (option == AdminEdit.EDIT_EMAIL) {
            editEmail();
        }
        else if (option == AdminEdit.EDIT_CUT) {
            editCutPercentage();
        }

        editMenu();
    }

    @Override
    public void menu() throws SQLException {
        String[] options = {
            "Manage accounts\n", 
            "Profile\n", 
            "Log out\n"
        };
        AdminMenu option = AdminMenu.values()[IO.getValidInput(options)];

        if (option == AdminMenu.LOG_OUT) {
            return;
        }

        IO.printLogo();
        resetMenu();

        if (option == AdminMenu.MANAGE_ACCOUNTS) {
            accountsMenu();
        }
        else if (option == AdminMenu.PROFILE) {
            profileMenu(this::editMenu);
        }

        menu();
    }
}
