package tender;

import com.google.gson.Gson;
import org.jsoup.Jsoup;

import java.sql.*;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

class ParserTenders implements Iparser {
    private Format formatter = new SimpleDateFormat("dd.MM.yyyy");
    private String UrlConnect = String.format("jdbc:mysql://%s:%d/%s?jdbcCompliantTruncation=false&useUnicode=true&characterEncoding=utf-8", Main.Server, Main.Port, Main.Database);

    public void Parser() {
        /*String s = "";
        try {
            s = DownloadFile.ReadJsonFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(s);*/
        String urlGetTenders = "http://www.tender.pro/api/_info.tenderlist_by_set.json?_key=1732ede4de680a0c93d81f01d7bac7d1&set_type_id=2&set_id=2&max_rows=1000&open_only=t";
        String s = DownloadFile.DownloadFromUrl(urlGetTenders);
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
                ParserTender(d, ExtendTenderClass::TenderKwords, ExtendTenderClass::AddVNum);
            } catch (Exception e) {
                Log.Logger("Ошибка при парсинге тендера", e.getStackTrace(), e);
            }
            //break;
        }

    }

    @Override
    public void ParserTender(DataTen d, ITenderKwrds tk, IAddVerNum av) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        try (Connection con = DriverManager.getConnection(UrlConnect, Main.UserDb, Main.PassDb)) {
            Date OpenDate = (d.open_date != null) ? GetDate(d.open_date) : new Date(0L);
            PreparedStatement stmt0 = con.prepareStatement(String.format("SELECT id_tender FROM %stender WHERE purchase_number = ? AND date_version = ? AND type_fz = 4", Main.Prefix));
            stmt0.setString(1, String.valueOf(d.id));
            stmt0.setDate(2, new java.sql.Date(OpenDate.getTime()));
            ResultSet r = stmt0.executeQuery();
            if (r.next()) {
                r.close();
                stmt0.close();
                //Log.Logger("Такой тендер уже есть в базе", String.valueOf(t.id));
                return;
            }
            r.close();
            stmt0.close();
            String urlTender = String.format("http://www.tender.pro/api/_tender.info.json?_key=1732ede4de680a0c93d81f01d7bac7d1&company_id=%d&id=%d", d.company_id, d.id);
            String s = DownloadFile.DownloadFromUrl(urlTender);
            if (s.equals("") || s.isEmpty()) {
                Log.Logger("Получили пустую строку", urlTender);
                return;
            }
            Gson gson = new Gson();
            TypeTender tt = gson.fromJson(s, TypeTender.class);
            DataTen t = null;
            if (tt != null && tt.result != null && tt.result.data != null) {
                t = tt.result.data;
            }
            if (tt.error != null && tt.error.message != null) {
                Log.Logger("Неудачная попытка скачать тендер, берем объект из списка", d.id, d.company_id, tt.error.message);
                t = d;
            }
            if (t == null) {
                Log.Logger("Не создан объект тендера", s);
                return;
            }
            try {
                if (t.id == 0 || t.is_223fz == 1) {
                    Log.Logger("Нет номера у тендера или 223 тендер");
                    return;
                }
            } catch (Exception e) {
                Log.Logger("Ошибки в объекте тендера", s, d.id, d.company_id);
                return;
            }
            //System.out.println(t.id);

            Date CloseDate = (t.close_date != null) ? GetDate(t.close_date) : new Date(0L);
            int cancelstatus = 0;
            PreparedStatement stmt = con.prepareStatement(String.format("SELECT id_tender, date_version FROM %stender WHERE purchase_number = ? AND cancel=0 AND type_fz = 4", Main.Prefix));
            stmt.setInt(1, t.id);
            ResultSet rs = stmt.executeQuery();
            //stmt.executeQuery("SET GLOBAL sql_mode=''");
            while (rs.next()) {
                int id_t = rs.getInt(1);
                Date date_b = rs.getTimestamp(2);
                /*System.out.println(date_b);
                System.out.println(dateVersion);*/
                if (OpenDate.after(date_b) || (date_b.equals(OpenDate))) {
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
            String Href = String.format("http://www.tender.pro/view_tender_public.shtml?tenderid=%d", t.id);
            String PurchaseObjectInfo = (t.title == null) ? "" : t.title;
            String NoticeVersion;
            int IdOrganizer = 0;
            TypeCompany c = null;
            if (t.company_id != 0) {
                String UrlCompany = String.format("http://www.tender.pro/api/_company.info_public.json?id=%s", t.company_id);
                String com = DownloadFile.DownloadFromUrl(UrlCompany);
                if (com.equals("") || com.isEmpty()) {
                    Log.Logger("Получили пустую строку компании", UrlCompany);

                } else {
                    Gson gsonc = new Gson();
                    c = gsonc.fromJson(com, TypeCompany.class);
                    if (c != null && c.result != null && c.result.data != null && c.result.data.inn != null && !Objects.equals(c.result.data.inn, "")) {
                        PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_organizer FROM %sorganizer WHERE inn = ? AND kpp = ?", Main.Prefix));
                        stmto.setString(1, c.result.data.inn);
                        stmto.setString(2, (c.result.data.kpp == null) ? "" : c.result.data.kpp);
                        ResultSet rso = stmto.executeQuery();
                        if (rso.next()) {
                            IdOrganizer = rso.getInt(1);
                            rso.close();
                            stmto.close();
                        } else {
                            rso.close();
                            stmto.close();
                            PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %sorganizer SET full_name = ?, inn = ?, kpp = ?, post_address = ?, fact_address = ?, contact_phone = ?, contact_fax = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                            stmtins.setString(1, (c.result.data.title_full == null) ? "" : c.result.data.title_full);
                            stmtins.setString(2, c.result.data.inn);
                            stmtins.setString(3, (c.result.data.kpp == null) ? "" : c.result.data.kpp);
                            stmtins.setString(4, (c.result.data.address_legal == null) ? "" : c.result.data.address_legal);
                            stmtins.setString(5, (c.result.data.address == null) ? "" : c.result.data.address);
                            stmtins.setString(6, (c.result.data.phone == null) ? "" : c.result.data.phone);
                            stmtins.setString(7, (c.result.data.fax == null) ? "" : c.result.data.fax);
                            stmtins.executeUpdate();
                            ResultSet rsoi = stmtins.getGeneratedKeys();
                            if (rsoi.next()) {
                                IdOrganizer = rsoi.getInt(1);
                            }
                            rsoi.close();
                            stmtins.close();
                        }
                    }

                }


            }
            int IdPlacingWay = 0;
            if (!Objects.equals(t.type_name, "") && t.type_name != null) {
                PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_placing_way FROM %splacing_way WHERE name = ? AND code = ? LIMIT 1", Main.Prefix));
                stmto.setString(1, t.type_name);
                stmto.setString(2, String.valueOf(t.type_id));
                ResultSet rso = stmto.executeQuery();
                if (rso.next()) {
                    IdPlacingWay = rso.getInt(1);
                    rso.close();
                    stmto.close();
                } else {
                    rso.close();
                    stmto.close();
                    int conf = GetConformity(t.type_name);
                    PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %splacing_way SET name = ?, conformity = ?, code = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                    stmtins.setString(1, t.type_name);
                    stmtins.setInt(2, conf);
                    stmtins.setString(3, String.valueOf(t.type_id));
                    stmtins.executeUpdate();
                    ResultSet rsoi = stmtins.getGeneratedKeys();
                    if (rsoi.next()) {
                        IdPlacingWay = rsoi.getInt(1);
                    }
                    rsoi.close();
                    stmtins.close();

                }


            }
            int IdEtp = 0;
            String etpName = "Tender.Pro";
            String etpUrl = "http://Tender.Pro";
            try {
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
                    rsoi.close();
                    stmtins.close();
                }
            } catch (Exception ignored) {

            }
            int idTender = 0;
            NoticeVersion = (t.anno != null) ? Jsoup.parse(t.anno).text() : "";
            PreparedStatement insertTender = con.prepareStatement(String.format("INSERT INTO %stender SET id_region = 0, id_xml = ?, purchase_number = ?, doc_publish_date = ?, href = ?, purchase_object_info = ?, type_fz = ?, id_organizer = ?, id_placing_way = ?, id_etp = ?, end_date = ?, cancel = ?, date_version = ?, num_version = ?, notice_version = ?, xml = ?, print_form = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
            insertTender.setString(1, String.valueOf(t.id));
            insertTender.setString(2, String.valueOf(t.id));
            insertTender.setDate(3, new java.sql.Date(OpenDate.getTime()));
            insertTender.setString(4, Href);
            insertTender.setString(5, PurchaseObjectInfo);
            insertTender.setInt(6, 4);
            insertTender.setInt(7, IdOrganizer);
            insertTender.setInt(8, IdPlacingWay);
            insertTender.setInt(9, IdEtp);
            insertTender.setDate(10, new java.sql.Date(CloseDate.getTime()));
            insertTender.setInt(11, cancelstatus);
            insertTender.setDate(12, new java.sql.Date(OpenDate.getTime()));
            insertTender.setInt(13, 1);
            insertTender.setString(14, NoticeVersion);
            insertTender.setString(15, urlTender);
            insertTender.setString(16, Href);
            insertTender.executeUpdate();
            ResultSet rt = insertTender.getGeneratedKeys();
            if (rt.next()) {
                idTender = rt.getInt(1);
            }
            rt.close();
            insertTender.close();
            Main.AddTender++;
            int idLot = 0;
            int LotNumber = 1;
            PreparedStatement insertLot = con.prepareStatement(String.format("INSERT INTO %slot SET id_tender = ?, lot_number = ?, currency = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
            insertLot.setInt(1, idTender);
            insertLot.setInt(2, LotNumber);
            insertLot.setString(3, (t.currency_name == null) ? "" : t.currency_name);
            insertLot.executeUpdate();
            ResultSet rl = insertLot.getGeneratedKeys();
            if (rl.next()) {
                idLot = rl.getInt(1);
            }
            rl.close();
            insertLot.close();
            int idCustomer = 0;
            if (c != null) {
                if (c != null && c.result != null && c.result.data != null && c.result.data.inn != null && !Objects.equals(c.result.data.inn, "")) {
                    PreparedStatement stmto = con.prepareStatement(String.format("SELECT id_customer FROM %scustomer WHERE inn = ? LIMIT 1", Main.Prefix));
                    stmto.setString(1, c.result.data.inn);
                    ResultSet rso = stmto.executeQuery();
                    if (rso.next()) {
                        idCustomer = rso.getInt(1);
                        rso.close();
                        stmto.close();
                    } else {
                        rso.close();
                        stmto.close();
                        PreparedStatement stmtins = con.prepareStatement(String.format("INSERT INTO %scustomer SET full_name = ?, is223=1, reg_num = ?, inn = ?", Main.Prefix), Statement.RETURN_GENERATED_KEYS);
                        stmtins.setString(1, (c.result.data.title_full == null) ? "" : c.result.data.title_full);
                        stmtins.setString(2, java.util.UUID.randomUUID().toString());
                        stmtins.setString(3, c.result.data.inn);
                        stmtins.executeUpdate();
                        ResultSet rsoi = stmtins.getGeneratedKeys();
                        if (rsoi.next()) {
                            idCustomer = rsoi.getInt(1);
                        }
                        rsoi.close();
                        stmtins.close();
                    }
                }
            }
            if (d.delivery_address != null && !Objects.equals(d.delivery_address, "")) {
                PreparedStatement insertCusRec = con.prepareStatement(String.format("INSERT INTO %scustomer_requirement SET id_lot = ?, id_customer = ?, delivery_place = ?, delivery_term = ?", Main.Prefix));
                insertCusRec.setInt(1, idLot);
                insertCusRec.setInt(2, idCustomer);
                insertCusRec.setString(3, (d.delivery_address != null) ? d.delivery_address : "");
                insertCusRec.setString(4, NoticeVersion);
                insertCusRec.executeUpdate();
                insertCusRec.close();
            }
            if (t.title != null && !Objects.equals(t.title, "")) {
                PreparedStatement insertPurObj = con.prepareStatement(String.format("INSERT INTO %spurchase_object SET id_lot = ?, id_customer = ?, name = ?", Main.Prefix));
                insertPurObj.setInt(1, idLot);
                insertPurObj.setInt(2, idCustomer);
                insertPurObj.setString(3, t.title);
                insertPurObj.executeUpdate();
                insertPurObj.close();

            }

            try {
                tk.UpdateTKwords(idTender, con, NoticeVersion);
            } catch (Exception e) {
                Log.Logger("Ошибка добавления ключевых слов", e.getStackTrace());
            }

            try {
                av.AddVNum(con, String.valueOf(t.id));
            } catch (Exception e) {
                Log.Logger("Ошибка добавления версий", e.getStackTrace());
            }

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
