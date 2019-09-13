package tender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExtendTenderClass {
    static void TenderKwords(int idTender, Connection con, String NoticeVersion) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        StringBuilder s = new StringBuilder();
        PreparedStatement p1 = con.prepareStatement(String.format("SELECT DISTINCT po.name, po.okpd_name FROM %spurchase_object AS po LEFT JOIN %slot AS l ON l.id_lot = po.id_lot WHERE l.id_tender = ?", Main.Prefix, Main.Prefix));
        p1.setInt(1, idTender);
        ResultSet r1 = p1.executeQuery();
        while (r1.next()) {
            String name = r1.getString(1);
            if (name == null) {
                name = "";
            }
            String okpdName = r1.getString(2);
            if (okpdName == null) {
                okpdName = "";
            }
            s.append(String.format(" %s", name));
            s.append(String.format(" %s", okpdName));
        }
        r1.close();
        p1.close();
        PreparedStatement p2 = con.prepareStatement(String.format("SELECT DISTINCT file_name FROM %sattachment WHERE id_tender = ?", Main.Prefix));
        p2.setInt(1, idTender);
        ResultSet r2 = p2.executeQuery();
        while (r2.next()) {
            String attName = r2.getString(1);
            if (attName == null) {
                attName = "";
            }
            s.append(String.format(" %s", attName));
        }
        r2.close();
        p2.close();
        int idOrg = 0;
        PreparedStatement p3 = con.prepareStatement(String.format("SELECT purchase_object_info, id_organizer FROM %stender WHERE id_tender = ?", Main.Prefix));
        p3.setInt(1, idTender);
        ResultSet r3 = p3.executeQuery();
        while (r3.next()) {
            idOrg = r3.getInt(2);
            String purOb = r3.getString(1);
            s.append(String.format(" %s", purOb));
        }
        r3.close();
        p3.close();
        if (idOrg != 0) {
            PreparedStatement p4 = con.prepareStatement(String.format("SELECT full_name, inn FROM %sorganizer WHERE id_organizer = ?", Main.Prefix));
            p4.setInt(1, idOrg);
            ResultSet r4 = p4.executeQuery();
            while (r4.next()) {
                String innOrg = r4.getString(2);
                if (innOrg == null) {
                    innOrg = "";
                }
                String nameOrg = r4.getString(1);
                if (nameOrg == null) {
                    nameOrg = "";
                }
                s.append(String.format(" %s", innOrg));
                s.append(String.format(" %s", nameOrg));

            }
            r4.close();
            p4.close();
        }
        PreparedStatement p5 = con.prepareStatement(String.format("SELECT DISTINCT cus.inn, cus.full_name FROM %scustomer AS cus LEFT JOIN %spurchase_object AS po ON cus.id_customer = po.id_customer LEFT JOIN %slot AS l ON l.id_lot = po.id_lot WHERE l.id_tender = ?", Main.Prefix, Main.Prefix, Main.Prefix));
        p5.setInt(1, idTender);
        ResultSet r5 = p5.executeQuery();
        while (r5.next()) {
            String fullNameC;
            fullNameC = r5.getString(1);
            if (fullNameC == null) {
                fullNameC = "";
            }
            String innC;
            innC = r5.getString(2);
            if (innC == null) {
                innC = "";
            }
            s.append(String.format(" %s", innC));
            s.append(String.format(" %s", fullNameC));
        }
        r5.close();
        p5.close();
        s.append(String.format(" %s", NoticeVersion));
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(s.toString());
        String ss = matcher.replaceAll(" ");
        ss = ss.trim();
        PreparedStatement p6 = con.prepareStatement(String.format("UPDATE %stender SET tender_kwords = ? WHERE id_tender = ?", Main.Prefix));
        p6.setString(1, ss);
        p6.setInt(2, idTender);
        p6.executeUpdate();
        p6.close();

    }

    static void AddVNum(Connection con, String id) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        int verNum = 1;
        PreparedStatement p1 = con.prepareStatement(String.format("SELECT id_tender FROM %stender WHERE purchase_number = ? ORDER BY UNIX_TIMESTAMP(date_version) ASC", Main.Prefix));
        p1.setString(1, id);
        ResultSet r1 = p1.executeQuery();
        while (r1.next()) {
            int IdTender = r1.getInt(1);
            PreparedStatement p2 = con.prepareStatement(String.format("UPDATE %stender SET num_version = ? WHERE id_tender = ? AND type_fz = 4", Main.Prefix));
            p2.setInt(1, verNum);
            p2.setInt(2, IdTender);
            p2.executeUpdate();
            p2.close();
            verNum++;
        }
        r1.close();
        p1.close();

    }
}
