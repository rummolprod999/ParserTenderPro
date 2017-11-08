package tender;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

class Log {
    @SafeVarargs
    static <T extends Object> void Logger(T... parametrs) {
        StringBuilder s = new StringBuilder();
        s.append(new Date());
        for (T p : parametrs) {
            if(p instanceof StackTraceElement[]){
                for(StackTraceElement n: (StackTraceElement[])p){
                    s.append(String.format(" %s\n", n.toString()));
                }
            }
            else{
                s.append(String.format(" %s\n", p.toString()));
            }

        }
        try (FileWriter writer = new FileWriter(Main.logPath, true)) {
            writer.write(String.valueOf(s));
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }

    }
}
