package tender;

import java.sql.Connection;
import java.sql.SQLException;

public interface ITenderKwrds {
    void UpdateTKwords(int idTender, Connection con, String NoticeVersion) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException;
}
