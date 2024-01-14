/*
 * MOP Copyright (C) 2023 UTE Corporation
 *
 * WriteXMLFile is part of MOP.
 *
 * MOP is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * MOP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MOP. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package interfaz;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;

public class WriteXMLFile {

    public static void saveXML(String inicio, String fin) {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements (corrida)
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("corrida");
            doc.appendChild(rootElement);

            // parametros generales
            Element parametrosGenerales = doc.createElement("parametrosGenerales");
            rootElement.appendChild(parametrosGenerales);

            // inicio corrida
            Element inicioCorrida = doc.createElement("inicioCorrida");
            inicioCorrida.appendChild(doc.createTextNode(inicio));
            parametrosGenerales.appendChild(inicioCorrida);

            // fin corrida
            Element finCorridaCorrida = doc.createElement("finCorrida");
            finCorridaCorrida.appendChild(doc.createTextNode(fin));
            parametrosGenerales.appendChild(finCorridaCorrida);

            // set attribute to staff element
            Attr inicioUnidadesAttr = doc.createAttribute("unidades");
            inicioUnidadesAttr.setValue("meses");
            inicioCorrida.setAttributeNode(inicioUnidadesAttr);

            // set attribute to staff element
//            Attr finUnidadesAttr = doc.createAttribute("unidades");
//            finUnidadesAttr.setValue("meses");
//            finCorridaCorrida.setAttributeNode(finUnidadesAttr);

            finCorridaCorrida.setAttribute("unidades","meses");

            // shorten way
            // staff.setAttribute("id", "1");


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("generado.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            System.out.println("XML Generado!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public Element dataToXMLNode(Document doc, String nodeName, HashMap<String,String> data) {

        Element root = doc.createElement(nodeName);

        for (HashMap.Entry<String, String> entry : data.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());
            Element entryNode = doc.createElement(entry.getKey());
            entryNode.appendChild(doc.createElement(entry.getValue()));
        }

        return root;
    }
}
