package models;

import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import exceptions.InvalidFieldException;
import exceptions.UniqueFieldException;
import interfaces.Crud;
import interfaces.Formattable;
import services.HelperService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Predicate;

public final class Game implements Crud<Game>, Formattable {
    // Fields
    private Integer ID;
    private Integer developerID;
    private Integer publisherID;
    private String name;
    private Double price;
    private String releaseDate;
    private Boolean isVisible;
    private String type;

    // Other members
    public static Integer nextID = 0;


    // Constructors
    public Game(Integer ID, 
                Integer developerID, 
                Integer publisherID, 
                String name, 
                Double price, 
                String releaseDate, 
                Boolean isVisible, 
                String type) {
        this.ID = ID;
        this.developerID = developerID;
        this.publisherID = publisherID;
        this.name = name;
        this.price = price;
        this.releaseDate = releaseDate;
        this.isVisible = isVisible;
        this.type = type;

        nextID = Integer.max(nextID, ID);
    }

    public Game(Game game) {
        this.ID = game.getID();
        this.developerID = game.getDeveloperID();
        this.publisherID = game.getPublisherID();
        this.name = game.getName();
        this.price = game.getPrice();
        this.releaseDate = game.getReleaseDate();
        this.isVisible = game.getIsVisible();
        this.type = game.getType();
    }


    // Important methods
    @Override
    public String toString() {
        return "Name: " + name + (type.equals("dlc") ? " DLC" : "") +
               (type == "dlc" ? " \nBase game: " + DB.getDLCs().get(ID).getBaseGameName() : "") +
               (publisherID != 0 ? " \nPrice: " + (price.equals(0.0) ? "Free" : price + " RON") : "") +
               (publisherID != 0 ? " \nRelease date: " + releaseDate : "") +
               " \nDeveloped by " + getDeveloperUsername() +
               (publisherID != 0 ? " \nPublished by " + getPublisherUsername() : " \nUnplublished") +
               (isVisible ? "" : " \nDelisted") + '\n';
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public Game clone() {
        return new Game(this);
    }


    // Getters
    public Integer getID() {
        return ID;
    }

    public Integer getDeveloperID() {
        return developerID;
    }

    public String getDeveloperUsername() {
        return DB.getDevelopers().get(developerID).getUsername();
    }

    public Integer getPublisherID() {
        return publisherID;
    }

    public String getPublisherUsername() {
        return DB.getProviders().get(publisherID).getUsername();
    }

    public Boolean getPublished() {
        return publisherID != 0;
    }

    public String getName() {
        return name + (type.equals("dlc") ? " DLC" : "");
    }

    public Double getPrice() {
        return price;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public String getType() {
        return type;
    }

    public static Map<Integer, Game> getGames() throws SQLException {
        Map<Integer, Game> games = new HashMap<>();

        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT * FROM game"
            );
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String releaseDate = rs.getDate("release_date") == null 
                ? null 
                : FORMAT.dateForClass(rs.getDate("release_date").toString());
            Double price = rs.getString("price") == null
                ? 0.00
                : rs.getDouble("price");

            games.put(
                rs.getInt("game_id"), 
                new Game(
                    rs.getInt("game_id"),
                    rs.getInt("developer_id"),
                    rs.getInt("publisher_id"),
                    rs.getString("name"),
                    price,
                    releaseDate,
                    rs.getBoolean("is_visible"),
                    rs.getString("type")
                )
            );
        }
        return games;
    }

    public static Game getFromInput(Integer developerID, String type) {
        IO.printLogo();

        HelperService.Validator nameValidator = value -> {
            if (value.isEmpty()) {
                throw new InvalidFieldException("The name cannot be empty.");
            }
            Map<Integer, Game> games = DB.getGames();
            for (Game game : games.values()) {
                if (game.getName().toLowerCase().equals(value.toLowerCase())) {
                    throw new UniqueFieldException("The name " + value + " is already taken.");
                }
            }
            return true;
        };

        String name = HelperService.getInput(
            nameValidator, 
            "Name: "
        );
        if (name == null) {
            return null;
        }

        return new Game(
            Game.nextID + 1, 
            developerID, 
            0, 
            name, 
            0.00, 
            null, 
            true, 
            type
        );
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO game VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.setInt(2, developerID);
        if (publisherID == 0) {
            preparedStatement.setNull(3, java.sql.Types.INTEGER);
        } else {
            preparedStatement.setInt(3, publisherID);
        }
        preparedStatement.setString(4, name);
        preparedStatement.setDouble(5, price);
        if (releaseDate == null) {
            preparedStatement.setNull(6, java.sql.Types.DATE);
        }
        else {
            preparedStatement.setDate(6, FORMAT.dateForDB(releaseDate));
        }
        preparedStatement.setBoolean(7, isVisible);
        preparedStatement.setString(8, type);

        DB.modifyGames(this, "create");

        return preparedStatement.executeUpdate();
    }

