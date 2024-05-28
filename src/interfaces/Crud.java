package interfaces;

import java.sql.SQLException;

public interface Crud<T extends Formattable> {
    public int create() throws SQLException;
    public T read() throws SQLException;
    public int update(String column, String value, String type) throws SQLException;
    public int delete() throws SQLException;
}
