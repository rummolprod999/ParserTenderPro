package tender;

import com.google.gson.Gson;

import java.io.IOException;
import java.sql.*;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

class ParserTenders implements Iparser {
    private Format formatter = new SimpleDateFormat("dd.MM.yyyy");
    private String urlGetTenders = "http://www.tender.pro/api/_info.tenderlist_by_set.json?_key=1732ede4de680a0c93d81f01d7bac7d1&set_type_id=2&set_id=2&max_rows=1000&open_only=t";
    private String UrlConnect = String.format("jdbc:mysql://%s:%d/%s?jdbcCompliantTruncation=false", Main.Server, Main.Port, Main.Database);

    public void Parser() {
        String s = "";
        try {
            s = DownloadFile.ReadJsonFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(s);
        //String s = DownloadFile.DownloadFromUrl(urlGetTenders);
        if (s.equals("") || s.isEmpty()) {
            Log.Logger("Получили пустую строку", urlGetTenders);
            System.exit(0);
        }
        Gson gson = new Gson();
        TypeListTenders Tlist = gson.fromJson(s, TypeListTenders.class);
        if (Tlist.result == null || Tlist.result.data == null) {
            Log.Logger("Не найден список тендеров", urlGetTenders);
            System.exit(0);
        }
        for (DataTen d : Tlist.result.data
                ) {
            try {
                ParserTender(d);
            } catch (Exception e) {
                Log.Logger("Ошибка при парсинге тендера", e.getStackTrace(), e);
            }
            break;
        }

    }

    public void ParserTender(DataTen d) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String urlTender = String.format("http://www.tender.pro/api/_tender.info.json?_key=1732ede4de680a0c93d81f01d7bac7d1&company_id=%d&id=%d", d.company_id, d.id);
        String s = DownloadFile.DownloadFromUrl(urlTender);
        if (s.equals("") || s.isEmpty()) {
            Log.Logger("Получили пустую строку", urlTender);
            return;
        }
        Gson gson = new Gson();
        TypeTender t = gson.fromJson(s, TypeTender.class);
        if (t.result.data.id == 0) {
            Log.Logger("Нет номера у тендера");
            return;
        }
        //System.out.println(t.result.data.id);
        Date OpenDate = (t.result.data.open_date != null) ? GetDate(t.result.data.open_date) : new Date(0L);
        Date CloseDate = (t.result.data.close_date != null) ? GetDate(t.result.data.close_date) : new Date(0L);
        Date ShipDate = (t.result.data.ship_date != null) ? GetDate(t.result.data.ship_date) : new Date(0L);
        Date FinishDate = (t.result.data.finish_date != null) ? GetDate(t.result.data.finish_date) : new Date(0L);
        Date dateVersion = OpenDate;
        int cancelstatus = 0;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        try (Connection con = DriverManager.getConnection(UrlConnect, Main.UserDb, Main.PassDb)) {
            PreparedStatement stmt = con.prepareStatement(String.format("SELECT id_tender, date_version FROM %stender WHERE purchase_number = ? AND cancel=0", Main.Prefix));
            stmt.setInt(1, t.result.data.id);
            ResultSet rs = stmt.executeQuery();
            //stmt.executeQuery("SET GLOBAL sql_mode=''");
            while (rs.next()) {
                int id_t = rs.getInt(1);
                Date date_b = rs.getTimestamp(2);
                /*System.out.println(date_b);
                System.out.println(dateVersion);*/
                if (dateVersion.after(date_b) || (date_b.equals(dateVersion))) {
                    PreparedStatement preparedStatement = con.prepareStatement(String.format("UPDATE %stender SET cancel=1 WHERE id_tender = ?", Main.Prefix));
                    preparedStatement.setInt(1, id_t);
                    preparedStatement.execute();
                    preparedStatement.close();
                } else {
                    cancelstatus = 1;
                }

            }
            rs.close();
            stmt.close();
            String Href = String.format("http://www.tender.pro/view_tender_public.shtml?tenderid=%d", t.result.data.id);
            String PurchaseObjectInfo = t.result.data.title;
            String NoticeVersion = "";
            String PrintForm = Href;
            int IdOrganizer = 0;
            TypeCompany c = null;
            if (t.result.data.company_id != 0) {
                String UrlCompany = String.format("http://www.tender.pro/api/_company.info_public.json?id=%s", t.result.data.company_id);
                String com = DownloadFile.DownloadFromUrl(UrlCompany);
                if (com.equals("") || com.isEmpty()) {
                    Log.Logger("Получили пустую строку компании", UrlCompany);

                } else {
                    Gson gsonc = new Gson();
                    c = gsonc.fromJson(com, TypeCompany.class);
                    if (!Objects.equals(c.result.data.inn, "")) {
                        PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_organizer FROM %sorganizer WHERE inn = ? AND kpp = ?", Main.Prefix));
                        stmto.setString(1, c.result.data.inn);
                        stmto.setString(2, c.result.data.kpp);
                        ResultSet rso = stmto.executeQuery();
                        if (rso.next()) {
                            IdOrganizer = rso.getInt(1);
                            rso.close();
                            stmto.close();
                        } else {
                            rso.close();
                            stmto.close();
                            PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %sorganizer SET full_name = ?, inn = ?, kpp = ?, post_address = ?, fact_address = ?, contact_phone = ?, contact_fax = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                            stmtins.setString(1, c.result.data.title_full);
                            stmtins.setString(2, c.result.data.inn);
                            stmtins.setString(3, c.result.data.kpp);
                            stmtins.setString(4, c.result.data.address_legal);
                            stmtins.setString(5, c.result.data.address);
                            stmtins.setString(6, c.result.data.phone);
                            stmtins.setString(7, c.result.data.fax);
                            stmtins.executeUpdate();
                            ResultSet rsoi = stmtins.getGeneratedKeys();
                            if (rsoi.next()) {
                                IdOrganizer = rsoi.getInt(1);
                            }
                        }
                    }

                }


            }
            int IdPlacingWay = 0;
            if (!Objects.equals(t.result.data.type_name, "")) {
                PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_placing_way FROM %splacing_way WHERE name = ? AND code = ? LIMIT 1", Main.Prefix));
                stmto.setString(1, t.result.data.type_name);
                stmto.setString(2, String.valueOf(t.result.data.type_id));
                ResultSet rso = stmto.executeQuery();
                if (rso.next()) {
                    IdPlacingWay = rso.getInt(1);
                    rso.close();
                    stmto.close();
                } else {
                    rso.close();
                    stmto.close();
                    int conf = GetConformity(t.result.data.type_name);
                    PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %splacing_way SET name = ?, conformity = ?, code = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                    stmtins.setString(1, t.result.data.type_name);
                    stmtins.setInt(2, conf);
                    stmtins.setString(3, String.valueOf(t.result.data.type_id));
                    stmtins.executeUpdate();
                    ResultSet rsoi = stmtins.getGeneratedKeys();
                    if (rsoi.next()) {
                        IdPlacingWay = rsoi.getInt(1);
                    }
                }


            }
            int IdEtp = 0;
            String etpName = "Tender.Pro";
            String etpUrl = "http://Tender.Pro";
            if (true) {
                PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_etp FROM %setp WHERE name = ? AND url = ? LIMIT 1", Main.Prefix));
                stmto.setString(1, etpName);
                stmto.setString(2, etpUrl);
                ResultSet rso = stmto.executeQuery();
                if (rso.next()) {
                    IdEtp = rso.getInt(1);
                    rso.close();
                    stmto.close();
                } else {
                    rso.close();
                    stmto.close();
                    PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %setp SET name = ?, url = ?, conf=0", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                    stmtins.setString(1, etpName);
                    stmtins.setString(2, etpUrl);
                    stmtins.executeUpdate();
                    ResultSet rsoi = stmtins.getGeneratedKeys();
                    if (rsoi.next()) {
                        IdEtp = rsoi.getInt(1);
                    }
                }
            }
            int typeFz = 0;
            int idTender = 0;
            int Version = 0;
            String UrlXml = Href;

            System.out.println(IdEtp);
        }
    }

    //System.out.println(FinishDate);


    private Date GetDate(String dt) {
        Date d = new Date(0L);
        try {
            d = (Date) formatter.parseObject(dt);
        } catch (ParseException ignored) {

        }
        return d;
    }

    private int GetConformity(String conf) {
        String s = conf.toLowerCase();
        if (s.contains("открыт")) {
            return 5;
        } else if (s.contains("аукцион")) {
            return 1;
        } else if (s.contains("котиров")) {
            return 2;
        } else if (s.contains("предложен")) {
            return 3;
        } else if (s.contains("единств")) {
            return 4;
        } else {
            return 6;
        }
    }
}
