package tender;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Set;

class DownloadFile {
    static String DownloadFromUrl(String urls) {
        String[] pr = getRandomSetElement(Main.Proxy).split(":");
        StringBuilder s = new StringBuilder();
        int count = 0;
        while (true) {
            if (count > 10) {
                Log.Logger(String.format("Не скачали строку за %d попыток", count), urls);
                break;
            }
            try {
                URL url = new URL(urls);
                if(Main.UseProxy){
                    InetSocketAddress proxyAddress = new InetSocketAddress(pr[0], Integer.valueOf(pr[1]));
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
                    HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return (new PasswordAuthentication(pr[2], pr[3].toCharArray()));
                        }
                    });
                    uc.connect();
                }

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

    static <E> E getRandomSetElement(Set<E> set) {
        return set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
    }
}

