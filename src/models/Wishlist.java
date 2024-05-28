package models;

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

public final class Wishlist implements Crud<Wishlist>, Formattable {
    // Fields
    private Integer gameID;
    private Integer userID;
    private String addedDate;


    // Constructors
    public Wishlist(Integer gameID,
                    Integer userID,
                    String addedDate) {
        this.gameID = gameID;
        this.userID = userID;
        this.addedDate = addedDate;
    }

    public Wishlist(Wishlist wishlist) {
        this.gameID = wishlist.gameID;
        this.userID = wishlist.userID;
        this.addedDate = wishlist.addedDate;
    }


    // Important methods
    @Override
    public String toString() {
        return "Game: " + getGameName() +
               " \nAdded date: " + addedDate +
               (getPublished() ? " \nPrice: " + getPrice() : " \nUnpublished") + '\n';
    }

    @Override
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override 
    public Wishlist clone() {
        return new Wishlist(this);
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

    public String getAddedDate() {
        return addedDate;
    }

    public Double getPrice() {
        return DB.getGames().get(gameID).getPrice();
    }

    public Boolean getPublished() {
        return DB.getGames().get(gameID).getPublished();
    }

    public static Map<Pair<Integer, Integer>, Wishlist> getWishlists() throws SQLException {
        Map<Pair<Integer, Integer>, Wishlist> wishlists = new HashMap<>();

        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT * FROM wishlist"
            );

        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            wishlists.put(
                Pair.of(rs.getInt("game_id"), rs.getInt("user_id")),
                new Wishlist(
                    rs.getInt("game_id"),
                    rs.getInt("user_id"),
                    FORMAT.dateForClass(rs.getDate("added_date").toString())
                )
            );
        }
        return wishlists;
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO wishlist VALUES (?, ?, ?)"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);
        statement.setDate(3, FORMAT.dateForDB(addedDate));

        DB.modifyWishlists(this, "create");

        return statement.executeUpdate();
    }

    @Override
    public Wishlist read() throws SQLException {
        PreparedStatement statement = DB.getConnection()
            .prepareStatement(
                "SELECT *"  +
                "\nFROM wishlist" +
                "\nWHERE game_id = ? AND user_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);

        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new Wishlist(
                rs.getInt("game_id"),
                rs.getInt("user_id"),
                FORMAT.dateForClass(rs.getDate("added_date").toString())
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
                "DELETE FROM wishlist" +
                "\nWHERE game_id = ? AND user_id = ?"
            );
        statement.setInt(1, gameID);
        statement.setInt(2, userID);

        DB.modifyWishlists(this, "delete");

        return statement.executeUpdate();
    }


    // Filters
    public static List<Wishlist> filterByUser(List<Wishlist> wishlists, Integer userID) {
        Predicate<Wishlist> condition = wishlist -> wishlist.getUserID().equals(userID);
        return HelperService.filterByCondition(wishlists, condition);
    }

    public static List<Wishlist> filterByPublishedStatus(List<Wishlist> wishlists, Boolean isPublished) {
        Predicate<Wishlist> condition = wishlist -> 
            wishlist.getPublished().equals(isPublished);
        return HelperService.filterByCondition(wishlists, condition);
    }

    public static List<Wishlist> filterByType(List<Wishlist> wishlists, String type) {
        Map<Integer, Game> games = DB.getGames();
        Predicate<Wishlist> condition = wishlist ->
            games.get(wishlist.getGameID()).getType().equals(type);
        return HelperService.filterByCondition(wishlists, condition);
    }


    // Sorters
    private static List<Wishlist> sortByName(List<Wishlist> wishlists) {
        return HelperService.sortByCriteria(
            wishlists, 
            (wishlist1, wishlist2) -> wishlist1.getGameName().compareTo(wishlist2.getGameName())
        );
    }

    private static List<Wishlist> sortByPrice(List<Wishlist> wishlists) {
        return HelperService.sortByCriteria(
            wishlists, 
            (wishlist1, wishlist2) -> wishlist1.getPrice().compareTo(wishlist2.getPrice())
        );
    }

    private static List<Wishlist> sortByReleaseDate(List<Wishlist> wishlists) {
        return HelperService.sortByCriteria(
            wishlists, 
            (wishlist1, wishlist2) -> wishlist1.getGameReleaseDate().compareTo(wishlist2.getGameReleaseDate())
        );
    }

    public static List<Wishlist> sortByCriteria(List<Wishlist> wishlists, String criteria) {
        switch (criteria) {
            case "Name":
                return sortByName(wishlists);
            case "Price":
                return sortByPrice(wishlists);
            case "Release date":
                return sortByReleaseDate(wishlists);
            default:
                return wishlists;
        }
    }
}
