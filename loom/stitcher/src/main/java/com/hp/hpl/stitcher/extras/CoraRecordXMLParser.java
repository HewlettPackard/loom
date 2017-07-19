/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.stitcher.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CoraRecordXMLParser {

    // CONSTANTS AND VARIABLES ---------------------------------------------------------------------

    static final String PUBLICATION = "publication";
    static final String AUTHOR = "author";
    static final String TITLE = "title";
    static final String ID = "id";

    static int recordNumber = 1;

    // CONSTANTS AND VARIABLES - END ---------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    // Copied and adapted from http://www.java-samples.com/showtutorial.php?tutorialid=152
    public Collection<CoraRecord> parseXmlFile(String filePath) {
        Document dom = null;

        // Get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse using builder to get DOM representation of the XML file
            dom = db.parse(filePath);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return parseDocument(dom);
    }

    // METHODS - END -------------------------------------------------------------------------------

    // HELPER METHODS ------------------------------------------------------------------------------

    private Collection<CoraRecord> parseDocument(Document dom) {
        Collection<CoraRecord> coraRecords;

        // Get the root element
        Element docEle = dom.getDocumentElement();

        // Get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName(PUBLICATION);

        // Create collection where CoraRecords will be stored
        coraRecords = new ArrayList<CoraRecord>(nl.getLength());

        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {

                // Get the corresponding XML element
                Element el = (Element) nl.item(i);

                // Get CoraRecord and add it to the collection
                CoraRecord c = getCoraRecord(el, recordNumber++);
                coraRecords.add(c);
            }
        }

        return coraRecords;
    }

    private CoraRecord getCoraRecord(Element el, long recordNumber) {
        String publicationId = el.getAttribute(ID);
        String authors = toSingleString(getTextValue(el, AUTHOR));
        String title = toSingleString(getTextValue(el, TITLE));

        return new CoraRecord(Long.toString(recordNumber), publicationId, authors, title, 1);
    }

    private Collection<String> getTextValue(Element ele, String tagName) {
        Collection<String> coll = null;

        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null) {
            coll = new ArrayList<String>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                coll.add(el.getFirstChild().getNodeValue());
            }
        }

        return coll;
    }

    private String toSingleString(Collection<String> coll) {
        if (coll.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean firstRun = true;

        // Append each string and a blank space to the string builder
        for (String s : coll) {
            if (firstRun) {
                sb.append(s);
                firstRun = false;
                continue;
            }

            if ((sb.lastIndexOf(" ") != sb.length() - 1) && (!s.startsWith(" "))) {
                sb.append(' ');
            }
            sb.append(s);
        }

        return sb.toString();
    }

    // HELPER METHODS - END ------------------------------------------------------------------------

}
