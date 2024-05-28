package services;

import static misc.Constants.AUDIT;
import static misc.Constants.IO;
import static misc.Constants.DB;
import exceptions.InvalidFieldException;
import exceptions.UniqueFieldException;
import misc.Heart;
import misc.Enums.MainMenu;
import misc.Enums.AuditMenu;
import models.Client;
import models.Provider;
import models.Admin;
import models.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public final class AppService {
    // Singleton instance
    private static AppService INSTANCE = null;


    // Constructor
    private AppService() {}


    // Getters
    public static AppService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppService();
        }
        return INSTANCE;
    }


    // Menu helpers
    private <T extends User> void loginMenu(Map<Integer, T> users) throws SQLException, InvalidFieldException {
        IO.printLogo();

        String username = IO.getUserInput("Username: ");

        if (username.isEmpty()) {
            throw new InvalidFieldException("Username cannot be empty!");
        }

        String password = IO.getUserInput("Password: ");

        if (password.isEmpty()) {
            throw new InvalidFieldException("Password cannot be empty!");
        }

        boolean invalidUsername = true;
        for (T user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                if (user.getIsBanned()) {
                    IO.pauseOutput("You are banned!");
                    return;
                }
                user.menu();
                return;
            }
            else if (user.getUsername().equals(username)) {
                invalidUsername = false;
            }
        }
        if (invalidUsername) {
            throw new InvalidFieldException("Invalid username!");
        }
        throw new InvalidFieldException("Invalid password!");
    }

    private void quit() throws SQLException {
        DB.getConnection().close();
        IO.clearScreen();
        Heart.INSTANCE.printFullHeart();
    }


    // Menus
    private void loginAdminMenu() throws SQLException, InvalidFieldException {
        loginMenu(DB.getAdmins());
    }

    private void loginProviderMenu() throws SQLException, InvalidFieldException {
        loginMenu(DB.getProviders());
    }

    private void loginClientMenu() throws SQLException, InvalidFieldException {
        loginMenu(DB.getClients());
    }

    private void registerMenu() {
        try {
            Client client = Client.getFromInput();
            if (client == null) {
                return;
            }
            client.create();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAudit() throws IOException {
        String[] options = {
            "Show actions\n",
            "Show libraries\n",
            "Go back\n"
        };
        AuditMenu option = AuditMenu.values()[IO.getValidInput(options)];

        if (option == AuditMenu.GO_BACK) {
            return;
        }
        if (option == AuditMenu.ACTIONS) {
            AUDIT.showActionsCSV();
        }
        else if (option == AuditMenu.LIBRARIES) {
            AUDIT.showLibrariesCSV();
        }
        showAudit();
    }

    private void resetAudit() throws IOException {
        String[] options = {
            "Reset actions\n",
            "Reset libraries\n",
            "Go back\n"
        };
        AuditMenu option = AuditMenu.values()[IO.getValidInput(options)];

        if (option == AuditMenu.GO_BACK) {
            return;
        }
        if (option == AuditMenu.ACTIONS) {
            AUDIT.resetActionsCSV();
        }
        else if (option == AuditMenu.LIBRARIES) {
            AUDIT.resetLibrariesCSV();
        }
        resetAudit();
    }

    private void mainMenu() throws SQLException, IOException, InvalidFieldException {
        String[] options = {
            "Login as admin\n", 
            "Login as provider\n", 
            "Login as client\n", 
            "Register as client\n",
            "Show audit\n",
            "Reset audit\n",
            "Exit\n"
        };
        MainMenu option = MainMenu.values()[IO.getValidInput(options)];

        if (option == MainMenu.QUIT) {
            quit();
        }
        else {
            if (option == MainMenu.LOGIN_ADMIN) {
                loginAdminMenu();
            }
            else if (option == MainMenu.LOGIN_PROVIDER) {
                loginProviderMenu();
            }
            else if (option == MainMenu.LOGIN_CLIENT) {
                loginClientMenu();
            }
            else if (option == MainMenu.REGISTER_CLIENT) {
                registerMenu();
            }
            else if (option == MainMenu.SHOW_AUDIT) {
                showAudit();
            }
            else if (option == MainMenu.RESET_AUDIT) {
                resetAudit();
            }

            mainMenu();
        }
    }

    public void run() {
        try {
            DB.loadDatabase();
            mainMenu();
        }
        catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        catch (InvalidFieldException e) {
            IO.pauseOutput(e.getMessage());
            run();
        }
    }
}
