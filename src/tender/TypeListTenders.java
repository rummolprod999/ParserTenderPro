package tender;

public class TypeListTenders {
    String success;
    Result result;
}

class Result {
    DataTen[] data;
}

class DataTen {
    int id;
    int company_id;
    int is_223fz;
    String ship_date;
    String open_date;
    String close_date;
    String finish_date;
    String company_name;
    String type_name;
    int type_id;
    String delivery_address;
    String currency_name;
    String title;
    String anno;


}

class TypeTender {
    String success;
    ResultT result;
}

class ResultT {
    DataTen data;


}

class TypeCompany{
    ResultC result;
}

class ResultC {
    DataC data;

}

class DataC{
    String address_legal;
    String title_full;
    String fax;
    String address;
    String country_name;
    String phone;
    String kpp;
    String inn;
    String site;
}
