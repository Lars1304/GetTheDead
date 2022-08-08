package com.smartcomm.ubs.pq3.fixer;

import ch.assentis.dblayer.common.XMLUtils;
import com.assentis.docrepo.service.iface.bean.File;
import com.assentis.docrepo.service.iface.bean.SearchResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class PQ3Fixer {
    private static Properties properties = null;
    private static DocRepoService drs = null;

    private static final Function<String, String> removeBOM = s -> s.startsWith("\uFEFF") ? s.substring(1) : s;

    public static void main(String[] args) throws Exception {

        getProperties(Paths.get(args[0]));
        drs = new DocRepoService(properties.getProperty("REPO_URL"), properties.getProperty("USER"), properties.getProperty("PASSWORD") );

        String[] elementTypes = properties.getProperty("ELEMENT_TYPES").split(",");
        final ContentFixer contentFixer = new ContentFixer();
        for (String elementType : elementTypes) {
            SearchResult[] searchResults = drs.findElementsByType(elementType);
            System.out.println(elementType + "      " + searchResults.length);
            for (SearchResult searchResult : searchResults){
                final File item = (File) searchResult.getItem();
                Long dbKey = item.getDBKey();
                String elementId = item.getElementID();

                final byte[] content =  drs.getContents(dbKey);//ADBUtility.unzipElementContent(cockpitElement.getContentZipped());
                final String contentStr = new String(content, StandardCharsets.UTF_8);
                final Document document = XMLUtils.parseDocument(removeBOM.apply(contentStr));
                boolean changed;

                //contentFixer.setIndividualFixer(individualFixer);
                // to test a fix for a single element just uncomment the following line and enter the elementId
//                    if (cockpitElement.getElementID().equalsIgnoreCase("2007403257.1495442062258.86768239481790160.21.1003186756"))
                changed = contentFixer.fixContentProblemsInElement(document, elementId);

                if (changed){
                    drs.updateContent(dbKey, doc2Byte(document));
                }
            }
        }
        HashMap<String,Counter> usage = contentFixer.getUsage();

        System.out.println("\n\n");
        for (String methode : usage.keySet()){
            System.out.println(methode + "\t" + usage.get(methode).getCounter());
        }

    }

    private static String buildPath(String[]pathNames){
        StringBuffer sb = new StringBuffer();


        for(int i=1;i< pathNames.length;i++){
            sb.append("/");
            sb.append(pathNames[i]);
        }

        return sb.toString();
    }

    private static void getProperties(Path file) {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(file.toFile()));
        } catch (FileNotFoundException e) {
            System.out.println("Properties file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO-Error");
            e.printStackTrace();
        }

    }
    private static Document byte2Doc(byte[] content) throws IOException, ParserConfigurationException, SAXException {
        String contentStr = new String(content, StandardCharsets.UTF_8);
        return XMLUtils.parseDocument(removeBOM.apply(contentStr));
    }

    private static byte[] doc2Byte(Document doc) throws IOException {
        String newContent = XMLUtils.serializeAsString(doc);
        return newContent.getBytes(StandardCharsets.UTF_8);
    }

}
