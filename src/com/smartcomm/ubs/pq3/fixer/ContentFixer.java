package com.smartcomm.ubs.pq3.fixer;

import ch.assentis.dblayer.common.XMLUtils;
//import com.assentis.docwrite.htmlclient.ssrules.impl.ResourceName;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ContentFixer {

    private static final Logger LOGGER = LogManager.getLogger(ContentFixer.class);

    private static final String MISSING_COMPONENT_ID_XPATH = "//TextComponent[Reference and Content/write:html/write:body/write:textCompEmbedded/Expression and not(Content/write:html/write:body/write:textCompEmbedded/Expression/@id) and not(contains(Reference/text(), 'dpmodlib'))]";
    private static final String EMPTY_COMPONENT_XPATH = "//TextComponent[not(Content/.//Text[write:html or a:doc-fragment]) and not(Reference)]";

    private static HashMap<String,Counter> usage = new HashMap<>();
 //   private  IndividualFixer individualFixer = null;

    public boolean fixContentProblemsInElement(Document document, String elementId) throws Exception {
        XPath xpath = getNamespaceAwareXpath();
        return fixAllTheProblems(xpath, document, elementId);
    }

    private boolean fixAllTheProblems(XPath xpath, Document document, String elementId)
            throws Exception {

        boolean changed = handleEmbeddedExpressions(xpath, document, elementId);
//        changed = fixUidProblem(xpath, document, elementId) || changed;
        changed = convertParagraphToDiv(xpath, document, elementId) || changed;
//        changed = fixMissingDefaultInSwitch(xpath, document, elementId) || changed;
//        changed = fixMissingLocTextTextStructure(xpath, document, elementId) || changed;
//        changed = fixMissingTableWidthProblem(xpath, document, elementId) || changed; // CANO Specific
//        changed = fixReadOnlyProblem(xpath, document, elementId) || changed;
//        changed = fixVerticalAlignProblem(xpath, document, elementId) || changed; // CANO Specific
//        changed = fixTRStyleProblem(xpath, document, elementId) || changed;
//        changed = fixAttributeAlignProblem(xpath, document, elementId) || changed;
//        changed = fixAttributeVAlignProblem(xpath, document, elementId) || changed;
        changed = fixFontTagProblem(xpath, document, elementId) || changed;
        changed = removeEmptySpansProblem(xpath, document, elementId) || changed;
        changed = fixTableInsideDivProblem(xpath, document, elementId) || changed;
//        changed = addMissingDivs(xpath, document, elementId) || changed;
//        changed = fixMultipleVersionDateProblem(xpath, document, elementId) || changed;
//        changed = removeBreakLineInExpressionName(xpath, document, elementId) || changed;
//        changed = removeEmptyTextComponents(xpath, document, elementId) || changed;
        changed = addMissingWriteHtml(xpath, document, elementId) || changed;
//        changed = addMissingTextComponentId(xpath, document, elementId) || changed;
//        changed = fixMissingFontStyle7PtUponOpenInEditor(xpath, document, elementId) || changed; // CANO Specific
//        changed = removeAttributesOnBR(xpath, document, elementId) || changed;
        changed = removeEmptyTBody(xpath, document, elementId) || changed;
        changed = removeIncompleteTable(xpath, document, elementId) || changed;
//        changed = replace0PXwith0PT(xpath, document, elementId) || changed;
//        changed = removeLineHeigh0PTWhenNoContent(xpath, document, elementId) || changed;
//        changed = removeTopAndBottomMargin(xpath, document, elementId) || changed;
//        changed = removeEmptyColGroups(xpath, document, elementId) || changed;
//        changed = removeLangFromSpan(xpath, document, elementId) || changed;
//        changed = removeMSOAnsiLanguageFromSpan(xpath, document, elementId) || changed;
//        changed = changeSpanLineHeightToDiv(xpath, document, elementId) || changed;
//        changed = handleSpecialCase1(xpath, document, elementId) || changed;		// for Element BDZH_P
//        changed = removeDoubleTd(xpath, document, elementId) || changed;
//        changed = fixFontWeightBold(xpath, document, elementId) || changed;
//        changed = fixTdStyleProblem(xpath, document, elementId) || changed;
//        changed = fixWriteHtmlBrProblem(xpath, document, elementId) || changed;
//        changed = fixSpanTableProblem(xpath, document, elementId) || changed;
//        changed = removeEmptyDiv(xpath, document, elementId) || changed;
//        changed = addMissingColgroup(xpath, document, elementId) || changed;
//        changed = fixWriteStrong(xpath, document, elementId) || changed;
        changed = fixColWithTextAlign(xpath, document, elementId) || changed;
//        changed = removeStyleInTR(xpath, document, elementId) || changed;
//        changed = removeStyleInOL(xpath, document, elementId) || changed;
        changed = removeStyleInTable(xpath, document, elementId) || changed;
//        changed = fixWriteSpan(xpath, document, elementId) || changed;
//        changed = fixDivWithFontSize(xpath, document, elementId) || changed;
//        changed = removeMozStyleInTD(xpath, document, elementId) || changed;
//        changed = fixSpanTable(xpath, document, elementId) || changed;
        changed = replaceSpacesInVarIds(xpath, document, elementId) || changed;
//        changed = individualFixer.fixIndividual(xpath, document, elementId) || changed;
//        changed = CustomerSpecificFixes.fixIt(xpath, document, elementId) || changed;

        return changed;
    }

    private boolean handleEmbeddedExpressions(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String embExpXpath = "//write:a[starts-with(@href, 'textexpemb:')]";
        NodeList embExps = (NodeList) xpath.evaluate(embExpXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("handleEmbeddedExpressions", embExps, elementId);

        boolean changed = false;
        for (int i = 0; i < embExps.getLength(); i++) {
            Element embExp = (Element) embExps.item(i);
            String expression = embExp.getAttribute("href").substring("textexpemb:".length());

            try (InputStream is = new ByteArrayInputStream(expression.getBytes(StandardCharsets.UTF_8))) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document embExpDoc = builder.parse(is);

                changed = fixAllTheProblems(xpath, embExpDoc, elementId) || changed;

                if (changed) {
                    expression = XMLUtils.serializeAsString(embExpDoc);
                    embExp.setAttribute("href", "textexpemb:" + expression);
                }
            } catch (Exception e) {
                LOGGER.error("EmbExp parsing failed for element - {}", elementId, e);
            }

        }
        return changed;
    }

    private boolean replaceSpacesInVarIds(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spacesInVarIds = "//write:a[contains(@class,'fck_variable') and contains(@id,' ')]";
        NodeList aList = (NodeList) xpath.evaluate(spacesInVarIds, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("replaceSpacesInVarIds", aList, elementId);

        boolean changed = false;
        for (int i = 0; i < aList.getLength(); i++) {
            Element a = (Element) aList.item(i);
            String id = a.getAttribute("id");
            String newId = id.replace(' ', '_');
            a.setAttribute("id", newId);
            changed = true;
        }
        return changed;
    }

    private boolean fixDivWithFontSize(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String divWithFontSize = "//div[contains(@style,'font-size')]";
        NodeList divList = (NodeList) xpath.evaluate(divWithFontSize, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixColWithTextAlign", divList, elementId);

        boolean changed = false;
        for (int i = 0; i < divList.getLength(); i++) {
            Element div = (Element) divList.item(i);
            NodeList divInTree = (NodeList) xpath.evaluate("//div",div,XPathConstants.NODESET);
            if (divInTree.getLength() >0){
                LOGGER.warn("Could not replace div with span due to divs in the tree for elementid {}. Just removed font-size", elementId);
                String divStyle = removeAttributeFromStyle(div.getAttribute("style"), "font-size");
                if(divStyle.length() > 0) {
                    div.setAttribute("style",divStyle);
                } else {
                    div.removeAttribute("style");
                }
            } else {
                Element divParent = (Element) div.getParentNode();
                Element span = (Element) div.cloneNode(true);

                document.renameNode(span, null, "span");

                //remove every attribute except style
                NamedNodeMap spanAttributes = span.getAttributes();
                for (int j = 0; j < spanAttributes.getLength(); j++) {
                    String attrName = spanAttributes.item(j).getNodeName();
                    if (!"style".equalsIgnoreCase(attrName)) {
                        span.removeAttribute(attrName);
                    }
                }
                String[] elements = span.getAttribute("style").split(";");
                String divStyle = removeAttributeFromStyle(span.getAttribute("style"), "font-size");
                String spanStyle = null;

                for (String element : elements) {
                    if (ltrim(element).startsWith("font-size")) {
                        spanStyle = element + ";";
                        break;
                    }
                }
                span.setAttribute("style", spanStyle);
                Element newDiv = document.createElement("div");
                newDiv.appendChild(span);
                if (divStyle.length() > 0) {
                    newDiv.setAttribute("style", divStyle);
                }
                divParent.replaceChild(newDiv, div);
            }
            changed = true;
        }
        return changed;
    }

    private boolean fixSpanTable(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spanTable = "//span/table";
        NodeList tablelList = (NodeList) xpath.evaluate(spanTable, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixSpanTable", tablelList, elementId);

        boolean changed = false;
        for (int i = 0; i < tablelList.getLength(); i++) {
            Element table = (Element) tablelList.item(i);
            Element span = (Element) table.getParentNode();
            Element spanParent = (Element) span.getParentNode();
            Element tableClone = (Element) table.cloneNode(true);
            spanParent.replaceChild(tableClone,span);
            changed = true;
        }
        return changed;
    }

    private boolean removeMozStyleInTD(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tdWithMozStyle = "//td[contains(@style,'-moz-')]";
        NodeList tdList = (NodeList) xpath.evaluate(tdWithMozStyle, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeMozStyleInTD", tdList, elementId);

        boolean changed = false;
        for (int i = 0; i < tdList.getLength(); i++) {
            Element td = (Element) tdList.item(i);
            String style = td.getAttribute("style");

            String[] styleAttributes = style.split(";");
            StringBuilder newStyle = new StringBuilder();
            for(String styleAttribute : styleAttributes){
                if(!ltrim(styleAttribute).startsWith("-moz-")){
                    newStyle.append(styleAttribute).append("; ");
                }
            }
            td.setAttribute("style", newStyle.toString());
            changed = true;
        }
        return changed;
    }

    private boolean removeStyleInTR(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String trWithStyle = "//tr[@style]";
        NodeList trList = (NodeList) xpath.evaluate(trWithStyle, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeStyleInTR", trList, elementId);

        boolean changed = false;
        for (int i = 0; i < trList.getLength(); i++) {
            Element tr = (Element) trList.item(i);
            tr.removeAttribute("style");
            changed = true;
        }
        return changed;
    }

    private boolean removeStyleInOL(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String olWithStyle = "//ol[@style]";
        NodeList olList = (NodeList) xpath.evaluate(olWithStyle, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeStyleInOL", olList, elementId);

        boolean changed = false;
        for (int i = 0; i < olList.getLength(); i++) {
            Element ol = (Element) olList.item(i);
            ol.removeAttribute("style");
            changed = true;
        }
        return changed;
    }

    private boolean removeStyleInTable(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tableWithStyle = "//table[@style]";
        NodeList tableList = (NodeList) xpath.evaluate(tableWithStyle, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeStyleInTable", tableList, elementId);

        boolean changed = false;
        for (int i = 0; i < tableList.getLength(); i++) {
            Element table = (Element) tableList.item(i);
            table.removeAttribute("style");
            changed = true;
        }
        return changed;
    }

    private boolean fixColWithTextAlign(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String colWithTextAlign= "//col[contains(@style,'text-align')]";
        NodeList colList = (NodeList) xpath.evaluate(colWithTextAlign, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixColWithTextAlign", colList, elementId);

        boolean changed = false;
        for (int i = 0; i < colList.getLength(); i++) {
            Element col = (Element) colList.item(i);
            String style = col.getAttribute("style");
            col.setAttribute("style", removeAttributeFromStyle(style, "text-align"));
            changed = true;
        }
        return changed;
    }

    private boolean fixWriteStrong(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String writeStrong= "//Text/*/strong";
        NodeList strongList = (NodeList) xpath.evaluate(writeStrong, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixWriteStrong", strongList, elementId);

        boolean changed = false;
        for (int i = 0; i < strongList.getLength(); i++) {
            Element strong = (Element) strongList.item(i);
            Element write = (Element) strong.getParentNode();

            Element div = document.createElement("div");
            Element strongClone = (Element) strong.cloneNode(true);
            div.appendChild(strongClone);

            write.replaceChild(div,strong);
            changed = true;
        }
        return changed;
    }

    private boolean fixWriteSpan(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String writeSpan= "//Text/*/span";
        NodeList spanList = (NodeList) xpath.evaluate(writeSpan, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixWriteStrong", spanList, elementId);

        boolean changed = false;
        for (int i = 0; i < spanList.getLength(); i++) {
            Element span = (Element) spanList.item(i);
            Element write = (Element) span.getParentNode();

            Element div = document.createElement("div");
            Element spanClone = (Element) span.cloneNode(true);
            div.appendChild(spanClone);

            write.replaceChild(div,span);
            changed = true;
        }
        return changed;
    }

    private boolean addMissingColgroup(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        // adds a colgroup with one single column, which might be not sufficient

        String tableTbody= "//table[not(colgroup) and (tbody)]";
        NodeList tableList = (NodeList) xpath.evaluate(tableTbody, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("addMissingColgroup", tableList, elementId);

        boolean changed = false;
        for (int i = 0; i < tableList.getLength(); i++) {
            Element table = (Element) tableList.item(i);
            NodeList tmp = (NodeList) xpath.evaluate("tbody",table,XPathConstants.NODESET);
            Element tbody = (Element) tmp.item(0);

            Element colgroup = document.createElement("colgroup");
            Element col = document.createElement("col");
            colgroup.appendChild(col);

            table.removeChild(tbody);
            table.appendChild(colgroup);
            table.appendChild(tbody);
            changed = true;
        }
        return changed;
    }

    private boolean removeEmptyDiv(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String emptyDiv= "//div[not(*) and contains(class,'fck_empty_element')]";
        NodeList emptyDivList = (NodeList) xpath.evaluate(emptyDiv, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeEmptyDiv", emptyDivList, elementId);

        boolean changed = false;
        for (int i = 0; i < emptyDivList.getLength(); i++) {
            Element div = (Element) emptyDivList.item(i);
            div.getParentNode().removeChild(div);
            changed = true;
        }
        return changed;
    }

    private boolean fixSpanTableProblem(XPath xpath, Document document, String elementId) throws XPathExpressionException {

        String problemString= "//span/table";
        NodeList problemList = (NodeList) xpath.evaluate(problemString, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixSpanTableProblem", problemList, elementId);

        boolean changed = false;
        for (int i = 0; i < problemList.getLength(); i++) {
            Element table = (Element) problemList.item(i);
            Element span = (Element) table.getParentNode();
            Element spansParent = (Element)span.getParentNode();

            String tdString = "tbody/tr/td";

            NodeList tdList = (NodeList) xpath.evaluate(tdString,table,XPathConstants.NODESET);

            for(int j=0;j < tdList.getLength();j++){
                Element td = (Element) tdList.item(j);
                NodeList divList = td.getElementsByTagName("div");
                if (divList.getLength() == 0) {
                    Element tmp = wrapWithSpan(document,span,td);
                    while(td.hasChildNodes()) {
                        td.removeChild(td.getFirstChild());
                    }
                    td.setTextContent("");
                    td.appendChild(tmp);
                } else {
                    for(int k=0;k < divList.getLength();k++){
                        Element div = (Element) divList.item(k);
                        Element tmp = wrapWithSpan(document,span,div);
                        while(div.hasChildNodes()) {
                            div.removeChild(div.getFirstChild());
                        }
                        div.setTextContent("");
                        div.appendChild(tmp);
                    }
                }
            }

            spansParent.replaceChild(table,span);
            changed = true;
        }
        return changed;

    }

    private Element wrapWithSpan(Document doc, Element span, Element toWrap) {
        Element newSpan = doc.createElement("span");

        // copy attributes to new node
        NamedNodeMap spanAttributes = span.getAttributes();
        for (int i = 0; i < spanAttributes.getLength(); i++) {
            Node attribute = spanAttributes.item(i);
            newSpan.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }

        //copy nodes of toWrap
        NodeList nodes = toWrap.getChildNodes();
        for(int i=0;i < nodes.getLength();i++){
            newSpan.appendChild(nodes.item(i));
        }

        // copy text content if any
        String textContent = toWrap.getTextContent();

        if(textContent != null && textContent.length()>0) {
            newSpan.setTextContent(textContent);
        }

        return newSpan;
    }


    private boolean fixWriteHtmlBrProblem(XPath xpath, Document document, String elementId) throws XPathExpressionException {

        String problemString= "//write:html/br";
        NodeList problemList = (NodeList) xpath.evaluate(problemString, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixWriteHtmlBrProblem", problemList, elementId);

        boolean changed = false;
        for (int i = 0; i < problemList.getLength(); i++) {
            Element oldBr = (Element) problemList.item(i);
            Element writeElement = (Element) oldBr.getParentNode();
            Element div = document.createElement("div");
            Element br = document.createElement("br");
            div.appendChild(br);
            writeElement.appendChild(div);
            writeElement.replaceChild(div,oldBr);

            changed = true;
        }
        return changed;

    }

    private boolean fixTdStyleProblem(XPath xpath, Document document, String elementId) throws XPathExpressionException {

        String tdWithStyleProblem= "//td[contains(@style,': ;') ]";
        NodeList tdList = (NodeList) xpath.evaluate(tdWithStyleProblem, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixTdStyle", tdList, elementId);

        boolean changed = false;
        for (int i = 0; i < tdList.getLength(); i++) {
            Element tdElement = (Element) tdList.item(i);
            String styleValue = tdElement.getAttribute("style");

            String[] styleElements = styleValue.split(";");
            StringBuilder newStyleValue = new StringBuilder();

            for(String styleElement : styleElements){
                if(styleElement.charAt(styleElement.length()-1) != ' '){
                    newStyleValue.append(styleElement).append("; ");
                }
            }

            tdElement.setAttribute("style",newStyleValue.toString());

            changed = true;
        }
        return changed;

    }

    private boolean fixFontWeightBold(XPath xpath, Document document, String elementId) throws XPathExpressionException {

        String spanWithFontWeightBold= "//span[contains(@style,'font-weight: bold') ]";
        NodeList spanList = (NodeList) xpath.evaluate(spanWithFontWeightBold, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixFontWeightBold", spanList, elementId);

        boolean changed = false;
        for (int i = 0; i < spanList.getLength(); i++) {
            Element spanElement = (Element) spanList.item(i);
            String content = spanElement.getTextContent();
            Node parent = spanElement.getParentNode();
            Element strong = document.createElement("strong");
            strong.setTextContent(content);
            parent.replaceChild(strong,spanElement);
            changed = true;
        }
        return changed;

    }

    private boolean fixUidProblem(XPath xpath, Document document, String elementId) throws XPathExpressionException {

        String ifWithUid = "//If[@uId]";
        NodeList ifs = (NodeList) xpath.evaluate(ifWithUid, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixUidProblem", ifs, elementId);

        boolean changed = false;
        for (int i = 0; i < ifs.getLength(); i++) {
            Element ifElement = (Element) ifs.item(i);
            ifElement.setAttribute("uID", ifElement.getAttribute("uId"));
            ifElement.removeAttribute("uId");
            changed = true;
        }
        return changed;
    }

    private boolean convertParagraphToDiv(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String paragraphXpath = "//p";
        NodeList paragraphs = (NodeList) xpath.evaluate(paragraphXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("convertParagraphToDiv", paragraphs, elementId);

        boolean changed = false;
        for (int i = 0; i < paragraphs.getLength(); i++) {
            Element paragraph = (Element) paragraphs.item(i);
            document.renameNode(paragraph, "", "div");
            changed = true;
        }
        return changed;
    }

    private boolean fixMissingDefaultInSwitch(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String switchXpath = "//Switch[not(Default)]";
        NodeList switchs = (NodeList) xpath.evaluate(switchXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixMissingDefaultInSwitch", switchs, elementId);

        boolean changed = false;
        for (int i = 0; i < switchs.getLength(); i++) {
            Element switchElement = (Element) switchs.item(i);

            Element defaultElement = document.createElement("Default");
            switchElement.appendChild(defaultElement);

            Element textElement = document.createElement("Text");
            defaultElement.appendChild(textElement);

            changed = true;
        }
        return changed;
    }

    private boolean fixMissingLocTextTextStructure(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        boolean changed = fixMissingLocTextTextStructure("Case", xpath, document, elementId);
        changed = fixMissingLocTextTextStructure("Default", xpath, document, elementId) || changed;
        changed = fixMissingLocTextTextStructure("Then", xpath, document, elementId) || changed;
        changed = fixMissingLocTextTextStructure("Else", xpath, document, elementId) || changed;
        return changed;
    }


    /*
     *               <Parent>                   <Parent>
     *  Convert          <Text/>      to            <LocTexts>
     *               </Parent>                          <LocText>
     *                                                      <Text>
     *                                                      <Locales>
     *                                                          <Locale/>
     *                                                      </Locales>
     * */
    private boolean fixMissingLocTextTextStructure(String parent, XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String caseTextXpath = "//" + parent + "/Text";
        NodeList nodes = (NodeList) xpath.evaluate(caseTextXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixMissingLocTextTextStructure", nodes, elementId);

        boolean changed = false;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element textNode = (Element) nodes.item(i);
            Element caseNode = (Element) textNode.getParentNode();
            caseNode.removeChild(textNode);

            Element locTextsNode = document.createElement("LocTexts");
            caseNode.appendChild(locTextsNode);

            Element locTextNode = document.createElement("LocText");
            locTextsNode.appendChild(locTextNode);

            locTextNode.appendChild(textNode);
            Element localesNode = document.createElement("Locales");
            locTextNode.appendChild(localesNode);

            Element localeNode = document.createElement("Locale");
            localesNode.appendChild(localeNode);

            Element masterNode = document.createElement("Master");
            masterNode.setTextContent("false");
            locTextNode.appendChild(masterNode);

            changed = true;
        }
        return changed;
    }

    private boolean fixMissingTableWidthProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String colXpath = "//colgroup/col[not(@width) and not(contains(@style, 'width'))]";
        NodeList cellNodes = (NodeList) xpath.evaluate(colXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixMissingTableWidthProblem", cellNodes, elementId);

        boolean changed = false;
        for (int i = 0; i < cellNodes.getLength(); i++) {
            double desiredWidth = 16.5;
            Element col = (Element) cellNodes.item(i);
            Element colgroup = (Element) col.getParentNode();
            NodeList cols = colgroup.getChildNodes();
            for (int j = 0; j < cols.getLength(); j++) {
                Node childNode = cols.item(j);
                if (childNode instanceof Text) {
                    continue;
                }
                Element colSibling = (Element) childNode;
                if (colSibling.hasAttribute("width")) {
                    double width = Double.parseDouble(colSibling.getAttribute("width").replaceAll("cm", ""));
                    desiredWidth -= width;
                } else if (colSibling.hasAttribute("style")
                        && colSibling.getAttribute("style").toLowerCase().contains("width")) {
                    String style = colSibling.getAttribute("style");
                    String widthStr1 = style.substring(style.indexOf("width"));
                    String widthStr = widthStr1.replaceFirst("width\\s*:\\s*", "");
                    String widthValueStr = widthStr.substring(0, widthStr.indexOf("cm"));
                    double width = Double.parseDouble(widthValueStr);
                    desiredWidth -= width;
                }
            }
            col.setAttribute("style", "width: " + desiredWidth + "cm");
            changed = true;
        }
        return changed;
    }

    private boolean fixReadOnlyProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String write_a_Xpath = "//*[@contenteditable]";
        NodeList write_a_s = (NodeList) xpath.evaluate(write_a_Xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixReadOnlyProblem", write_a_s, elementId);

        boolean changed = false;
        for (int i = 0; i < write_a_s.getLength(); i++) {
            Element write_a = (Element) write_a_s.item(i);
            write_a.removeAttribute("contenteditable");
            changed = true;
        }

        return changed;
    }

    /* vertial-align to vertical-align */
    private boolean fixVerticalAlignProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String td_with_style_xpath = "//td[contains(@style, 'vertial-align')]";
        NodeList td_s = (NodeList) xpath.evaluate(td_with_style_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixVerticalAlignProblem", td_s, elementId);

        boolean changed = false;
        for (int i = 0; i < td_s.getLength(); i++) {
            Element td = (Element) td_s.item(i);
            td.setAttribute("style", td.getAttribute("style").replaceAll("vertial-align", "vertical-align"));
            changed = true;
        }
        return changed;
    }

    /*
     * cvc-complex-type.3.2.2: Attribute 'style' is not allowed to appear in element
     * 'tr'.
     *
     * <tr style must be removed and inserted into the cell inside the row (<td>
     *
     * 65 elements
     * 1960274605.1535362709299.10834511893527004.16.1003186756.textcomponent
     *
     *
     */
    /*
     * cvc-complex-type.3.2.2: Attribute 'style' is not allowed to appear in element
     * 'tr'. 1294573215.1533734842652.89074150329801704.89.1003186756
     *
     * Move table formatting to css
     */
    private boolean fixTRStyleProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String tr_with_style_xpath = "//tr[@style]";
        NodeList tr_s = (NodeList) xpath.evaluate(tr_with_style_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixTRStyleProblem", tr_s, elementId);

        boolean changed = false;
        for (int i = 0; i < tr_s.getLength(); i++) {
            Element tr = (Element) tr_s.item(i);
            if ("vertical-align: top".equals(tr.getAttribute("style"))) {
                NodeList td_s = tr.getChildNodes();
                for (int j = 0; j < td_s.getLength(); j++) {
                    Element td = (Element) td_s.item(j);
                    applyStyleIfNotPresent(td, "vertical-align", "top");
                    changed = true;
                }
                tr.removeAttribute("style");
            } else {
//                throw new IllegalArgumentException("Unexpected style attribute for TR, elementId - " + elementId);
            }
        }
        return changed;
    }

    /*
     * cvc-complex-type.3.2.2: Attribute 'align' is not allowed to appear in element
     * 'td'. 1469843303.1296716677475.93941442297688924.0.1003186756
     *
     * Move table formatting to css
     */
    private boolean fixAttributeAlignProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String td_with_align_xpath = "//td[@align]";
        NodeList td_s = (NodeList) xpath.evaluate(td_with_align_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixAttributeAlignProblem", td_s, elementId);

        boolean changed = false;
        for (int i = 0; i < td_s.getLength(); i++) {
            Element td = (Element) td_s.item(i);
            if ("left".equals(td.getAttribute("align"))) {
                td.removeAttribute("align");
            } else if ("right".equals(td.getAttribute("align"))) {
                applyStyleIfNotPresent(td, "text-align", "right");
                td.removeAttribute("align");
                changed = true;
            } else {
                throw new IllegalArgumentException("Unexpected align attribute '" + td.getAttribute("align")
                        + "' for TD, elementId - " + elementId);
            }
        }
        return changed;
    }

    private boolean fixAttributeVAlignProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String td_with_valign_xpath = "//td[@valign]";
        NodeList td_s = (NodeList) xpath.evaluate(td_with_valign_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixAttributeVAlignProblem", td_s, elementId);

        boolean changed = false;
        for (int i = 0; i < td_s.getLength(); i++) {
            Element td = (Element) td_s.item(i);
            if ("top".equals(td.getAttribute("valign"))) {
                applyStyleIfNotPresent(td, "vertical-align", "top");
                td.removeAttribute("valign");
                changed = true;
            } else {
                throw new IllegalArgumentException("Unexpected valign attribute '" + td.getAttribute("valign")
                        + "' for TD, elementId - " + elementId);
            }
        }
        return changed;
    }


    /*
     * cvc-complex-type.2.4.d: Invalid content was found starting with element
     * 'font'. No child element is expected at this point.
     *
     * 175 tsdocuments
     */
    /*
     * cvc-complex-type.2.4.d: Invalid content was found starting with element
     * 'font'. 708 - 175 elements
     */
    private boolean fixFontTagProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String font_xpath = "//font";
        NodeList font_s = (NodeList) xpath.evaluate(font_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixFontTagProblem", font_s, elementId);

        boolean changed = false;
        for (int i = 0; i < font_s.getLength(); i++) {
            Element font = (Element) font_s.item(i);
            NamedNodeMap map = font.getAttributes();

            if (map.getLength() == 0 || (map.getLength() == 1 && "size".equalsIgnoreCase(map.item(0).getNodeName()))) {
                Node parent = font.getParentNode();
                NodeList children = font.getChildNodes();
                while (children.getLength() > 0) {
                    parent.insertBefore(children.item(0), font);
                }
                parent.removeChild(font);
                changed = true;
            } else if (map.getLength() == 1 && "color".equalsIgnoreCase(map.item(0).getNodeName())) {
                // <font color="#000000">[  ]</font> -> <span style="color: #666667">
                font = (Element) document.renameNode(font, "", "span");
                font.setAttribute("style", "color: " + font.getAttribute("color"));
                font.removeAttribute("color");
                changed = true;
            } else if (map.getLength() == 1 && "style".equalsIgnoreCase(map.item(0).getNodeName()) && map.item(0)
                    .getNodeValue().matches("background-color: #[0-9a-f]{6}")) {
                // style -> background-color: #xxxxxx -> span with style background-color
                document.renameNode(font, "", "span");
                changed = true;
            } else {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < map.getLength(); j++) {
                    Node node = map.item(j);
                    sb.append(node.getNodeName()).append("=").append(node.getNodeValue()).append("; ");
                }
                throw new IllegalArgumentException(
                        "Unexpected font element '" + sb.toString() + "', elementId - " + elementId);
            }
        }
        return changed;
    }

    /*
     * cvc-complex-type.2.4.a: Invalid content was found starting with element
     * 'div'. One of '{strong, b, em, u, strike, sub, sup, span, br, img,
     * "http://www.assentis.com/write/html":a}' is expected.
     *
     * ??? 4461 elements from which 20+ are TC-s and expressions
     * 680085891.1354025956492.89206503683931593.142.1003186756
     *
     * Problem: div tags are inside of span tags
     *
     * remove spans that have divs & manual fix for TC-s and expressions:
     *
     * 1. run migrate step 3 2. open the TC in the editor 3. the editor will
     * sanitize it 4. save the TC
     *
     * For the tsdocuments we have to look with Ankush
     *
     */
    private boolean removeEmptySpansProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String span_xpath = "//span[not(@*) or (count(@*)=1 and @id)]";
        NodeList span_s = (NodeList) xpath.evaluate(span_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeEmptySpansProblem", span_s, elementId);

        boolean changed = false;

        while (span_s.getLength() > 0) {
            Element span = (Element) span_s.item(0);
            Node spanParent = span.getParentNode();
            NodeList childNodes = span.getChildNodes();
            while (childNodes.getLength() > 0) {
                spanParent.insertBefore(childNodes.item(0), span);
            }
            spanParent.removeChild(span);
            changed = true;
            span_s = (NodeList) xpath.evaluate(span_xpath, document, XPathConstants.NODESET);
        }

        return changed;
    }

    private boolean fixTableInsideDivProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {
        String table_xpath = "//div[not(@*) and count(*|text()) = 1]/table";
        NodeList table_s = (NodeList) xpath.evaluate(table_xpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixTableInsideDivProblem", table_s, elementId);

        boolean changed = false;

        while (table_s.getLength() > 0) {
            Element table = (Element) table_s.item(0);
            Node div = table.getParentNode();
            Node divParent = div.getParentNode();
            NodeList childNodes = div.getChildNodes();
            while (childNodes.getLength() > 0) {
                divParent.insertBefore(childNodes.item(0), div);
            }
            divParent.removeChild(div);
            changed = true;
            table_s = (NodeList) xpath.evaluate(table_xpath, document, XPathConstants.NODESET);

        }
        return changed;
    }

    private boolean addMissingDivs(XPath xpath, Document document, String elementId) throws XPathExpressionException {
        String writeHtmlWithNoDiv = "//Text/write:html[count(*) = 0 and count(text()) = 1]";
        NodeList writeHtmls = (NodeList) xpath.evaluate(writeHtmlWithNoDiv, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("addMissingDivs", writeHtmls, elementId);

        boolean changed = false;
        for (int i = 0; i < writeHtmls.getLength(); i++) {
            Element writeHtml = (Element) writeHtmls.item(i);
            Node text = writeHtml.getFirstChild();
            writeHtml.removeChild(text);

            Element div = document.createElement("div");
            div.appendChild(text);

            writeHtml.appendChild(div);

            changed = true;
        }
        return changed;
    }

    private boolean fixMultipleVersionDateProblem(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String textComponentXpath = "//TextComponent[count(VersionDate) > 1]";
        NodeList textComponents = (NodeList) xpath.evaluate(textComponentXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixMultipleVersionDateProblem", textComponents, elementId);

        boolean changed = false;
        for (int i = 0; i < textComponents.getLength(); i++) {
            Element textComponent = (Element) textComponents.item(i);

            NodeList versionDates = textComponent.getElementsByTagName("VersionDate");

            for (int j = 0; j < versionDates.getLength(); j++) {
                Element versionDate = (Element) versionDates.item(j);
                if (StringUtils.isBlank(versionDate.getTextContent())) {
                    textComponent.removeChild(versionDate);
                    break;
                }
            }

            changed = true;
        }
        return changed;
    }

    private boolean removeBreakLineInExpressionName(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String breakLineXpath = "//write:a/br";
        NodeList brs = (NodeList) xpath.evaluate(breakLineXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeBreakLineInExpressionName", brs, elementId);

        boolean changed = false;
        for (int i = 0; i < brs.getLength(); i++) {
            Element br = (Element) brs.item(i);
            br.getParentNode().removeChild(br);
            changed = true;
        }
        return changed;
    }

    /**
     * Removes the textcomponents without element id reference and with empty text tags
     */
    private boolean removeEmptyTextComponents(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        NodeList emptyTCs = (NodeList) xpath.evaluate(EMPTY_COMPONENT_XPATH, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeEmptyTextComponents", emptyTCs, elementId);

        boolean changed = false;
        for (int i = 0; i < emptyTCs.getLength(); i++) {
            Element emptyTC = (Element) emptyTCs.item(i);
            emptyTC.getParentNode().removeChild(emptyTC);

            changed = true;
        }
        return changed;
    }

    private boolean removeEmptyTBody(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tBody = "//tbody[not(tr)]";
        NodeList tBodies = (NodeList) xpath.evaluate(tBody, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeEmptyTBody", tBodies, elementId);

        boolean changed = false;
        for (int i = 0; i < tBodies.getLength(); i++) {
            Node tBodyNode = tBodies.item(i);
            tBodyNode.getParentNode().removeChild(tBodyNode);
            changed = true;
        }
        return changed;
    }

    private boolean removeIncompleteTable(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tableXPath = "//table[not(thead) and not(tbody)]";
        NodeList tableList = (NodeList) xpath.evaluate(tableXPath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeIncompleteTable", tableList, elementId);

        boolean changed = false;
        for (int i = 0; i < tableList.getLength(); i++) {
            Element table = (Element) tableList.item(i);
            table.getParentNode().removeChild(table);
            changed = true;
        }
        return changed;
    }

    private boolean removeDoubleTd(XPath xpath, Document document, String elementId) throws Exception {

        String tdXPath = "//tr/td[not(*)]";
        NodeList tdList = (NodeList) xpath.evaluate(tdXPath, document, XPathConstants.NODESET);
        NodeList secondList = (NodeList) xpath.evaluate("//tr/td/td[not(*)]", document, XPathConstants.NODESET);

        boolean changed = false;

        if (tdList.getLength() > 0 && secondList.getLength() > 0) {
            debugXpathEvaluationContent("removeDoubleTd", tdList, elementId);

            for (int j = 0; j < tdList.getLength(); j++) {
                Element trtdWithContent = (Element) tdList.item(j).getNextSibling();

                NodeList divs = (NodeList) xpath.evaluate("td/div", trtdWithContent, XPathConstants.NODESET);

                NodeList liste = trtdWithContent.getChildNodes();

                while (liste.getLength() > 0) {
                    Element td = (Element) liste.item(0);
                    trtdWithContent.removeChild(td);
                }
                for (int i = 0; i < divs.getLength(); i++) {
                    trtdWithContent.appendChild(divs.item(i));
                }
                changed = true;
            }
        }
        return changed;
    }

    private boolean replace0PXwith0PT(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tdWith0pxXPath = "//td[contains(@style,'0px')]";
        NodeList tdList = (NodeList) xpath.evaluate(tdWith0pxXPath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("replace0PXwith0PT", tdList, elementId);

        boolean changed = false;
        for (int i = 0; i < tdList.getLength(); i++) {
            Element td = (Element) tdList.item(i);
            td.setAttribute("style", td.getAttribute("style").replace("0px", "0pt"));
            changed = true;
        }
        return changed;
    }

    private boolean removeLineHeigh0PTWhenNoContent(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String tdWithLineHeight0PZXPath = "//td[contains(@style,'line-height:0pt;')]";
        NodeList tdList = (NodeList) xpath.evaluate(tdWithLineHeight0PZXPath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeLineHeigh0PTWhenNoContent", tdList, elementId);

        boolean changed = false;
        for (int i = 0; i < tdList.getLength(); i++) {
            Element td = (Element) tdList.item(i);
            if(td.getTextContent() == null || td.getTextContent().trim().length() == 0){
                td.setAttribute("style", td.getAttribute("style").replace("line-height:0pt;", ""));
                changed = true;
            }
        }
        return changed;
    }

    private boolean removeTopAndBottomMargin(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spanWithMarginTop9Pt= "//span[contains(@style,'margin-top:9pt') and  (contains(@style,'margin-bottom:12pt;')) ]";
        NodeList tdList = (NodeList) xpath.evaluate(spanWithMarginTop9Pt, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeTopAndBottomMargin", tdList, elementId);

        boolean changed = false;
        for (int i = 0; i < tdList.getLength(); i++) {
            Element td = (Element) tdList.item(i);
            td.setAttribute("style", td.getAttribute("style").replace("margin-top:9pt", ""));
            td.setAttribute("style", td.getAttribute("style").replace("margin-bottom:12pt;", ""));
            changed = true;
        }
        return changed;
    }

    private boolean handleSpecialCase1(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        if(!elementId.contains("477838489.1556572553458.90380355014242266.6.1417831984")){
            return false;
        }

        String xPathWrite= "//write:html";
        NodeList resultList = (NodeList) xpath.evaluate(xPathWrite, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("handleSpecialCase1", resultList, elementId);

        if(resultList.getLength() != 1){
            LOGGER.error("Unexpected number of {} matches. 1 match is expected. No change was made.", resultList.getLength());
            return false;
        }

        Element writeHtml = (Element) resultList.item(0);
        Node table = (Node) xpath.evaluate("//table", writeHtml, XPathConstants.NODE);

        Node div = (Node) xpath.evaluate("//td/div", table, XPathConstants.NODE);

        String content = div.getTextContent();
        div.setTextContent("");

        Element span = document.createElement("span");
        span.setAttribute("style", "font-size: 8pt;");
        span.setTextContent(content);

        div.appendChild(span);

        Node firstDiv = writeHtml.getFirstChild();
        writeHtml.removeChild(firstDiv);
        writeHtml.appendChild(table);

        Element div2 = document.createElement("div");
        Element br = document.createElement("br");
        div2.appendChild(document.createElement("br"));
        writeHtml.appendChild(div2);

        return true;
    }

    private boolean changeSpanLineHeightToDiv(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spanWithLineHeightt= "//span[contains(@style,'line-height') ]";
        NodeList spanList = (NodeList) xpath.evaluate(spanWithLineHeightt, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("changeSpanLineHeightToDiv", spanList, elementId);

        boolean changed = false;
        for (int i = 0; i < spanList.getLength(); i++) {
            Element element = (Element) spanList.item(i);
            Document doc = element.getOwnerDocument();
            doc.renameNode(element, null, "div");
            changed = true;
        }
        return changed;
    }


    private boolean removeEmptyColGroups(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String emptyColGroup= "//colgroup[not(*)]";
        NodeList colGroupList = (NodeList) xpath.evaluate(emptyColGroup, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeEmptyColGroups", colGroupList, elementId);

        boolean changed = false;
        for (int i = 0; i < colGroupList.getLength(); i++) {
            Element colGroup = (Element) colGroupList.item(i);
            colGroup.getParentNode().removeChild(colGroup);
            changed = true;
        }
        return changed;
    }

    private boolean removeMSOAnsiLanguageFromSpan(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spanWithLang= "//span[contains(@style,'mso-ansi-language')]";
        NodeList spanList = (NodeList) xpath.evaluate(spanWithLang, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeMSOAnsiLanguageFromSpan", spanList, elementId);

        boolean changed = false;
        for (int i = 0; i < spanList.getLength(); i++) {
            Element span = (Element) spanList.item(i);
            span.removeAttribute("style");
            changed = true;
        }
        return changed;
    }

    private boolean removeLangFromSpan(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String spanWithLang= "//span/@lang";
        NodeList spanList = (NodeList) xpath.evaluate(spanWithLang, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeLangFromSpan", spanList, elementId);

        boolean changed = false;
        for (int i = 0; i < spanList.getLength(); i++) {
            Element span = ((Attr) spanList.item(i)).getOwnerElement();
            span.removeAttribute("lang");
            changed = true;
        }
        return changed;
    }

    private boolean addMissingWriteHtml(XPath xpath, Document document, String elementId)
            throws XPathExpressionException {

        String missingWriteHtmlXpath = "//LocText/Text[not(write:html) and not(a:doc-fragment)]";
        NodeList texts = (NodeList) xpath.evaluate(missingWriteHtmlXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("addMissingWriteHtml", texts, elementId);

        boolean changed = false;
        for (int i = 0; i < texts.getLength(); i++) {
            Element text = (Element) texts.item(i);

            Element writeHtml = document.createElementNS("http://www.assentis.com/write/html", "write:html");
            writeHtml.setAttribute("xmlns:write", "http://www.assentis.com/write/html");
            text.appendChild(writeHtml);

            changed = true;
        }
        return changed;
    }

    /**
     * Adds the text component's expression id, if it wasn't already set
     * The id will be the component's name
     */
    private boolean addMissingTextComponentId(XPath xpath, Document document, String elementId)
            throws Exception {

        NodeList texts = (NodeList) xpath.evaluate(MISSING_COMPONENT_ID_XPATH, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("addMissingTextComponentId", texts, elementId);

        boolean changed = false;
//        for (int i = 0; i < texts.getLength(); i++) {
//            Element component = (Element) texts.item(i);
//
//            final String reference = component.getElementsByTagName("Reference").item(0).getTextContent();
//            final String componentName = ResourceName.getResourceName(reference);
//
//            final Element expression = (Element) component.getElementsByTagName("Expression").item(0);
//
//            expression.setAttribute("id", componentName);
//            changed = true;
//        }
        return changed;
    }

    /**
     * Converts
     *      <div>
     *          <span style="font-size: 7pt">
     *              <div></div>
     *          </span>
     *      </div>
     * to
     *      <div>
     *          <span style="font-size: 7pt">
     *              Happiness!
     *          </span>
     *      </div>
     */
    private boolean fixMissingFontStyle7PtUponOpenInEditor(XPath xpath, Document document, String elementId)
            throws Exception {

        String offendingDivXpath = "//div/span[@style='font-size: 7pt']/div";
        NodeList divs = (NodeList) xpath.evaluate(offendingDivXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("fixMissingFontStyle7PtUponOpenInEditor", divs, elementId);

        boolean changed = false;
        for (int i = 0; i < divs.getLength(); i++) {
            Element div = (Element) divs.item(i);
            Node span = div.getParentNode();
            NodeList childNodes = div.getChildNodes();
            while (childNodes.getLength() > 0) {
                span.insertBefore(childNodes.item(0), div);
            }
            span.removeChild(div);
            changed = true;
        }
        return changed;
    }

    private boolean removeAttributesOnBR(XPath xpath, Document document, String elementId)
            throws Exception {

        String offendingBrXpath = "//br[@type]";
        NodeList brs = (NodeList) xpath.evaluate(offendingBrXpath, document, XPathConstants.NODESET);

        debugXpathEvaluationContent("removeAttributesOnBR", brs, elementId);

        boolean changed = false;
        for (int i = 0; i < brs.getLength(); i++) {
            Element br = (Element) brs.item(i);
            br.removeAttribute("type");
            changed = true;
        }
        return changed;
    }

    private void applyStyleIfNotPresent(Element element, String styleName, String styleValue) {
        String style = styleName + ": " + styleValue;
        if (element.hasAttribute("style")) {
            if (!element.getAttribute("style").contains(styleName + ":")) {
                style = style + "; " + element.getAttribute("style");
            } else {
                style = element.getAttribute("style");
            }
        }
        element.setAttribute("style", style);
    }

    public static void debugXpathEvaluationContent(String function, NodeList nodes, String elementId) {
        if (nodes.getLength() > 0) {
            Counter cnt = usage.get(function);
            if (cnt == null){
                cnt = new Counter();
            } else {
                cnt.increment();
            }
            usage.put(function, cnt);

            LOGGER.debug("{} - {} - {}", function, nodes.getLength(), elementId);
        }
    }

    public HashMap<String, Counter> getUsage(){
        return usage;
    }

    private XPath getNamespaceAwareXpath() {
        XPath xPath = new XPathFactoryImpl().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "a":
                        return "http://www.assentis.com/schema/afo";
                    case "fo":
                        return "http://www.w3.org/1999/XSL/Format";
                    case "write":
                        return "http://www.assentis.com/write/html";
                    default:
                        throw new IllegalArgumentException("No prefix provided!");
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<?> getPrefixes(String namespaceURI) {
                return null;
            }
        });
        return xPath;
    }

    private static int BUFFER = 4 * 1024 * 1024;

    public static void main(String[] args) {
        Path inputPromFile = Paths.get(args[0]);
        try {
            System.out.println("Prom processing started at - " + new Date());
            processPromFile(inputPromFile);
            System.out.println("Prom processing finished at - " + new Date());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void processPromFile(Path inputPromFile) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HH.mm.ss").format(new Date());
        Path tempDir = inputPromFile.resolveSibling(timeStamp);

        extractPromFile(inputPromFile, tempDir);

        File[] files = getAllFilesForFixing(tempDir);
        Stream<File> fileStream = Arrays.stream(files);
        fileStream.parallel().forEach(file -> {
            Document document;

            try {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    document = builder.parse(file);
                } catch (final Exception e) {
                    LOGGER.error("Parsing failed for file - {}", file.getPath(), e);
                    return;
                }

                ContentFixer contentFixer = new ContentFixer();
                boolean changed = contentFixer.fixContentProblemsInElement(document, file.getPath());

                if (changed) {
                    try {
                        updateFileWithNewContent(document, file);
                    } catch (final Exception e) {
                        LOGGER.error("Updating failed for file {}", file.getPath(), e);
                    }
                }

            } catch (final Throwable e) {
                LOGGER.error("Fixing failed for file - {}", file.getPath(), e);
                System.exit(-555555); // force break as we have xpath compilation problem
            }
        });

        fileStream.close();
        repackPromFile(inputPromFile, tempDir);
    }

    private static File[] getAllFilesForFixing(Path tempDir) {
        return tempDir.toFile().listFiles(
                (dir, name) -> name.endsWith(".textcomp") || name.endsWith(".textexp") || name.endsWith(".tsdocument"));
    }

    private static void extractPromFile(Path inputPromFile, Path tempDir) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(inputPromFile.toFile()), BUFFER))) {
            if (Files.notExists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            byte[] data = new byte[BUFFER];
            int len;
            String filename;
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                filename = FilenameUtils.getName(zipEntry.getName());
                try (OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(tempDir.resolve(filename).toFile()), BUFFER)) {
                    while ((len = zis.read(data)) > 0) {
                        os.write(data, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void updateFileWithNewContent(Document document, File file)
            throws TransformerException, IOException {
        document.setXmlStandalone(true);
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(document), new StreamResult(sw));
        Files.write(file.toPath(), sw.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void repackPromFile(Path inputPromFile, Path tempDir) throws IOException {

        String tempDirName = tempDir.getFileName().toString();
        String inputPromFileName = inputPromFile.getFileName().toString();
        Path outputPromFile = inputPromFile.resolveSibling(tempDirName + "_" + inputPromFileName);

        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputPromFile.toFile()), BUFFER))) {
            for (File file : tempDir.toFile().listFiles()) {
                try (InputStream is = new BufferedInputStream(new FileInputStream(file), BUFFER)) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    IOUtils.copy(is, zos, BUFFER);
                    zos.closeEntry();
                }
            }
        }
    }

    private String removeAttributeFromStyle(String style, String attribute){
        String[] styleElements = style.split(";");

        StringBuilder newStyle = new StringBuilder();
        for(String element : styleElements){
            if(!ltrim(element).startsWith(attribute)){
                newStyle.append(ltrim(element)).append(";");
            }
        }
        return newStyle.toString();

    }

    private String ltrim(String s){
        return StringUtils.stripStart(s," ");
    }


//    public void setIndividualFixer(IndividualFixer individualFixer) {
//        this.individualFixer = individualFixer;
//    }
}
