package tender;
import java.sql.SQLException;

interface Iparser {
    void Parser();
    void ParserTender (DataTen d, ITenderKwrds tk, IAddVerNum av) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException;

}
