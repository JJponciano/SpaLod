package info.ponciano.lab.spalodwfs.services;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.XSD;
import org.springframework.stereotype.Service;

import info.ponciano.lab.spalodwfs.model.FormData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class FormDataService {

    public void saveFormData(FormData formData) {
       // Load ontology file (Replace with your own ontology file)
       String ontologyFile = "spalod.owl";
       InputStream in = FileManager.get().open(ontologyFile);
       OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
       ontModel.read(in, null);

       // Create an individual based on a class in your ontology
       // Replace the class URI with the appropriate one from your ontology
       OntClass formClass = ontModel.getOntClass("http://spalod/Form");
       String formIndividualUri = "http://spalod/Request_" + System.currentTimeMillis();
       Individual formIndividual = ontModel.createIndividual(formIndividualUri, formClass);

       // Add properties to the individual
       // Replace the property URIs with the appropriate ones from your ontology

       Property emailProperty = ontModel.getProperty("http://spalod/hasEmail");
       formIndividual.addProperty(emailProperty, formData.getEmail());

       Property droplinkProperty = ontModel.getProperty("http://spalod/hasDroplink");
       formIndividual.addProperty(droplinkProperty, formData.getDroplink());

       Property choiceProperty = ontModel.getProperty("http://spalod/hasChoice");
       formIndividual.addProperty(choiceProperty, formData.getChoice());

       Property infoProperty = ontModel.getProperty("http://spalod/hasInfo");
       formIndividual.addProperty(infoProperty, formData.getInfo());

       Property newsletterAcceptanceProperty = ontModel.getProperty("http://spalod/hasNewsletterAcceptance");
       formIndividual.addProperty(newsletterAcceptanceProperty, String.valueOf(formData.isNewsletterAcceptance()));

       // Save the updated ontology to a file
       try (OutputStream out = new FileOutputStream(ontologyFile)) {
           ontModel.write(out, "RDF/XML");
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
}