    @Override 
    public Game read() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT * " +
                "\nFROM game" + 
                "\nWHERE game_id = ?"
            );
        preparedStatement.setInt(1, ID);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            String releaseDate = rs.getDate("release_date") == null 
                ? null 
                : FORMAT.dateForClass(rs.getDate("release_date").toString());
            Double price = rs.getString("price") == null
                ? 0.00
                : rs.getDouble("price");

            return new Game(
                rs.getInt("game_id"),
                rs.getInt("developer_id"),
                rs.getInt("publisher_id"),
                rs.getString("name"),
                price,
                releaseDate,
                rs.getBoolean("is_visible"),
                rs.getString("type")
            );
        }

        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "UPDATE game" +
                "\nSET " + column + " = ?" +
                "\nWHERE game_id = ?"
            );

        if (type.equals("date")) {
            preparedStatement.setDate(1, FORMAT.dateForDB(value));
        } 
        else if (type.equals("boolean")) {
            preparedStatement.setBoolean(1, Boolean.parseBoolean(value));
        }
        else if (type.equals("double")) {
            preparedStatement.setDouble(1, Double.parseDouble(value));
        }
        else {
            preparedStatement.setString(1, value);
        }
        preparedStatement.setInt(2, ID);

        int response = preparedStatement.executeUpdate();

        DB.modifyGames(this.read(), "update");

        return response;
    }

    @Override
    public int delete() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM purchase" +
                "\nWHERE game_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM wishlist" +
                "\nWHERE game_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM dlc" +
                "\nWHERE game_id = ? OR base_game_id = ?"
            );
        preparedStatement.setInt(1, ID);
        preparedStatement.setInt(2, ID);
        preparedStatement.executeUpdate();

        preparedStatement = DB.getConnection()
            .prepareStatement("DELETE FROM game"
                              + "\nWHERE game_id = ?");
        preparedStatement.setInt(1, ID);

        DB.modifyGames(this.read(), "delete");

        return preparedStatement.executeUpdate();
    }


    // Filters
    public static List<Game> filterByType(List<Game> games, String type) {
        Predicate<Game> condition = game -> game.getType().equals(type);
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterByVisibility(List<Game> games, Boolean isVisible) {
        Predicate<Game> condition = game -> game.getIsVisible().equals(isVisible);
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterDeveloperAll(List<Game> games, Integer developerID) {
        Predicate<Game> condition = game -> game.getDeveloperID().equals(developerID);
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterDeveloperPrivileged(List<Game> games, Integer developerID) {
        Predicate<Game> condition = game -> 
            (game.getDeveloperID().equals(developerID) && game.getPublisherID().equals(0)) ||
            (game.getPublisherID().equals(developerID));
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterDeveloperUnpublished(List<Game> games, Integer developerID) {
        Predicate<Game> condition = game -> game.getDeveloperID().equals(developerID) && game.getPublisherID().equals(0);
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterDeveloperPublished(List<Game> games, Integer developerID) {
        Predicate<Game> condition = game -> game.getDeveloperID().equals(developerID) && game.getPublisherID() != 0;
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterPublishedBy(List<Game> games, Integer publisherId) {
        Predicate<Game> condition = game -> game.getPublisherID().equals(publisherId);
        return HelperService.filterByCondition(games, condition);
    }

    public static List<Game> filterContractedUnpublished(Integer publisherID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT game_id, game.developer_id, name, price, release_date, is_visible, type" +
                "\nFROM game" +
                "\nJOIN provider ON game.developer_id = provider.user_id" + 
                "\nJOIN contract ON contract.developer_id = provider.user_id" +
                "\nWHERE game.publisher_id IS NULL AND contract.publisher_id = ?"
            );
        preparedStatement.setInt(1, publisherID);

        ResultSet rs = preparedStatement.executeQuery();
        List<Game> games = new ArrayList<>();
        while (rs.next()) {
            String releaseDate = rs.getDate("release_date") == null 
                ? null 
                : FORMAT.dateForClass(rs.getDate("release_date").toString());
            Double price = rs.getString("price") == null
                ? 0.00
                : rs.getDouble("price");

            games.add(new Game(
                rs.getInt("game_id"),
                rs.getInt("developer_id"),
                0,
                rs.getString("name"),
                price,
                releaseDate,
                rs.getBoolean("is_visible"),
                rs.getString("type")
            ));
        }

        return games;
    }

    public static List<Game> filterDisowned(Integer userID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT *" +
                "\nFROM game" +
                "\nWHERE type = 'game' AND publisher_id IS NOT NULL AND is_visible = 1 AND game_id NOT IN (" + 
                "\n    SELECT game_id" +
                "\n    FROM purchase" +
                "\n    WHERE user_id = ?)"
            );
        preparedStatement.setInt(1, userID);

        ResultSet rs = preparedStatement.executeQuery();
        List<Game> games = new ArrayList<>();
        while (rs.next()) {
            String releaseDate = rs.getDate("release_date") == null 
                ? null 
                : FORMAT.dateForClass(rs.getDate("release_date").toString());
            Double price = rs.getString("price") == null
                ? 0.00
                : rs.getDouble("price");

            games.add(new Game(
                rs.getInt("game_id"),
                rs.getInt("developer_id"),
                rs.getInt("publisher_id"),
                rs.getString("name"),
                price,
                releaseDate,
                rs.getBoolean("is_visible"),
                rs.getString("type")
            ));
        }

        return games;
    }

    public static List<Game> filterNotInWishlist(Integer userID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT *" +
                "\nFROM game" +
                "\nWHERE type = 'game' AND is_visible = 1 AND game_id NOT IN (" + 
                "\n    SELECT game_id" +
                "\n    FROM wishlist" +
                "\n    WHERE user_id = ?)" +
                "\n AND game_id NOT IN (" +
                "\n    SELECT game_id" +
                "\n    FROM purchase" +
                "\n    WHERE user_id = ?)"
            );
        preparedStatement.setInt(1, userID);
        preparedStatement.setInt(2, userID);

        ResultSet rs = preparedStatement.executeQuery();
        List<Game> games = new ArrayList<>();
        while (rs.next()) {
            String releaseDate = rs.getDate("release_date") == null 
                ? null 
                : FORMAT.dateForClass(rs.getDate("release_date").toString());
            Double price = rs.getString("price") == null
                ? 0.00
                : rs.getDouble("price");

            games.add(new Game(
                rs.getInt("game_id"),
                rs.getInt("developer_id"),
                rs.getInt("publisher_id"),
                rs.getString("name"),
                price,
                releaseDate,
                rs.getBoolean("is_visible"),
                rs.getString("type")
            ));
        }

        return games;
    }


    // Sorters
    private static List<Game> sortByName(List<Game> games) {
        return HelperService.sortByCriteria(
            games, 
            (game1, game2) -> game1.getName().compareTo(game2.getName())
        );
    }

    private static List<Game> sortByPrice(List<Game> games) {
        return HelperService.sortByCriteria(
            games, 
            (game1, game2) -> game1.getPrice().compareTo(game2.getPrice())
        );
    }

    private static List<Game> sortByReleaseDate(List<Game> games) {
        return HelperService.sortByCriteria(
            games, 
            (game1, game2) -> game1.getReleaseDate().compareTo(game2.getReleaseDate())
        );
    }

    public static List<Game> sortByCriteria(List<Game> games, String criteria) {
        switch (criteria) {
            case "Name":
                return sortByName(games);
            case "Price":
                return sortByPrice(games);
            case "Release date":
                return sortByReleaseDate(games);
            default:
                return games;
        }
    }
}
