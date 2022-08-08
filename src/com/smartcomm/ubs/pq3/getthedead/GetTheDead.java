package com.smartcomm.ubs.pq3.getthedead;

import com.assentis.docrepo.service.common.DocRepoConstants;
import com.assentis.docrepo.service.iface.bean.File;
import com.assentis.docrepo.service.iface.bean.SearchResult;
import com.smartcomm.ubs.pq3.fixer.DocRepoService;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GetTheDead {

    private static DocRepoService drs = null;
    private static Properties properties = null;
    private static  Elements elements = null;

    public static void main(String[] args) throws Exception {

        getProperties(Paths.get(args[0]));
        drs = new DocRepoService(properties.getProperty("REPO_URL"), properties.getProperty("USER"), properties.getProperty("PASSWORD") );

        elements = new Elements();

        loadElements(DocRepoConstants.FILETYPE_TSTEMPLATE, true);
        loadElements(DocRepoConstants.FILETYPE_DEPLOYMENTPACKAGE, true);
        loadElements(DocRepoConstants.FILETYPE_WORKSPACE, true);
        loadElements(DocRepoConstants.FILETYPE_DPMODLIB, true);
        loadElements(DocRepoConstants.FILETYPE_TEXTCOMP, false);
        loadElements(DocRepoConstants.FILETYPE_TEXTEXP,false);

        for (Element element : elements.getCompList()){
            long[] referencing = drs.getReferencedBy(element.getDbKey());
            for (long key : referencing) {
                if (key == 30671) {
                    String x = "jasd";
                }
                Element referer = elements.getElementByDbKey(key);
                if (referer == null) {
                    SearchResult sr = drs.findElementByDbKey(key);
                    String type = new Element(sr,false).getType();
                    System.out.println("Element with dbKey " + key + "(" + type +") referencing " + element.getDbKey() + " cannot be found");
                } else {
                    elements.addReference(referer, element);
                }
            }
        }
        elements.markElementsToKeep();

        PrintWriter pw = new PrintWriter(new FileWriter("c:/tmp/pq3_Textkomponenten.txt"));
        for(Element e : elements.getCompList()){
            pw.println(createLine(e));
        }
        pw.flush();
        pw.close();
    }

    private static String createLine(Element e){
        StringBuffer sb = new StringBuffer();
        sb.append(e.isKeep() ? "Nein;" : "Ja;");
        sb.append(e.getName());
        sb.append(";");
        sb.append(e.getType());
        sb.append(";");
        sb.append(e.getPath());
        sb.append(";");
        sb.append(e.getElementId());
        sb.append(";");
        sb.append("Referenced by: ");

        boolean first = true;
        for(Element r : e.getReferencedByList()){
            if (first){
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(r.getType());
            sb.append(":");
            sb.append(r.getName());

        }
        return sb.toString();
    }

    private static void loadElements(String type, boolean keep) throws Exception {
        SearchResult[] searchResults = drs.findElementsByType(type);
        for (SearchResult searchResult : searchResults) {
            final File item = (File) searchResult.getItem();
            Element element = new Element(searchResult, keep);
            elements.add(element);
        }
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

}
