package com.smartcomm.ubs.pq3.getthedead;

import com.assentis.docrepo.service.common.DocRepoConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Elements {
    private final static String ddTypes = DocRepoConstants.FILETYPE_DEPLOYMENTPACKAGE + "|" +
                                            DocRepoConstants.FILETYPE_DPMODLIB + "|" +
                                             DocRepoConstants.FILETYPE_WORKSPACE;

    HashMap<Long, Element> tst = null;
    HashMap<Long, Element> dd = null;
    HashMap<Long, Element> comp = null;

    public Elements() {
        tst = new HashMap<>();
        dd = new HashMap<>();
        comp = new HashMap<>();
    }

    public void add(Element e){
        if (DocRepoConstants.FILETYPE_TSTEMPLATE.equals(e.getType())){
            tst.put(e.getDbKey(), e);
        } else if (ddTypes.contains(e.getType())) {
            dd.put(e.getDbKey(), e);
        } else {
            comp.put(e.getDbKey(),e);
        }
    }

    public void addReference(Element referencing, Element referenced){
        referencing.addReferencing(referenced);
        referenced.addReferencedBy(referencing);
    }

    public Element getElementByDbKey(long key){
        Element e = comp.get(key);
        if (e != null) {
            return e;
        }
        e = dd.get(key);
        if (e != null) {
            return e;
        }
        return tst.get(key);
    }

    public List<Element> getTstList(){
        return new ArrayList<>(tst.values());
    }

    public List<Element> getCompList(){
        return new ArrayList<>(comp.values());
    }

    public void markElementsToKeep(){
        for(Element e : comp.values()){
            e.shouldBeKept();
        }
    }
}
