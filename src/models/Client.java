package models;

import static misc.Constants.MIN_PASSWORD_LENGTH;
import static misc.Constants.EMAIL_REGEX;
import static misc.Constants.PHONE_REGEX;
import static misc.Constants.AUDIT;
import static misc.Constants.IO;
import static misc.Constants.DB;
import static misc.Constants.DATE;
import exceptions.InvalidFieldException;
import exceptions.UniqueFieldException;
import misc.Enums.ClientMenu;
import misc.Enums.ClientLibrary;
import misc.Enums.ClientWishlist;
import misc.Enums.ClientEdit;
import interfaces.Crud;
import interfaces.ProfileEdit;
import interfaces.ProfileMenu;
import services.HelperService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang3.tuple.Pair;

public final class Client extends User implements Crud<Client>, ProfileEdit, ProfileMenu {
    // Fields
    private String firstName;
    private String lastName;
    private String phoneNumber;


    // Other members
    private final String[] filters = {"game", "dlc"};
    private final String[] sortOptions = {"Name", "Price", "Release date"};
    private Integer filterIndex = 0;
    private Integer sortIndex = 0;


    // Constructors
    public Client(Integer ID, 
                  String username, 
                  String password, 
                  String email, 
                  Boolean isBanned, 
                  String type, 
                  String firstName, 
                  String lastName, 
                  String phoneNumber) {
        super(ID, username, password, email, isBanned, type);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public Client(Client client) {
        super(client);
        this.firstName = client.getFirstName();
        this.lastName = client.getLastName();
        this.phoneNumber = client.getPhoneNumber();
    }


    // Important methods
    @Override
    public Client clone() {
        return new Client(this);
    }


    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public static Map<Integer, Client> getClients() throws SQLException {
        Map<Integer, Client> clients = new HashMap<>();

        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT client.user_id, username, password, email, is_banned, type, first_name, last_name, phone_number" +
                "\nFROM user" +
                "\nJOIN client ON user.user_id = client.user_id"
            );

        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            clients.put(
                rs.getInt("user_id"),
                new Client(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getBoolean("is_banned"),
                    rs.getString("type"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone_number")
                )
            );
        }

        return clients;
    }

    public static List<String> getInfo(Integer clientID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT"+
                "\n    username," +
                "\n    COUNT(CASE WHEN game.type = 'game' THEN 1 END) AS no_games," +
                "\n    COUNT(CASE WHEN game.type = 'dlc' THEN 1 END) AS no_dlcs," +
                "\n    SUM(purchase.price) AS money_spend" +
                "\nFROM purchase" +
                "\nJOIN game ON purchase.game_id = game.game_id" +
                "\nJOIN user ON purchase.user_id = user.user_id" +
                "\nWHERE purchase.user_id = ?"
            );

