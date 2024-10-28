package models;

import static misc.Constants.MIN_PASSWORD_LENGTH;
import static misc.Constants.EMAIL_REGEX;
import static misc.Constants.DATE_REGEX;
import static misc.Constants.WEBSITE_REGEX;
import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import static misc.Constants.DATE;
import exceptions.InvalidFieldException;
import exceptions.UniqueFieldException;
import misc.Enums.ProviderMenu;
import misc.Enums.ProviderEdit;
import misc.Enums.GamesEdit;
import misc.Enums.PublisherContracts;
import misc.Enums.DeveloperContracts;
import misc.Enums.PublisherGames;
import misc.Enums.DeveloperGames;
import interfaces.Crud;
import interfaces.ProfileEdit;
import interfaces.ProfileMenu;
import services.HelperService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

public final class Provider extends User implements Crud<Provider>, ProfileEdit, ProfileMenu {
    // Fields
    private String website;
    private String typeProvider;


    // Other members
    private final String[] filters = {
        "game", 
        "dlc"
    };
    private final String[] sortOptions = {
        "Name", 
        "Price", 
        "Release date"
    };
    private Integer filterIndex = 0;
    private Integer sortIndex = 0;


    // Constructors
    public Provider(Integer ID, 
                    String username, 
                    String password, 
                    String email, 
                    Boolean isBanned, 
                    String type, 
                    String website, 
                    String typeProvider) {
        super(ID, username, password, email, isBanned, type);
        this.website = website;
        this.typeProvider = typeProvider;
    }

    public Provider(Provider provider) {
        super(provider);
        this.website = provider.website;
        this.typeProvider = provider.typeProvider;
    }


    // Important methods
    @Override
    public Provider clone() {
        return new Provider(this);
    }


    // Getters
    public String getWebsite() {
        return website;
    }

    public String getTypeProvider() {
        return typeProvider;
    }

