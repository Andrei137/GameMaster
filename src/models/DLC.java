package models;

import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import interfaces.Crud;
import interfaces.Formattable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public final class DLC implements Crud<DLC>, Formattable {
    // Fields
    private Integer gameID;
    private Integer baseGameID;


    // Constructors
    public DLC(Integer gameID,
               Integer baseGameID) {
        this.gameID = gameID;
        this.baseGameID = baseGameID;
    }

    public DLC(DLC dlc) {
        this.gameID = dlc.gameID;
        this.baseGameID = dlc.baseGameID;
    }


    // Important methods
    @Override
    public String toString() {
        return getGameName() + " DLC";
    }

    @Override
    public int hashCode() {
        return this.getID();
    }

    @Override 
    public DLC clone() {
        return new DLC(this);
    }


    // Getters
    public Integer getID() {
        return gameID;
    }

    public String getGameName() {
        return DB.getGames().get(gameID).getName();
    }

    public Integer getBaseGameID() {
        return baseGameID;
    }

    public String getBaseGameName() {
        return DB.getGames().get(baseGameID).getName();
    }

    public static Map<Integer, DLC> getDLCs() throws SQLException {
        Map<Integer, DLC> DLCs = new HashMap<>();

        PreparedStatement statement = DB.getConnection().
            prepareStatement(
                "SELECT * FROM dlc"
            );

        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            DLCs.put(
                rs.getInt("game_id"),
                new DLC(
                    rs.getInt("game_id"),
                    rs.getInt("base_game_id")
                )
            );
        }
        return DLCs;
    }

    public static DLC getFromInput(Integer developerID) throws SQLException {
        List<Game> baseGames = Game.filterByType(
            Game.filterDeveloperAll(
                new ArrayList<>(DB.getGames().values()),
                developerID
            ),
            "game"
        );
        Game baseGame = IO.selectFromOptions(
            baseGames, 
            "No games available!"
        );
        if (baseGame == null) {
            return null;
        }

        Game dlc = Game.getFromInput(developerID, "dlc");
        if (dlc == null) {
            return null;
        }
        dlc.create();

        return new DLC(
            dlc.getID(), 
            baseGame.getID()
        );
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO dlc VALUES (?, ?)"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, baseGameID);

        DB.modifyDLCs(this, "create");

        return statement.executeUpdate();
    }

    @Override
    public DLC read() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT *" +
                "\nFROM dlc" +
                "\nWHERE game_id = ? AND base_game_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, baseGameID);

        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new DLC(
                rs.getInt("game_id"),
                rs.getInt("base_game_id")
            );
        }
        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        return 0;
    }

    @Override 
    public int delete() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM dlc" +
                "\nWHERE game_id = ? AND base_game_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, baseGameID);

        DB.modifyDLCs(this, "delete");

        return statement.executeUpdate();
    }


    // Filters
    public static List<Game> filterDisowned(Integer userID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT dlc.game_id, developer_id, publisher_id, name, price, release_date, is_visible" +
                "\nFROM game" +
                "\nJOIN dlc ON game.game_id = dlc.game_id" +
                "\nWHERE type = 'dlc' AND publisher_id IS NOT NULL AND is_visible = 1" + 
                "\nAND dlc.game_id NOT IN (" +
                "\n    SELECT game_id" +
                "\n    FROM purchase" +
                "\n    WHERE user_id = ?)" +
                "\nAND base_game_id IN (" +
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
                "dlc"
            ));
        }

        return games;
    }

    public static List<Game> filterNotInWishlist(Integer userID) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT dlc.game_id, developer_id, publisher_id, name, price, release_date, is_visible" +
                "\nFROM game" +
                "\nJOIN dlc ON game.game_id = dlc.game_id" +
                "\nWHERE type = 'dlc' AND is_visible = 1" + 
                "\nAND dlc.game_id NOT IN (" +
                "\n    SELECT game_id" +
                "\n    FROM wishlist" +
                "\n    WHERE user_id = ?)" +
                "\nAND dlc.game_id NOT IN (" +
                "\n    SELECT game_id" +
                "\n    FROM purchase" +
                "\n    WHERE user_id = ?)" +
                "\nAND base_game_id IN (" +
                "\n    SELECT game_id" +
                "\n    FROM purchase" +
                "\n    WHERE user_id = ?)"
            );
        preparedStatement.setInt(1, userID);
        preparedStatement.setInt(2, userID);
        preparedStatement.setInt(3, userID);

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
                "dlc"
            ));
        }

        return games;
    }
}
