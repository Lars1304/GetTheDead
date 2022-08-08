package com.smartcomm.ubs.pq3.getthedead;

import com.assentis.docrepo.service.common.DocRepoConstants;
import com.assentis.docrepo.service.iface.bean.File;
import com.assentis.docrepo.service.iface.bean.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class Element {

    private final static String KEEPS = DocRepoConstants.FILETYPE_TSTEMPLATE + "|" +
            DocRepoConstants.FILETYPE_DEPLOYMENTPACKAGE + "|" +
            DocRepoConstants.FILETYPE_DPMODLIB + "|" +
            DocRepoConstants.FILETYPE_WORKSPACE;

    private ArrayList<Element> referencedBy = null;
    private ArrayList<Element> referencing = null;
    private String name = null;
    private String elementId = null;
    private long dbKey = -9;
    private String type = null;
    private String path = null;
    private boolean keep = false;
    private SearchResult searchResult = null;

    public Element(SearchResult searchResult, boolean keep) {
        final File item = (File) searchResult.getItem();
        dbKey = item.getDBKey();
        name = item.getName();
        elementId = item.getElementID();
        type = item.getType();
        path = convertPath(searchResult.getPathNames());
        this.searchResult = searchResult;
        this.keep = keep;

        referencing = new ArrayList<>();
        referencedBy = new ArrayList<>();


    }

    public void addReferencedBy(Element e) {
        referencedBy.add(e);
    }

    public void addReferencing(Element e) {
        referencing.add(e);
    }

    public List<Element> getReferencedByList() {
        return referencedBy;
    }

    public List<Element> getReferencingList(){
        return referencing;
    }

    public String getName() {
        return name;
    }

    public String getElementId() {
        return elementId;
    }

    public long getDbKey() {
        return dbKey;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public boolean isKeep() {
        return keep;
    }

    public boolean shouldBeKept() {
        if (keep) {
            return true;
        }
        for (Element e : referencedBy) {
            boolean b = e.shouldBeKept();
            if (b){
                keep = true;
                return true;
            }
        }

        return false;
    }

    private String convertPath(String[] pathNames) {
        StringBuffer sb = new StringBuffer();


        for (int i = 1; i < pathNames.length; i++) {
            sb.append("/");
            sb.append(pathNames[i]);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", elementId='" + elementId + '\'' +
                ", dbKey=" + dbKey +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
