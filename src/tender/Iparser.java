package tender;
import java.sql.SQLException;

interface Iparser {
    void Parser();
    void ParserTender (DataTen d) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException;

}
