/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package info.ponciano.lab.spalodwfs.mvc.models.semantic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Dr Jean-Jacques Ponciano Contact: jean-jacques@ponciano.info
 */
class OwlManagement extends OntoManagement {

    public OwlManagement() throws OntoManagementException {
        super("src/main/resources/ontologies/iso-19115.owl");
    }

    public OwlManagement(String ontologyPath) throws OntoManagementException {
        super(ontologyPath);
    }

    /**
     * Loading information from an XML file into an ontology.
     *
     * @param xmlPathfile path of the XML file
     * @return true if the uplift works, false otherwise.
     */
    @Override
    public boolean uplift(String xmlPathfile) {
        try {
            File file = new File(xmlPathfile);
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            if (document.hasChildNodes()) {
                NodeList childNodes = document.getChildNodes();
                writeNodeList(childNodes.item(0).getChildNodes(), null, null);
            } else {
                return false;
            }
        } catch (OntoManagementException | IOException | ParserConfigurationException | SAXException e) {
            Logger.getLogger(OwlManagement.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
        return true;
    }

    private void writeNodeList(NodeList nodeList, Individual indiv, OntProperty parentProperty) throws OntoManagementException {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node elemNode = nodeList.item(count);
            if (elemNode.getNodeType() == Node.ELEMENT_NODE) {
                OntResource current = this.asOntResource(getNodeName(elemNode));
                //if the current resource is null, the node is a value.
                //the node represents a property or an individual .
                if (current == null) {
                    createsValue(elemNode, parentProperty, indiv);
                } else if (current.isClass()) {//if the node represents a class.
                    createsClass(elemNode, current.asClass(), parentProperty, indiv);
                } else if (current.isProperty()) { //if the node represents a property.
                    OntProperty ontProperty = current.asProperty();
                    writeNodeList(elemNode.getChildNodes(), indiv, ontProperty);
                } else {
                    throw new OntoManagementException(current + " is unknown");
                }
            }
        }
    }

    private void createsClass(Node elemNode, OntClass nodeName, OntProperty parentProperty, Individual indiv) throws DOMException, OntoManagementException {

        Individual n = this.getIndividual(elemNode, nodeName);
        if (n == null) {
            throw new OntoManagementException(nodeName + "not found!");
        }
        if (parentProperty != null) {
            indiv.addProperty(parentProperty, n);
        }
        if (elemNode.hasChildNodes()) {
            //recursive call if the node has child nodes
            writeNodeList(elemNode.getChildNodes(), n, null);
        }
    }

    private void createsValue(Node elemNode, OntProperty parentProperty, Individual indiv) throws DOMException, OntoManagementException {
        //if the property does not exists, it is a value.
        String textContent = elemNode.getTextContent();
        if (parentProperty == null) {
            throw new OntoManagementException("Property not found: " + elemNode);
        } else {
            //A data value OR an error.
            if (parentProperty.isDatatypeProperty()) {
                indiv.addLiteral(parentProperty, textContent);
            } else {
                String nodeName = elemNode.getNodeName();
                //class not found excepted for west/east/north/south/BoundLongitude parentProperty.getLocalName().contains("BoundLongitude)"
                if (nodeName.contains("Decimal")) {
                    Individual angle = this.ont.createIndividual(OwlManagement.generateURI(), this.ont.createClass("http://lab.ponciano.info/ontology/2020/Spalod/iso-19103#Angle"));
                    angle.addLiteral(this.ont.createDatatypeProperty(NS + "decimalValue"), textContent);
                    indiv.addProperty(parentProperty, angle);
                } else {
                    throw new OntoManagementException("Class not found: " + elemNode);
                }

            }
        }
    }

    /**
     * Save the current ontology in an OWL file.
     *
     * @param path File path to save the ontology.
     * @throws IOException If the file cannot be written.
     */
    public void saveOntology(String path) throws IOException {
        this.ont.getOnt().write(new FileWriter(path));
    }

    private Individual getIndividual(Node elemNode, OntClass nodeClass) throws DOMException, OntoManagementException {
        Individual n = null;
        boolean notCreate = true;
        if (elemNode.hasAttributes()) {
            NamedNodeMap nodeMap = elemNode.getAttributes();
            int i = 0;
            while (notCreate && i < nodeMap.getLength()) {
                Node node = nodeMap.item(i);
                String attrName1 = node.getNodeName();
                String attrValue = node.getNodeValue();
                if (attrName1.toLowerCase().equals("uuid")) {
                    n = this.ont.createIndividual(generateURI(), nodeClass);
                    //(NS + attrValue, nodeClass);
                    n.addLiteral(this.ont.createDatatypeProperty(NS + "characterString"), attrValue);
                    notCreate = false;
                } else if (attrName1.equals("codeListValue") || attrName1.equals("codeListElementValue")) {
                    String name = NS + "_" + attrValue;
                    n = this.ont.getIndividual(name);//here the ontology load "dataset" which is a property instead of "_dataset" which is an individual.
                    if (n == null) {
                        name = NS + attrValue;
                        n = this.ont.getIndividual(name);//here the ontology load "dataset" which is a property instead of "_dataset" which is an individual.
                        if (n == null) {
                            throw new OntoManagementException(name + " not defined in the ontology!");
                        }
                    }
                    notCreate = false;
                }
                i++;
            }
        }
        if (notCreate) {
            n = this.ont.createIndividual(generateURI(), nodeClass);
        }
        return n;
    }

    public static String getNodeName(Node elemNode) {
        return elemNode.getNodeName().split(":")[1];
    }

    @Override
    public boolean change(String ind, String property, String value) {
        boolean change=true;
        String query="DELETE { <"+ind+"> <"+property+"> ?v .}\n";
        /*try {
            System.out.println("query1: "+query);
            this.update(query);
        } catch (OntoManagementException ex) {
            Logger.getLogger(OwlManagement.class.getName()).log(Level.SEVERE, null, ex);
            change=false;
        }*/
        query+="INSERT { <"+ind+"> <"+property+"> \""+value+ "\" .}\n";
        query+="WHERE {  <"+ind+"> <"+property+"> ?v .}";
        try {
            System.out.println("query2: "+query);
            this.update(query);
        } catch (OntoManagementException ex) {
            Logger.getLogger(OwlManagement.class.getName()).log(Level.SEVERE, null, ex);
            change=false;
        }
        return change;
    }

    @Override
    public String downlift(String metadataURI) throws OntoManagementException {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            //get metadata individual
            Individual individual = this.ont.getIndividual(metadataURI);

            Element rootElement = document.createElementNS(individual.getURI(),individual.getLocalName());
            document.appendChild(rootElement);

            recDownlift(individual, document, rootElement);
            // create the xml file
            // output DOM XML to console 
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter buff = new StringWriter();
            StreamResult console = new StreamResult(buff);
            transformer.transform(source, console);
            return buff.toString();
        } catch (ParserConfigurationException | TransformerException | DOMException ex) {
            Logger.getLogger(OwlManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private void recDownlift(Individual individual, Document document, Element rootElement) throws DOMException, OntoManagementException {
        if (individual != null) {
            OntClass ontClass = null;
            try {
                ontClass = individual.getOntClass();
            } catch (Exception e) {
            }
            if (ontClass == null) {
                throw new OntoManagementException(individual + " has no OntClass in the ontology!");
            }
            //add this individual to rootElement
            Element current = document.createElementNS(ontClass.getURI(),ontClass.getLocalName());
            rootElement.appendChild(current);
            //get properties
            StmtIterator listProperties = individual.listProperties();
            while (listProperties.hasNext()) {
                Statement next = listProperties.next();
                String nameP = next.getPredicate().getURI();
                OntProperty predicate = this.ont.getOnt().createOntProperty(nameP);
                RDFNode object = next.getObject();
                //System.out.println("Object: " + object);
                //System.out.println("Prp: " + predicate);

                if (predicate == null || !OwlManagement.containsNS(predicate.getNameSpace())) {
                    //System.out.println(nameP + " skiped");
                } else //if it is an object property
                if (predicate.isObjectProperty()) {
//                    if (predicate.getURI().contains("northBoundLatitude")) {
                        //System.out.println("elkjf");
//                    }
                    //add the property to the element and recusively go to the object property
                    Element predicatElement = document.createElementNS(predicate.getURI(),predicate.getLocalName());
                    current.appendChild(predicatElement);
                    //get object as resource
                    Individual iobj = this.ont.getIndividual(object.asResource().getURI());
                    recDownlift(iobj, document, predicatElement);
                } else // IF it is a data property
                if (predicate.isDatatypeProperty()) {
                    Element e = document.createElementNS(predicate.getURI(),predicate.getLocalName());
                    current.appendChild(e);

                    Element se = document.createElementNS("http://www.isotc211.org/2005/gco","CharacterString");
                    se.appendChild(document.createTextNode(object.toString()));
                    e.appendChild(se);
                }

            }
        }
    }

    @Override
    public void dataImport(String mduri, String ttlpath) {
        //local ontmodel creation from the path file in parameter
        OntModel om = (OntModel) RDFDataMgr.loadModel(ttlpath);
        //Creation of an individual of the MD_dataset class into the local ontmodel
        //and retrieve its URI into a variable
        String dsuri= NS +"dataset_"+ UUID.randomUUID().toString();
        Individual ds = om.createIndividual(dsuri, this.ont.createClass("http://lab.ponciano.info/ontology/2020/Spalod/iso-19115#MD_Dataset"));
        System.out.println(ds);
        //Add a link between the dataset individual and each individual of the ontmodel
        //having a geometry
        
        //insert the local ontmodel into the knowledge base
        
        //add a link into the knowledge base between the metadata uri given as 
        //parameter and the uri of the dataset individual
        
        
    }
    

}
