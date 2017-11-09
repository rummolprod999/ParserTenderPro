package tender;

import java.sql.Connection;
import java.sql.SQLException;

public interface IAddVerNum {
    void AddVNum(Connection con, String id) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException;
}
