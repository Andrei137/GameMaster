package models;

import static misc.Constants.AUDIT;
import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import static misc.Constants.DATE;
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
import org.apache.commons.lang3.tuple.Pair;

public final class Purchase implements Crud<Purchase>, Formattable {
    // Fields
    private Integer gameID;
    private Integer userID;
    private String purchaseDate;
    private Double price;


    // Constructors
    public Purchase(Integer gameID,
                    Integer userID,
                    String purchaseDate,
                    Double price) {
        this.gameID = gameID;
        this.userID = userID;
        this.purchaseDate = purchaseDate;
        this.price = price;
    }

    public Purchase(Purchase purchase) {
        this.gameID = purchase.gameID;
        this.userID = purchase.userID;
        this.purchaseDate = purchase.purchaseDate;
        this.price = purchase.price;
    }


    // Important methods
    @Override
    public String toString() {
        return "Game: " + getGameName() +
               " \nPurchase date: " + purchaseDate +
               " \nPrice: " + (price.equals(0.0) ? "Free" : price + " RON") + '\n';
    }

    @Override
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override 
    public Purchase clone() {
        return new Purchase(this);
    }


    // Getters
    public Pair<Integer, Integer> getID() {
        return Pair.of(gameID, userID);
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getGameName() {
        return DB.getGames().get(gameID).getName();
    }

    public String getGamePrice() {
        return DB.getGames().get(gameID).getPrice().toString();
    }

    public String getGameReleaseDate() {
        return DB.getGames().get(gameID).getReleaseDate();
    }

    public Integer getUserID() {
        return userID;
    }

    public String getUsername() {
        return DB.getUsers().get(userID).getUsername();
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public Double getPrice() {
        return price;
    }

    public static Map<Pair<Integer, Integer>, Purchase> getPurchases() throws SQLException {
        Map<Pair<Integer, Integer>, Purchase> purchases = new HashMap<>();

        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT * FROM purchase"
            );

        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            purchases.put(
                Pair.of(rs.getInt("game_id"), rs.getInt("user_id")),
                new Purchase(
                    rs.getInt("game_id"),
                    rs.getInt("user_id"),
                    FORMAT.dateForClass(rs.getDate("purchase_date").toString()),
                    rs.getDouble("price")
                )
            );
        }
        return purchases;
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO purchase VALUES (?, ?, ?, ?)"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);
        statement.setDate(3, FORMAT.dateForDB(purchaseDate));
        statement.setDouble(4, price);

        DB.modifyPurchases(this, "create");

        return statement.executeUpdate();
    }

    @Override
    public Purchase read() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT *" +
                "\nFROM purchase" +
                "\nWHERE game_id = ? AND user_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);

        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new Purchase(
                rs.getInt("game_id"),
                rs.getInt("user_id"),
                FORMAT.dateForClass(rs.getDate("purchase_date").toString()),
                rs.getDouble("price"));
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
                "DELETE FROM purchase" +
                "\nWHERE game_id = ? AND user_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);

        DB.modifyPurchases(this, "delete");

        int result = statement.executeUpdate();

        AUDIT.writeToLibrariesCSV(userID);

        return result;
    }


    // Filters
    public static List<Purchase> filterByUser(List<Purchase> purchases, Integer userID) {
        Predicate<Purchase> condition = purchase -> purchase.getUserID().equals(userID);
        return HelperService.filterByCondition(purchases, condition);
    }

    public static List<Purchase> filterByType(List<Purchase> purchases, String type) {
        Map<Integer, Game> games = DB.getGames();
        Predicate<Purchase> condition = purchase ->
            games.get(purchase.getGameID()).getType().equals(type);
        return HelperService.filterByCondition(purchases, condition);
    }


    // Sorters
    private static List<Purchase> sortByName(List<Purchase> purchases) {
        return HelperService.sortByCriteria(
            purchases, 
            (purchase1, purchase2) -> purchase1.getGameName().compareTo(purchase2.getGameName())
        );
    }

    private static List<Purchase> sortByPrice(List<Purchase> purchases) {
        return HelperService.sortByCriteria(
            purchases, 
            (purchase1, purchase2) -> purchase1.getPrice().compareTo(purchase2.getPrice())
        );
    }

    private static List<Purchase> sortByReleaseDate(List<Purchase> purchases) {
        return HelperService.sortByCriteria(
            purchases, 
            (purchase1, purchase2) -> purchase1.getGameReleaseDate().compareTo(purchase2.getGameReleaseDate())
        );
    }

    public static List<Purchase> sortByCriteria(List<Purchase> purchases, String criteria) {
        switch (criteria) {
            case "Name":
                return sortByName(purchases);
            case "Price":
                return sortByPrice(purchases);
            case "Release date":
                return sortByReleaseDate(purchases);
            default:
                return purchases;
        }
    }
}