    public static Map<Integer, Provider> getProviders() throws SQLException {
        Map<Integer, Provider> providers = new HashMap<>();
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT provider.user_id, username, password, email, is_banned, type, website, type_provider" +
                "\nFROM user" +
                "\nJOIN provider ON user.user_id = provider.user_id"
            );

        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            providers.put(
                rs.getInt("user_id"),
                new Provider(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getBoolean("is_banned"),
                    rs.getString("type"),
                    rs.getString("website"),
                    rs.getString("type_provider")
                )
            );
        }

        return providers;
    }

    public static Provider getFromInput(String typeProvider) {
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

        String website = HelperService.getInput(
            value -> {
                if (!value.isEmpty() && !value.matches(WEBSITE_REGEX)) {
                    throw new InvalidFieldException("Invalid website format!");
                }
                return true;
            },
            "Website (optional): "
        );
        if (website == null) {
            return null;
        }

        return new Provider(
            User.nextID + 1, 
            username, 
            password, 
            email.isEmpty() ? null : email, 
            false, 
            "provider", 
            website.isEmpty() ? null : website, 
            typeProvider);
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
            .prepareStatement("INSERT INTO provider VALUES (?, ?, ?)");
        preparedStatement.setInt(1, ID);
        preparedStatement.setString(2, website);
        preparedStatement.setString(3, typeProvider);

        
        DB.modifyProviders(this, "create");

        return preparedStatement.executeUpdate();
    }

    @Override
    public Provider read() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT provider.user_id, username, password, email, is_banned, type, website, type_provider" +
                "\nFROM user" +
                "\nJOIN provider ON user.user_id = provider.user_id" +
                "\nWHERE provider.user_id = ?"
            );

        preparedStatement.setInt(1, ID);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return new Provider(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getBoolean("is_banned"),
                rs.getString("type"),
                rs.getString("website"),
                rs.getString("type_provider")
            );
        }
        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        PreparedStatement preparedStatement;

        if (column.equals("website") || column.equals("type_provider")) {
            preparedStatement = DB.getConnection()
                .prepareStatement(
                    "UPDATE provider" +
                    "\nSET " + column + " = ?" +
                    "\nWHERE user_id = ?");
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
        else if (type.equals("int")) {
            preparedStatement.setInt(1, Integer.parseInt(value));
        }
        else if (type.equals("boolean")) {
            preparedStatement.setBoolean(1, Boolean.parseBoolean(value));
        }
        else {
            preparedStatement.setString(1, value);
        }
        preparedStatement.setInt(2, ID);
        
        int response = preparedStatement.executeUpdate();

        DB.modifyProviders(this.read(), "update");

        return response;
    }

    @Override
    public int delete() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM contract" +
                "\nWHERE developer_id = ? OR publisher_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.setInt(2, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM game" +
                "\nWHERE developer_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT game_id, developer_id" +
                "\nFROM game" +
                "\nWHERE publisher_id = ?"
            );
        preparedStatement.setInt(1, ID);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            preparedStatement = DB.getConnection()
                .prepareStatement(
                    "UPDATE game" +
                    "\nSET publisher_id = ?" +
                    "\nWHERE game_id = ?");
            preparedStatement.setInt(1, rs.getInt("developer_id"));
            preparedStatement.setInt(2, rs.getInt("game_id"));
            preparedStatement.executeUpdate();
        }

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM provider" +
                "\nWHERE user_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM user" +
                "\nWHERE user_id = ?");
        preparedStatement.setInt(1, ID);

        DB.modifyProviders(this, "delete");

        return preparedStatement.executeUpdate();
    }


    // Custom edit methods
    public void editWebsite() {
        HelperService.editField(
            this,
            "website",
            (value) -> value.isEmpty() || value.matches(WEBSITE_REGEX),
            "Invalid website format!"
        );

    }


    // Formattable interface
    public String format() {
        Admin admin = DB.getAdmins().values().iterator().next();
        return super.format() +
               "\nWebsite: " + ((website == null || website.isEmpty()) ? "N/A" : website) +
               "\nType: " + typeProvider +
               "\nCut percentage: " + (100 - admin.getCutPercentage()) + "%";
    }


    // Filters
    public static List<User> filterByType(List<User> users, String type) {
        Predicate<User> condition =
            provider -> provider.getType().equals("provider") && ((Provider)provider).getTypeProvider().equals(type);
        return HelperService.filterByCondition(users, condition);
    }


    // Menu Helpers
    private void resetMenu() {
        filterIndex = 0;
        sortIndex = 0;
    }

    private void publishGame(List<Game> games) throws SQLException {
        Game game = IO.selectFromOptions(
            games,
            "No " + filters[filterIndex] + "s to show!"
        );
        if (game == null) {
            return;
        }

        HelperService.Validator priceValidator = value -> {
            if (value.isEmpty()) {
                throw new InvalidFieldException("The price cannot be empty.");
            }
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("The price must be a number.");
            }
            return true;
        };

        IO.printLogo();
        System.out.println("< Game >");
        System.out.println("Name: " + game.getName());
        System.out.println("Developer: " + game.getDeveloperUsername());

        String price = HelperService.getInput(
            priceValidator, 
            "Price: "
        );
        if (price == null) {
            return;
        }

        game.update("publisher_id", this.getID().toString(), "int");
        game.update("price", price, "double");
        game.update("release_date", DATE.format(new Date()), "date");
    }

    private void editGame(List<Game> games) throws SQLException {
        if (games.isEmpty()) {
            System.out.println("No " + filters[filterIndex] + "s to show!");
            IO.pressAnyKeyToContinue();
            return;
        }

        String[] options = {
            "Edit name\n",
            "Edit price\n",
            "Go back\n",
        };
        GamesEdit option = GamesEdit.values()[IO.getValidInput(options)];

        if (option == GamesEdit.GO_BACK) {
            return;
        }
        Game game = IO.selectFromOptions(
            games, 
            "No "+ filters[filterIndex] + "s to show!"
        );
        if (game == null) {
            editGame(games);
            return;
        }

        IO.printLogo();
        System.out.println("< Game >");
        System.out.println("Name: " + game.getName());
        System.out.println("Price: " + game.getPrice() + '\n');

        if (option == GamesEdit.EDIT_NAME) {
            String newName = IO.getUserInput("New name: ");
            if (newName.isEmpty()) {
                IO.pauseOutput("The name must not be empty!");
                return;
            }

            game.update("name", newName, "string");
            IO.pauseOutput("Successfully updated the name!");
        }
        else if (option == GamesEdit.EDIT_PRICE) {
            String newPrice = IO.getUserInput("New price: ");
            if (newPrice.isEmpty()) {
                IO.pauseOutput("No price entered, the price remains the same!");
                return;
            }

            game.update("price", newPrice, "double");
            IO.pauseOutput("Successfully updated the price!");
        }
    }

    private void listGame(List<Game> games, boolean delist) throws SQLException {
        Game game = IO.selectFromOptions(
            Game.filterByVisibility(games, delist), 
            "No " + (delist ? "de" : "") + "listed " + filters[filterIndex] + "s to show!"
        );
        if (game == null) {
            return;
        }

        game.update("is_visible", delist ? "false" : "true", "boolean");
    }

    private void removeGame(List<Game> games) throws SQLException {
        Game game = IO.selectFromOptions(
            games, 
            "No " + filters[filterIndex] + "s to show!"
        );
        if (game == null) {
            return;
        }

        game.delete();
    }

    private void acceptContract(List<Contract> contracts) throws SQLException {
        Contract contract = IO.selectFromOptions(
            contracts, 
            "No pending contracts to show!"
        );
        if (contract == null) {
            return;
        }
        contract.update("status", "accepted", "string");
    }

    private void editContract(List<Contract> contracts) throws SQLException {
        Contract contract = IO.selectFromOptions(
            contracts, 
            "No pending contracts to show!"
        );
        if (contract == null) {
            return;
        }

        IO.printLogo();
        System.out.println("< Contract >");
        System.out.println("Developer: " + contract.getDeveloperUsername());
        System.out.println("Start date: " + contract.getStartDate());
        System.out.println("End date: " + contract.getEndDate() + '\n');
        String defaultEndDate = FORMAT.addOneYear(contract.getStartDate());
        String newEndDate = IO.getUserInput("New end date (default " + defaultEndDate + "): ");

        if (newEndDate.isEmpty()) {
            newEndDate = defaultEndDate;
        }

        if (newEndDate.compareTo(contract.getStartDate()) < 0) {
            IO.pauseOutput("End date must be after the start date");
            return;
        }

        if (!newEndDate.matches(DATE_REGEX)) {
            System.out.println("Invalid date format.");
            return;
        }

        contract.update("end_date", newEndDate, "date");
        IO.pauseOutput("Successfully updated the end date!");
    }

    private void cancelContract(List<Contract> contracts) throws SQLException {
        Contract contract = IO.selectFromOptions(
            contracts, 
            "No pending contracts to show!"
        );
        if (contract == null) {
            return;
        }

        contract.delete();
    }

    private void nullifyContract(List<Contract> contracts) throws SQLException {
        Contract contract = IO.selectFromOptions(
            contracts, 
            "No accepted contracts to show!"
        );
        if (contract == null) {
            return;
        }

        contract.delete();
    }


    // Menus
    private void gamesMenuPublisher() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Show published " + filters[filterIndex] + "s\n", 
            "Show unpublished " + filters[filterIndex] + "s\n", 
            "Publish a " + filters[filterIndex] + '\n', 
            "Edit a " + filters[filterIndex] + '\n', 
            "Delist a " + filters[filterIndex] + '\n', 
            "Relist a " + filters[filterIndex] + '\n', 
            "Remove a " + filters[filterIndex] + '\n', 
            "Change filter\n",
            "Change sorting criteria (" + sortOptions[sortIndex] + ")\n",
            "Go back\n"
        };
        PublisherGames option = PublisherGames.values()[IO.getValidInput(options)];

        if (option == PublisherGames.GO_BACK) {
            return;
        }
        if (option == PublisherGames.CHANGE_FILTER) {
            filterIndex = (filterIndex + 1) % filters.length;
            gamesMenuPublisher();
            return;
        }
        if (option == PublisherGames.CHANGE_SORT) {
            sortIndex = (sortIndex + 1) % sortOptions.length;
            gamesMenuPublisher();
            return;
        }

        IO.printLogo();
        List<Game> games = Game.filterPublishedBy(
            new ArrayList<>(DB.getGames().values()), 
            this.getID()
        );
        games = Game.sortByCriteria(games, sortOptions[sortIndex]);

        if (option == PublisherGames.SHOW_ALL) {
            games.addAll(Game.filterContractedUnpublished(this.getID()));
            IO.printItems(
                games, 
                "No games to show!"
            );
            gamesMenuPublisher();
            return;
        }

        games = Game.filterByType(games, filters[filterIndex]);
        if (option == PublisherGames.SHOW_PUBLISHED) {
            IO.printItems(
                games, 
                "No " + filters[filterIndex] + "s to show!"
            );
        }
        else if (option == PublisherGames.SHOW_UNPUBLISHED) {
            IO.printItems(
                Game.filterByType(
                    Game.filterContractedUnpublished(this.getID()),
                    filters[filterIndex]
                ),
                "No " + filters[filterIndex] + "s to show!"
            );
        }
        else if (option == PublisherGames.PUBLISH) {
            publishGame(
                Game.filterByType(
                    Game.filterContractedUnpublished(this.getID()), 
                    filters[filterIndex]
                )
            );
        }
        else if (option == PublisherGames.EDIT) {
            editGame(games);
        }
        else if (option == PublisherGames.DELIST) {
            listGame(games, true);
        }
        else if (option == PublisherGames.RELIST) {
            listGame(games, false);
        }
        else if (option == PublisherGames.REMOVE) {
            removeGame(games);
        }

        gamesMenuPublisher();
    }

    private void gamesMenuDeveloper() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Show published " + filters[filterIndex] + "s\n", 
            "Show unpublished " + filters[filterIndex] + "s\n", 
            "Develop a " + filters[filterIndex] + '\n', 
            "Publish a " + filters[filterIndex] + '\n', 
            "Edit a " + filters[filterIndex] + '\n', 
            "Delist a " + filters[filterIndex] + '\n', 
            "Relist a " + filters[filterIndex] + '\n', 
            "Remove a " + filters[filterIndex] + '\n', 
            "Change filter\n",
            "Change sorting criteria (" + sortOptions[sortIndex] + ")\n",
            "Go back\n"
        };
        DeveloperGames option = DeveloperGames.values()[IO.getValidInput(options)];

        if (option == DeveloperGames.GO_BACK) {
            return;
        }
        if (option == DeveloperGames.CHANGE_FILTER) {
            filterIndex = (filterIndex + 1) % filters.length;
            gamesMenuDeveloper();
            return;
        }
        if (option == DeveloperGames.CHANGE_SORT) {
            sortIndex = (sortIndex + 1) % sortOptions.length;
            gamesMenuDeveloper();
            return;
        }

        IO.printLogo();
        List<Game> games = new ArrayList<>(DB.getGames().values());
        games = Game.sortByCriteria(games, sortOptions[sortIndex]);

        if (option == DeveloperGames.SHOW_ALL) {
            IO.printItems(
                Game.filterDeveloperAll(games, this.getID()), 
                "No " + filters[filterIndex] + "s to show!"
            );
            gamesMenuDeveloper();
            return;
        }

        games = Game.filterByType(games, filters[filterIndex]);
        if (option == DeveloperGames.SHOW_PUBLISHED) {
            IO.printItems(
                Game.filterDeveloperPublished(games, this.getID()), 
                "No " + filters[filterIndex] + "s to show!"
            );
        }
        else if (option == DeveloperGames.SHOW_UNPUBLISHED) {
            IO.printItems(
                Game.filterByType(
                    Game.filterDeveloperUnpublished(games, this.getID()), 
                    filters[filterIndex]
                ),
                "No " + filters[filterIndex] + "s to show!"
            );
        }
        else if (option == DeveloperGames.DEVELOP) {
            if (filters[filterIndex].equals("game")) {
                Game game = Game.getFromInput(this.getID(), "game");
                if (game == null) {
                    gamesMenuDeveloper();
                    return;
                }
                game.create();
            }
            else {
                DLC dlc = DLC.getFromInput(this.getID());
                if (dlc == null) {
                    gamesMenuDeveloper();
                    return;
                }
                dlc.create();
            }
        }
        else if (option == DeveloperGames.PUBLISH) {
            publishGame(
                Game.filterByType(
                    Game.filterDeveloperUnpublished(
                        games, 
                        this.getID()
                    ),
                    filters[filterIndex]
                )
            );
        }
        else if (option == DeveloperGames.EDIT) {
            editGame(Game.filterDeveloperPrivileged(games, this.getID()));
        }
        else if (option == DeveloperGames.DELIST) {
            listGame(Game.filterPublishedBy(games, this.getID()), true);
        }
        else if (option == DeveloperGames.RELIST) {
            listGame(Game.filterPublishedBy(games, this.getID()), false);
        }
        else if (option == DeveloperGames.REMOVE) {
            removeGame(Game.filterDeveloperPrivileged(games, this.getID()));
        }

        gamesMenuDeveloper();
    }

    private void gamesMenu() {
        try {
            if (this.typeProvider.equals("publisher")) {
                gamesMenuPublisher();
            }
            else {
                gamesMenuDeveloper();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void contractsMenuPublisher() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Issue to developer\n", 
            "Edit pending\n", 
            "Cancel pending\n", 
            "Nullify approved\n", 
            "Go back\n"
        };
        PublisherContracts option = PublisherContracts.values()[IO.getValidInput(options)];

        if (option == PublisherContracts.GO_BACK) {
            return;
        }

        IO.printLogo();
        List<Contract> contracts = Contract.filterByProvider(new ArrayList<>(DB.getContracts().values()), this.getID());
        if (option == PublisherContracts.SHOW_ALL) {
            IO.printItems(
                contracts, 
                "No contracts to show!"
            );
        }
        else if (option == PublisherContracts.ISSUE)
        {
            Contract contract = Contract.getFromInput(this.getID());
            if (contract == null) {
                contractsMenuPublisher();
                return;
            }
            contract.create();
        }
        else if (option == PublisherContracts.EDIT) {
            editContract(Contract.filterByStatus(contracts, "pending"));
        }
        else if (option == PublisherContracts.CANCEL) {
            cancelContract(Contract.filterByStatus(contracts, "pending"));
        }
        else if (option == PublisherContracts.NULLIFY) {
            nullifyContract(Contract.filterByStatus(contracts, "accepted"));
        }

        contractsMenuPublisher();
    }

    private void contractsMenuDeveloper() throws SQLException {
        String[] options = {
            "Show all\n", 
            "Accept pending\n", 
            "Reject pending\n", 
            "Go back\n"
        };
        DeveloperContracts option = DeveloperContracts.values()[IO.getValidInput(options)];

        if (option == DeveloperContracts.GO_BACK) {
            return;
        }

        IO.printLogo();
        List<Contract> contracts = Contract.filterByProvider(new ArrayList<>(DB.getContracts().values()), this.getID());
        if (option == DeveloperContracts.SHOW_ALL) {
            IO.printItems(
                contracts, 
                "No contracts to show!"
            );
        }
        else if (option == DeveloperContracts.ACCEPT) {
            acceptContract(Contract.filterByStatus(contracts, "pending"));
        }
        else if (option == DeveloperContracts.REJECT) {
            cancelContract(Contract.filterByStatus(contracts, "pending"));
        }

        contractsMenuDeveloper();
    }

    private void contractsMenu() {
        try {
            if (this.typeProvider.equals("publisher")) {
                contractsMenuPublisher();
            }
            else {
                contractsMenuDeveloper();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editMenu() throws SQLException {
        String[] options = {
            "Edit username\n", 
            "Edit password\n", 
            "Edit email\n", 
            "Edit website\n", 
            "Go back\n"
        };
        ProviderEdit option = ProviderEdit.values()[IO.getValidInput(options)];

        if (option == ProviderEdit.GO_BACK) {
            profileMenu(this::editMenu);
            return;
        }
        if (option == ProviderEdit.EDIT_USERNAME) {
            editUsername();
        }
        else if (option == ProviderEdit.EDIT_PASSWORD) {
            editPassword();
        }
        else if (option == ProviderEdit.EDIT_EMAIL) {
            editEmail();
        }
        else if (option == ProviderEdit.EDIT_WEBSITE) {
            editWebsite();
        }

        editMenu();
    }

    @Override
    public void menu() throws SQLException {
        String[] options = {
            "Manage games\n", 
            "Contracts\n", 
            "Profile\n", 
            "Log out\n"
        };
        ProviderMenu option = ProviderMenu.values()[IO.getValidInput(options)];

        if (option == ProviderMenu.LOG_OUT) {
            return;
        }

        IO.printLogo();
        resetMenu();

        if (option == ProviderMenu.MANAGE_GAMES) {
            gamesMenu();
        }
        else if (option == ProviderMenu.CONTRACTS) {
            contractsMenu();
        }
        else if (option == ProviderMenu.PROFILE) {
            profileMenu(this::editMenu);
        }

        menu();
    }
}
