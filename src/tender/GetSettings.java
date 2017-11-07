package tender;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class GetSettings {
    public String Database;
    public String tempDirTenders;
    public String logDirTenders;
    public String Prefix;
    public String UserDb;
    public String PassDb;
    public String Server;
    public int Port;

    public GetSettings() {
        try {
            String filePathSetting = Main.executePath + File.separator + "setting_tenders.xml";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(filePathSetting);
            Node root = document.getDocumentElement();
            NodeList settings = root.getChildNodes();
            for (int i = 0; i < settings.getLength(); i++) {
                Node set = settings.item(i);
                if (set.getNodeType() != Node.TEXT_NODE) {
                    switch (set.getNodeName()) {
                        case "database":
                            Database = set.getChildNodes().item(0).getTextContent();
                            break;
                        case "tempdir_tenders":
                            tempDirTenders = Main.executePath + File.separator + set.getChildNodes().item(0).getTextContent();
                            break;
                        case "logdir_tenders":
                            logDirTenders = Main.executePath + File.separator + set.getChildNodes().item(0).getTextContent();
                            break;
                        case "prefix":
                            try {
                                Prefix = set.getChildNodes().item(0).getTextContent();
                            } catch (Exception e) {
                                Prefix = "";
                            }
                            break;
                        case "userdb":
                            UserDb = set.getChildNodes().item(0).getTextContent();
                            break;
                        case "passdb":
                            PassDb = set.getChildNodes().item(0).getTextContent();
                            break;
                        case "server":
                            Server = set.getChildNodes().item(0).getTextContent();
                            break;
                        case "port":
                            Port = Integer.valueOf(set.getChildNodes().item(0).getTextContent());
                            break;


                    }
                }

            }
            /*System.out.println(Database);
            System.out.println(tempDirTenders);*/
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
