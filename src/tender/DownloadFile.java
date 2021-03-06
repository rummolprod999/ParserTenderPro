package tender;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

class DownloadFile {
    static String DownloadFromUrl(String urls) {
        StringBuilder s = new StringBuilder();
        int count = 0;
        while (true) {
            if (count > 50) {
                Log.Logger(String.format("Не скачали строку за %d попыток", count), urls);
                break;
            }
            try {
                URL url = new URL(urls);
                InputStream is = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    s.append(inputLine);
                }
                br.close();
                is.close();
                return s.toString();

            } catch (Exception e) {
                count++;
                //e.printStackTrace();
            }
        }
        return s.toString();
    }

    static String ReadJsonFile() throws IOException {
        return new String(Files.readAllBytes(Paths.get(Main.executePath + File.separator + "11111.txt")));
    }
}