        preparedStatement.setInt(1, clientID);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return List.of(
                rs.getString("username"),
                rs.getString("no_games"),
                rs.getString("no_dlcs"),
                rs.getString("money_spend")
            );
        }
        return null;
    }

    public static Client getFromInput() {
        IO.printLogo();

        HelperService.Validator usernameValidator = (value) -> {
            if (value.isEmpty()) {
                throw new InvalidFieldException("The username must not be empty!");
            }
            Map<Integer, User> users = DB.getUsers();
            for (User user : users.values()) {
                if (user.getUsername().toLowerCase().equals(value.toLowerCase())) {
                    throw new UniqueFieldException("The username " + value + " is already taken!");
                }
            }
            return true;
        };

        String username = HelperService.getInput(
            usernameValidator, 
            "Username: "
        );
        if (username == null) {
            return null;
        }

        String password = HelperService.getInput(
            value -> {
                if (value.length() < MIN_PASSWORD_LENGTH) {
                    throw new InvalidFieldException("The password must be at least 4 characters long!");
                }
                return true;
            }, 
            "Password: "
        );
        if (password == null) {
            return null;
        }

        String firstName = HelperService.getInput(
            value -> {
                if (value.isEmpty()) {
                    throw new InvalidFieldException("The first name must not be empty!");
                }
                return true;
            },
            "First name: "
        );
        if (firstName == null) {
            return null;
        }

        String lastName = HelperService.getInput(
            value -> {
                if (value.isEmpty()) {
                    throw new InvalidFieldException("The last name must not be empty!");
                }
                return true;
            },
            "Last name: "
        );
        if (lastName == null) {
            return null;
        }

        String email = HelperService.getInput(
            value -> {
                if (!value.isEmpty() && !value.matches(EMAIL_REGEX)) {
                    throw new InvalidFieldException("Invalid email format!");
                }
                return true;
            },
            "Email (optional): "
        );
        if (email == null) {
            return null;
        }

        String phoneNumber = HelperService.getInput(
            value -> {
                if (!value.isEmpty() && !value.matches(PHONE_REGEX)) {
                    throw new InvalidFieldException("Invalid phone number format!");
                }
                return true;
            }, 
            "Phone number (optional): "
        );
        if (phoneNumber == null) {
            return null;
        }

        return new Client(
            User.nextID + 1, 
            username, 
            password, 
            email.isEmpty() ? "" : email, 
            false, 
            "client", 
            firstName, 
            lastName, 
            phoneNumber.isEmpty() ? "" : phoneNumber
        );
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement("INSERT INTO user VALUES (?, ?, ?, ?, ?, ?)");
        preparedStatement.setInt(1, ID);
        preparedStatement.setString(2, username);
        preparedStatement.setString(3, password);
        preparedStatement.setString(4, email);
        preparedStatement.setBoolean(5, isBanned);
        preparedStatement.setString(6, type);

        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement("INSERT INTO client VALUES (?, ?, ?, ?)");
        preparedStatement.setInt(1, ID);
        preparedStatement.setString(2, firstName);
        preparedStatement.setString(3, lastName);
        preparedStatement.setString(4, phoneNumber);

        DB.modifyClients(this, "create");

        return preparedStatement.executeUpdate();
    }

    @Override
    public Client read() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT client.user_id, username, password, email, is_banned, type, first_name, last_name, phone_number" +
                "\nFROM user" +
                "\nJOIN client ON user.user_id = client.user_id" +
                "\nWHERE client.user_id = ?"
            );

        preparedStatement.setInt(1, ID);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return new Client(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getBoolean("is_banned"),
                rs.getString("type"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone_number")
            );
        }
        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        PreparedStatement preparedStatement;

        if (column.equals("first_name") || column.equals("last_name") || column.equals("phone_number")) {
            preparedStatement = DB.getConnection()
                .prepareStatement(
                    "UPDATE client" +
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

        DB.modifyClients(this.read(), "update");

        return response;
    }

    @Override
    public int delete() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM purchase" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM wishlist" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM client" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement("DELETE FROM user"
                              + "\nWHERE user_id = ?");
        preparedStatement.setInt(1, ID);

        DB.modifyClients(this, "delete");

        return preparedStatement.executeUpdate();
    }


    // Custom edit methods
    public void editFirstName() {
        HelperService.editField(
            this,
            "first_name",
            (value) -> !value.isEmpty(), 
            "First name cannot be empty!"
        );
    }

    public void editLastName() {
        HelperService.editField(
            this,
            "last_name",
            (value) -> !value.isEmpty(), 
            "Last name cannot be empty!"
        );
    }

    public void editPhoneNumber() {
        HelperService.editField(
            this,
            "phone_number", 
            (value) -> value.isEmpty() || value.matches(PHONE_REGEX), 
            "Invalid phone number format!"
        );
    }


    // Formattable interface
    public String format() {
        return super.format() + 
               "\nFirst name: " + firstName + 
               "\nLast name: " + lastName + 
               "\nPhone number: " + (phoneNumber.isEmpty() ? "N/A" : phoneNumber);
    }


    // Menu Helpers
    private void resetMenu() {
        filterIndex = 0;
        sortIndex = 0;
    }

    private void buyGame(List<Game> games) throws SQLException {
        Game game = IO.selectFromOptions(
            games, 
            "No " + filters[filterIndex] + " to buy!"
        );
        if (game == null) {
            return;
        }

        Purchase newPurchase = new Purchase(
            game.getID(), 
            ID,
            DATE.format(new Date()), 
            game.getPrice()
        );
        newPurchase.create();

        AUDIT.writeToLibrariesCSV(this.getID());
    }

    private void buyGameFromWishlist(List<Wishlist> wishlist) throws SQLException {
        Wishlist wishlistItem = IO.selectFromOptions(
            wishlist, 
            "No " + filters[filterIndex] + " to buy!"
        );
        if (wishlistItem == null) {
            return;
        }

        Purchase newPurchase = new Purchase(
            wishlistItem.getGameID(), 
            ID,
            DATE.format(new Date()), 
            wishlistItem.getPrice()
        );
        newPurchase.create();
        wishlistItem.delete();

        AUDIT.writeToLibrariesCSV(this.getID());

    }

    private void removeFromLibrary(List<Purchase> purchases) throws SQLException {
        Purchase purchase = IO.selectFromOptions(
            purchases, 
            "No " + filters[filterIndex] + "s owned!"
        );
        if (purchase == null) {
            return;
        }

        purchase.delete();
    }

    private void addToWishlist(List<Game> games) throws SQLException {
        Game game = IO.selectFromOptions(
            games, 
            "No " + filters[filterIndex] + " to add to wishlist!"
        );
        if (game == null) {
            return;
        }

        Wishlist newWishlistItem = new Wishlist(
            game.getID(), 
            ID,
            DATE.format(new Date())
        );
        newWishlistItem.create();
    }

    private void removeFromWishlist(List<Wishlist> wishlist) throws SQLException {
        Wishlist wishlistItem = IO.selectFromOptions(
            wishlist, 
            "No " + filters[filterIndex] + "s owned!"
        );
        if (wishlistItem == null) {
            return;
        }

        wishlistItem.delete();
    }


    // Menus
    private void libraryMenu() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Show " + filters[filterIndex] + "s\n", 
            "Buy " + filters[filterIndex] + '\n', 
            "Remove " + filters[filterIndex] + '\n', 
            "Change filter\n",
            "Change sorting criteria (" + sortOptions[sortIndex] + ")\n",
            "Go back\n"
        };
        ClientLibrary option = ClientLibrary.values()[IO.getValidInput(options)];

        if (option == ClientLibrary.GO_BACK) {
            return;
        }
        if (option == ClientLibrary.CHANGE_FILTER) {
            filterIndex = (filterIndex + 1) % filters.length;
            libraryMenu();
            return;
        }
        if (option == ClientLibrary.CHANGE_SORT) {
            sortIndex = (sortIndex + 1) % sortOptions.length;
            libraryMenu();
            return;
        }

        IO.printLogo();
        List<Purchase> purchases = Purchase.filterByUser(
            new ArrayList<>(DB.getPurchases().values()),
            ID
        );
        purchases = Purchase.sortByCriteria(
            purchases, 
            sortOptions[sortIndex]
        );

        if (option == ClientLibrary.SHOW_ALL) {
            IO.printItems(
                purchases, 
                "No games owned!"
            );
        }
        else if (option == ClientLibrary.SHOW) {
            IO.printItems(
                Purchase.filterByType(purchases, filters[filterIndex]), 
                "No " + filters[filterIndex] + "s owned!"
            );
        }
        else if (option == ClientLibrary.BUY) {
            if (filters[filterIndex].equals("game")) {
                buyGame(
                    Game.sortByCriteria(
                        Game.filterByType(
                            Game.filterDisowned(ID), 
                            filters[filterIndex]
                        ),
                        sortOptions[sortIndex]
                    )
                );
            }
            else {
                buyGame(
                    Game.sortByCriteria(
                        Game.filterByType(
                            DLC.filterDisowned(ID), 
                            filters[filterIndex]
                        ),
                        sortOptions[sortIndex]
                    )
                );
            }
        }
        else if (option == ClientLibrary.REMOVE) {
            removeFromLibrary(Purchase.filterByType(purchases, filters[filterIndex]));
        }

        libraryMenu();
    }

    private void wishlistMenu() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Show " + filters[filterIndex] + "s\n",
            "Buy " + filters[filterIndex] + '\n',
            "Add " + filters[filterIndex] + '\n', 
            "Remove " + filters[filterIndex] + '\n', 
            "Change filter\n",
            "Change sorting criteria (" + sortOptions[sortIndex] + ")\n",
            "Go back\n"
        };
        ClientWishlist option = ClientWishlist.values()[IO.getValidInput(options)];

        if (option == ClientWishlist.GO_BACK) {
            return;
        }
        if (option == ClientWishlist.CHANGE_FILTER) {
            filterIndex = (filterIndex + 1) % filters.length;
            wishlistMenu();
            return;
        }
        if (option == ClientWishlist.CHANGE_SORT) {
            sortIndex = (sortIndex + 1) % sortOptions.length;
            wishlistMenu();
            return;
        }

        IO.printLogo();
        List<Wishlist> wishlist = Wishlist.filterByUser(
            new ArrayList<>(DB.getWishlists().values()),
            ID
        );
        wishlist = Wishlist.sortByCriteria(
            wishlist, 
            sortOptions[sortIndex]
        );

        if (option == ClientWishlist.SHOW_ALL) {
            IO.printItems(
                wishlist, 
                "No games in wishlist!"
            );
        }
        else if (option == ClientWishlist.SHOW) {
            IO.printItems(
                Wishlist.filterByType(wishlist, filters[filterIndex]), 
                "No " + filters[filterIndex] + "s in wishlist!"
            );
        }
        else if (option == ClientWishlist.BUY) {
            buyGameFromWishlist(
                Wishlist.filterByType(
                    Wishlist.filterByPublishedStatus(
                        wishlist, 
                        true
                    ), 
                    filters[filterIndex]
                )
            );
        }
        else if (option == ClientWishlist.ADD) {
            List<Game> games;
            if (filters[filterIndex].equals("game")) {
                games = Game.filterNotInWishlist(ID);
            }
            else {
                games = DLC.filterNotInWishlist(ID);
            }
            games = Game.sortByCriteria(
                games, 
                sortOptions[sortIndex]
            );
            addToWishlist(Game.filterByType(games, filters[filterIndex]));
        }
        else if (option == ClientWishlist.REMOVE) {
            removeFromWishlist(Wishlist.filterByType(wishlist, filters[filterIndex]));
        }

        wishlistMenu();
    }

    private void editMenu() throws SQLException {
        String[] options = {
            "Edit username\n", 
            "Edit password\n", 
            "Edit email\n", 
            "Edit first name\n", 
            "Edit last name\n", 
            "Edit phone number\n", 
            "Go back\n"
        };
        ClientEdit option = ClientEdit.values()[IO.getValidInput(options)];

        if (option == ClientEdit.GO_BACK) {
            profileMenu(this::editMenu);
            return;
        }
        if (option == ClientEdit.EDIT_USERNAME) {
            editUsername();
        }
        else if (option == ClientEdit.EDIT_PASSWORD) {
            editPassword();
        }
        else if (option == ClientEdit.EDIT_EMAIL) {
            editEmail();
        }
        else if (option == ClientEdit.EDIT_FIRST_NAME) {
            editFirstName();
        }
        else if (option == ClientEdit.EDIT_LAST_NAME) {
            editLastName();
        }
        else if (option == ClientEdit.EDIT_PHONE_NUMBER) {
            editPhoneNumber();
        }
        editMenu();
    }

    @Override
    public void menu() throws SQLException {
        String[] options = {
            "Library\n", 
            "Wishlist\n", 
            "Profile\n", 
            "Log out\n"
        };
        ClientMenu option = ClientMenu.values()[IO.getValidInput(options)];

        if (option == ClientMenu.LOG_OUT) {
            return;
        }

        IO.printLogo();
        resetMenu();

        if (option == ClientMenu.LIBRARY) {
            libraryMenu();
        }
        else if (option == ClientMenu.WISHLIST) {
            wishlistMenu();
        }
        else if (option == ClientMenu.PROFILE) {
            profileMenu(this::editMenu);
        }

        menu();
    }
}
