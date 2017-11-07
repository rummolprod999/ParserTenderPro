package tender;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.System.out;

public class Main {
    public final static File executePath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
    public static String Database;
    public static String tempDirTenders;
    public static String logDirTenders;
    public static String Prefix;
    public static String UserDb;
    public static String PassDb;
    public static String Server;
    public static int Port;
    public static String logPath;
    public static Date DateNow = new Date();

    public static void main(String[] args) {
        Init();
        ParserTenderPro();


    }

    private static void Init() {
        GetSettings set = new GetSettings();
        Database = set.Database;
        tempDirTenders = set.tempDirTenders;
        logDirTenders = set.logDirTenders;
        Prefix = set.Prefix;
        UserDb = set.UserDb;
        PassDb = set.PassDb;
        Server = set.Server;
        Port = set.Port;
        if (tempDirTenders.equals("") || tempDirTenders.isEmpty()) {
            out.println("Не задана папка для временных файлов, выходим из программы");
            System.exit(0);
        }
        if (logDirTenders.equals("") || logDirTenders.isEmpty()) {
            out.println("Не задана папка для логов, выходим из программы");
            System.exit(0);
        }
        File tmp = new File(tempDirTenders);
        if(tmp.exists()){
            tmp.delete();
            tmp.mkdir();
        }
        else{
            tmp.mkdir();
        }
        File log = new File(logDirTenders);
        if(!log.exists()){
            log.mkdir();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        logPath = String.format("%s%slog_parsing_%s.log", logDirTenders, File.separator, dateFormat.format(DateNow));
        //out.println(logPath);

    }

    private static void ParserTenderPro(){
        Log.Logger("Начало парсинга");
        ParserTenders p = new ParserTenders();
        try {
            p.Parser();
        } catch (Exception e) {
            Log.Logger("Error Main function", e);
            e.printStackTrace();
        }
        Log.Logger("Конец парсинга");
    }
}
