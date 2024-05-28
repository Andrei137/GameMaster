package models;

import static misc.Constants.DATE_REGEX;
import static misc.Constants.IO;
import static misc.Constants.FORMAT;
import static misc.Constants.DB;
import static misc.Constants.DATE;
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
import org.apache.commons.lang3.tuple.Pair;

public final class Contract implements Crud<Contract>, Formattable {
    // Fields
    private Integer developerID;
    private Integer publisherID;
    private String status;
    private String startDate;
    private String endDate;


    // Consturctors
    public Contract(Integer developerID, 
                    Integer publisherID, 
                    String status, 
                    String startDate, 
                    String endDate) {
        this.developerID = developerID;
        this.publisherID = publisherID;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Contract(Contract contract) {
        this.developerID = contract.developerID;
        this.publisherID = contract.publisherID;
        this.status = contract.status;
        this.startDate = contract.startDate;
        this.endDate = contract.endDate;
    }


    // Important methods
    @Override
    public String toString() {
        return "Developer: " + getDeveloperUsername() +
               " \nPublisher: " + getPublisherUsername() +
               " \nStatus: " + status +
               " \nStart date: " + startDate +
               " \nEnd date: " + endDate + '\n';
    }

    @Override
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override
    public Contract clone() {
        return new Contract(this);
    }


    // Getters
    public Pair<Integer, Integer> getID() {
        return Pair.of(developerID, publisherID);
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
        return DB.getPublishers().get(publisherID).getUsername();
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public static Map<Pair<Integer, Integer>, Contract> getContracts() throws SQLException {
        Map<Pair<Integer, Integer>, Contract> contracts = new HashMap<>();

        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT * FROM contract"
            );

        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            contracts.put(
                Pair.of(rs.getInt("developer_id"), rs.getInt("publisher_id")),
                new Contract(
                    rs.getInt("developer_id"),
                    rs.getInt("publisher_id"),
                    rs.getString("status"),
                    FORMAT.dateForClass(rs.getDate("start_date").toString()),
                    FORMAT.dateForClass(rs.getDate("end_date").toString())
                )
            );
        }

        return contracts;
    }

    public static Contract getFromInput(Integer publisherID) {
        IO.printLogo();

        Provider developer = IO.selectFromOptions(
            filterDevelopers(publisherID),  
            "No developers available."
        );
        if (developer == null) {
            return null;
        }

        Integer developerID = developer.getID();
        String startDate = DATE.format(new Date());
        String defaultEndDate = FORMAT.addOneYear(startDate);

        IO.printLogo();
        System.out.println("< Contract >");
        System.out.println("Developer: " + developer.getUsername());
        System.out.println("Start date: " + startDate);

        String endDate = HelperService.getInput(
            value -> {
                if (value.isEmpty()) {
                    return true;
                }
                if (value.compareTo(startDate) < 0) {
                    throw new InvalidFieldException("End date must be after the start date!");
                }
                if (!value.matches(DATE_REGEX)) {
                    throw new InvalidFieldException("Invalid date format!");
                }
                return true;
            },
            "End date (default: " + defaultEndDate + "): "
        );
        if (endDate == null) {
            return null;
        }

        return new Contract(
            developerID, 
            publisherID, 
            "pending", 
            startDate, 
            (endDate.isEmpty() ? defaultEndDate : endDate)
        );
    }


    // Helpers
    private static List<Provider> filterDevelopers(Integer publisherID) {
        Map<Integer, Provider> developers = DB.getDevelopers();
        Map<Pair<Integer, Integer>, Contract> contracts = DB.getContracts();

        List<Provider> filteredDevelopers = new ArrayList<>();
        for (Provider developer : developers.values()) {
            if (!contracts.containsKey(Pair.of(developer.getID(), publisherID))) {
                filteredDevelopers.add(developer);
            }
        }
        return filteredDevelopers;
    }


    // Crud interface
    @Override
    public int create() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "INSERT INTO contract VALUES (?, ?, ?, ?, ?)"
            );
        preparedStatement.setInt(1, developerID);
        preparedStatement.setInt(2, publisherID);
        preparedStatement.setString(3, status);
        preparedStatement.setDate(4, FORMAT.dateForDB(startDate));
        preparedStatement.setDate(5, FORMAT.dateForDB(endDate));

        DB.modifyContracts(this, "create");

        return preparedStatement.executeUpdate();
    }

    @Override 
    public Contract read() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "SELECT *" +
                "\nFROM contract" +
                "\nWHERE developer_id = ? AND publisher_id = ?"
            );
        preparedStatement.setInt(1, developerID);
        preparedStatement.setInt(2, publisherID);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            return new Contract(
                rs.getInt("developer_id"),
                rs.getInt("publisher_id"),
                rs.getString("status"),
                FORMAT.dateForClass(rs.getDate("start_date").toString()),
                FORMAT.dateForClass(rs.getDate("end_date").toString())
            );
        }
        return null;
    }

    @Override
    public int update(String column, String value, String type) throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "UPDATE contract" +
                "\nSET " + column + " = ?" +
                "\nWHERE developer_id = ? AND publisher_id = ?"
            );
        if (type.equals("date")) {
            preparedStatement.setDate(1, FORMAT.dateForDB(value));
        } 
        else {
            preparedStatement.setString(1, value);
        }
        preparedStatement.setInt(2, developerID);
        preparedStatement.setInt(3, publisherID);

        int response = preparedStatement.executeUpdate();

        DB.modifyContracts(this.read(), "update");

        return response;
    }

    @Override
    public int delete() throws SQLException {
        PreparedStatement preparedStatement = DB.getConnection()
            .prepareStatement(
                "DELETE FROM contract" +
                "\nWHERE developer_id = ? AND publisher_id = ?"
            );
        preparedStatement.setInt(1, developerID);
        preparedStatement.setInt(2, publisherID);

        DB.modifyContracts(this, "delete");

        return preparedStatement.executeUpdate();
    }


    // Filters
    public static List<Contract> filterByProvider(List<Contract> contracts, Integer providerID) {
        Predicate<Contract> condition = contract -> 
            contract.getPublisherID() == providerID || contract.getDeveloperID() == providerID;
        return HelperService.filterByCondition(contracts, condition);
    }

    public static List<Contract> filterByStatus(List<Contract> contracts, String status) {
        Predicate<Contract> condition = contract -> contract.getStatus().equals(status);
        return HelperService.filterByCondition(contracts, condition);
    }
}
