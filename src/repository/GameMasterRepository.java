package repository;

import static misc.Constants.DATABASE_URL;
import static misc.Constants.DATABASE_USER;
import static misc.Constants.DATABASE_PASSWORD;
import static misc.Constants.AUDIT;
import models.*;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.lang3.tuple.Pair;

public final class GameMasterRepository {
    // Singleton instance
    private static GameMasterRepository INSTANCE = null;


    // Database members
    private static Connection connection = null;


    // Other members
    private static Map<Integer, Client> clients = new HashMap<>();
    private static Map<Integer, Provider> providers = new HashMap<>();
    private static Map<Integer, Admin> admins = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Contract> contracts = new HashMap<>();
    private static Map<Integer, Game> games = new HashMap<>();
    private static Map<Integer, DLC> DLCs = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Purchase> purchases = new HashMap<>();
    private static Map<Pair<Integer, Integer>, Wishlist> wishlists = new HashMap<>();


    // Constructors
    private GameMasterRepository() {}


    // Connecting, loading and closing the database
    static {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void loadDatabase() throws SQLException {
        clients = Client.getClients();
        providers = Provider.getProviders();
        admins = Admin.getAdmins();
        contracts = Contract.getContracts();
        games = Game.getGames();
        DLCs = DLC.getDLCs();
        purchases = Purchase.getPurchases();
        wishlists = Wishlist.getWishlists();
    }


    // Getters
    public static GameMasterRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameMasterRepository();
        }

        return INSTANCE;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static Map<Integer, Client> getClients() {
        return clients;
    }

    public static Map<Integer, Provider> getProviders() {
        return providers;
    }

    public static Map<Integer, Provider> getDevelopers() {
        Map<Integer, Provider> developers = new HashMap<>();
        for (Provider provider : providers.values()) {
            if (provider.getTypeProvider().equals("developer")) {
                developers.put(provider.getID(), provider);
            }
        }
        return developers;
    }

    public static Map<Integer, Provider> getPublishers() {
        Map<Integer, Provider> publishers = new HashMap<>();
        for (Provider provider : providers.values()) {
            if (provider.getTypeProvider().equals("publisher")) {
                publishers.put(provider.getID(), provider);
            }
        }
        return publishers;
    }

    public static Map<Integer, Admin> getAdmins() {
        return admins;
    }


    public static Map<Integer, User> getUsers() {
        Map<Integer, User> users = new HashMap<>();
        users.putAll(clients);
        users.putAll(providers);
        users.putAll(admins);
        return users;
    }

    public static Map<Pair<Integer, Integer>, Contract> getContracts() {
        return contracts;
    }

    public static  Map<Integer, Game> getGames() {
        return games;
    }

    public static Map<Integer, DLC> getDLCs() {
        return DLCs;
    }

    public static Map<Pair<Integer, Integer>, Purchase> getPurchases() {
        return purchases;
    }

    public static Map<Pair<Integer, Integer>, Wishlist> getWishlists() {
        return wishlists;
    }


    // Helpers
    public static <K, T> void modifyEntities(Map<K, T> entities, T entity, K key, String action) {
        AUDIT.writeToActionsCSV(entity.getClass().getSimpleName(), action);

        if (action == "create") {
            entities.put(key, entity);
        }
        else if (action == "update") {
            entities.replace(key, entity);
        }
        else if (action == "delete") {
            entities.remove(key);
        }
    }

    public static void modifyClients(Client client, String action) throws SQLException {
        modifyEntities(clients, client, client.getID(), action);
        if (action == "delete") {
            purchases = Purchase.getPurchases();
            wishlists = Wishlist.getWishlists();
        }
    }

    public static void modifyProviders(Provider provider, String action) throws SQLException {
        modifyEntities(providers, provider, provider.getID(), action);
        if (action == "delete") {
            contracts = Contract.getContracts();
            games = Game.getGames();
        }
    }

    public static void modifyAdmins(Admin admin, String action) {
        modifyEntities(admins, admin, admin.getID(), action);
    }

    public static void modifyContracts(Contract contract, String action) {
        modifyEntities(contracts, contract, contract.getID(), action);
    }

    public static void modifyGames(Game game, String action) throws SQLException {
        modifyEntities(games, game, game.getID(), action);
        if (action == "delete") {
            clients = Client.getClients();
            DLCs = DLC.getDLCs();
            purchases = Purchase.getPurchases();
            wishlists = Wishlist.getWishlists();
        }
    }

    public static void modifyDLCs(DLC DLC, String action) {
        modifyEntities(DLCs, DLC, DLC.getID(), action);
    }

    public static void modifyPurchases(Purchase purchase, String action) {
        modifyEntities(purchases, purchase, purchase.getID(), action);
    }

    public static void modifyWishlists(Wishlist wishlist, String action) {
        modifyEntities(wishlists, wishlist, wishlist.getID(), action);
    }
}
