package tender;

import com.google.gson.Gson;

class ParserTenders {
    private String urlGetTenders = "http://www.tender.pro/api/_info.tenderlist_by_set.json?_key=1732ede4de680a0c93d81f01d7bac7d1&set_type_id=2&set_id=2&max_rows=1000&open_only=t";

    void Parser() {
        String s = DownloadFile.DownloadFromUrl(urlGetTenders);
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

    }
}
