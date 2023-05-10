package info.ponciano.lab.spalodwfs.model;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OntologyGenerator {

    public static void main(String[] args) {
        // Create ontology model
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        // Define ontology namespace
        String ontologyNamespace = "http://spalod/";

        // Define properties
        DatatypeProperty emailProperty = ontModel.createDatatypeProperty(ontologyNamespace + "hasEmail");
        emailProperty.addDomain(ontModel.createResource(ontologyNamespace + "Form"));
        emailProperty.addRange(XSD.xstring);

        DatatypeProperty droplinkProperty = ontModel.createDatatypeProperty(ontologyNamespace + "hasDroplink");
        droplinkProperty.addDomain(ontModel.createResource(ontologyNamespace + "Form"));
        droplinkProperty.addRange(XSD.xstring);

        DatatypeProperty choiceProperty = ontModel.createDatatypeProperty(ontologyNamespace + "hasChoice");
        choiceProperty.addDomain(ontModel.createResource(ontologyNamespace + "Form"));
        choiceProperty.addRange(XSD.xstring);

        DatatypeProperty infoProperty = ontModel.createDatatypeProperty(ontologyNamespace + "hasInfo");
        infoProperty.addDomain(ontModel.createResource(ontologyNamespace + "Form"));
        infoProperty.addRange(XSD.xstring);

        DatatypeProperty newsletterAcceptanceProperty = ontModel.createDatatypeProperty(ontologyNamespace + "hasNewsletterAcceptance");
        newsletterAcceptanceProperty.addDomain(ontModel.createResource(ontologyNamespace + "Form"));
        newsletterAcceptanceProperty.addRange(XSD.xstring);

        // Save ontology to a file
        String ontologyFile = "deeplearnify.owl";
        try (OutputStream out = new FileOutputStream(ontologyFile)) {
            ontModel.write(out, "RDF/XML");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}