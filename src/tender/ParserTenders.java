package tender;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ParserTenders implements Iparser{
    private Format formatter = new SimpleDateFormat("dd.MM.yyyy");
    private String urlGetTenders = "http://www.tender.pro/api/_info.tenderlist_by_set.json?_key=1732ede4de680a0c93d81f01d7bac7d1&set_type_id=2&set_id=2&max_rows=1000&open_only=t";

    public void Parser() {
        String s = "";
        try {
            s = DownloadFile.ReadJsonFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(s);
        //String s = DownloadFile.DownloadFromUrl(urlGetTenders);
        if(s.equals("") || s.isEmpty()){
            Log.Logger("Получили пустую строку", urlGetTenders);
            System.exit(0);
        }
        Gson gson = new Gson();
        TypeListTenders Tlist = gson.fromJson(s, TypeListTenders.class);
        if(Tlist.result == null || Tlist.result.data == null){
            Log.Logger("Не найден список тендеров", urlGetTenders);
            System.exit(0);
        }
        for (DataTen d: Tlist.result.data
             ) {
            try {
                ParserTender(d);
            } catch (Exception e) {
                Log.Logger("Ошибка при парсинге тендера", e.getStackTrace(), e);
            }
            break;
        }

    }

    public void ParserTender(DataTen d){
        String urlTender = String.format("http://www.tender.pro/api/_tender.info.json?_key=1732ede4de680a0c93d81f01d7bac7d1&company_id=%d&id=%d", d.company_id, d.id);
        String s = DownloadFile.DownloadFromUrl(urlTender);
        if(s.equals("") || s.isEmpty()){
            Log.Logger("Получили пустую строку", urlTender);
            return;
        }
        Gson gson = new Gson();
        TypeTender t = gson.fromJson(s, TypeTender.class);
        //System.out.println(t.result.data.id);
        Date OpenDate = (t.result.data.open_date != null)?GetDate(t.result.data.open_date):new Date(0L);
        Date CloseDate = (t.result.data.close_date != null)?GetDate(t.result.data.close_date):new Date(0L);
        Date ShipDate = (t.result.data.ship_date != null)?GetDate(t.result.data.ship_date):new Date(0L);
        Date FinishDate = (t.result.data.finish_date != null)?GetDate(t.result.data.finish_date):new Date(0L);
        System.out.println(FinishDate);



    }

    private Date GetDate(String dt) {
        Date d = new Date(0L);
        try {
            d = (Date) formatter.parseObject(dt);
        } catch (ParseException e) {

        }
        return d;
    }
}
