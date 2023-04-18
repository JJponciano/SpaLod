package info.ponciano.lab.spalodwfs.model;

import org.junit.jupiter.api.Test;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.update.UpdateAction;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
public class KBmanagerTest {

    private KBmanagerLocal tripleStoreHandler;
    private Dataset dataset;

    @TempDir
    Path tempDir;

    private File createTempOntologyFile() throws IOException {
        File tempFile = tempDir.resolve("temp_ontology.ttl").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            Model model = ModelFactory.createDefaultModel();
            model.setNsPrefix("ex", "http://example.com/");
            model.write(fos, "TURTLE");
        }
        return tempFile;
    }

    @BeforeEach
    void setUp() throws IOException {
        File tempFile = createTempOntologyFile();
        dataset = DatasetFactory.create();
        dataset.getDefaultModel().read(tempFile.getAbsolutePath(), "TURTLE");
        tripleStoreHandler = new KBmanagerLocal(tempFile.getAbsolutePath(), dataset);
    }

    @Test
    void updateLocalFile_addTriple() {
        TripleOperation tripleOperation = new TripleOperation("add", new TripleData(
                "http://example.com/subject1",
                "http://example.com/predicate1",
                "http://example.com/object1"
        ));

        tripleStoreHandler.updateLocalFile(tripleOperation);

        Model model = dataset.getDefaultModel();

        Resource subject = model.createResource("http://example.com/subject1");
        Property predicate = model.createProperty("http://example.com/predicate1");
        RDFNode object = model.createResource("http://example.com/object1");

        assertTrue(model.contains(subject, predicate, object));
    }
    @Test
    void updateLocalFile_removeTriple() {
        // Add the triple
        TripleOperation addTripleOperation = new TripleOperation("add", new TripleData(
                "http://example.com/subject1",
                "http://example.com/predicate1",
                "http://example.com/object1"
        ));

        tripleStoreHandler.updateLocalFile(addTripleOperation);
        Model model = dataset.getDefaultModel();
         Resource subject = model.createResource("http://example.com/subject1");
        Property predicate = model.createProperty("http://example.com/predicate1");
        RDFNode object = model.createResource("http://example.com/object1");

        assertTrue(model.contains(subject, predicate, object));

        // Remove the triple
        TripleOperation removeTripleOperation = new TripleOperation("remove", new TripleData(
                "http://example.com/subject1",
                "http://example.com/predicate1",
                "http://example.com/object1"
        ));

        tripleStoreHandler.updateLocalFile(removeTripleOperation);

        model = dataset.getDefaultModel();

         subject = model.createResource("http://example.com/subject1");
         predicate = model.createProperty("http://example.com/predicate1");
         object = model.createResource("http://example.com/object1");

        assertFalse(model.contains(subject, predicate, object));
    }
    @Test
    void executeSparqlQuery_selectAllTriples() {
        // Add sample data to the dataset
        String turtleData = "@prefix ex: <http://example.com/> .\n"
                + "ex:subject1 ex:predicate1 ex:object1 .\n"
                + "ex:subject2 ex:predicate2 ex:object2 .";
        dataset.getDefaultModel().read(new ByteArrayInputStream(turtleData.getBytes()), null, "TURTLE");

        // Execute SPARQL query to select all triples
        String sparqlQuery = "PREFIX ex: <http://example.com/>\n"
                + "SELECT ?s ?p ?o\n"
                + "WHERE {\n"
                + "  ?s ?p ?o .\n"
                + "}";
        String jsonResponse = this.tripleStoreHandler.executeSparqlQuery(sparqlQuery,"");

        // Check if the response contains both triples
        assertTrue(jsonResponse.contains("subject1"));
        assertTrue(jsonResponse.contains("predicate1"));
        assertTrue(jsonResponse.contains("object1"));
        assertTrue(jsonResponse.contains("subject2"));
        assertTrue(jsonResponse.contains("predicate2"));
        assertTrue(jsonResponse.contains("object2"));
    }

    @Test
    void enrichOntology_allKnownEntities(@TempDir Path tempDir) throws IOException {
        // Add sample data to the local ontology
        String localOntologyData = "@prefix ex: <http://example.com/> .\n"
                + "ex:subject1 ex:predicate1 ex:object1 .\n"
                + "ex:subject2 ex:predicate2 ex:object2 .";
        dataset.getDefaultModel().read(new ByteArrayInputStream(localOntologyData.getBytes()), null, "TURTLE");

        // Create a temporary file with the new ontology
        String newOntologyData = "@prefix ex: <http://example.com/> .\n"
                + "ex:subject3 ex:predicate1 ex:object3 .\n"
                + "ex:subject4 ex:predicate2 ex:object4 .";
        File newOntologyFile = tempDir.resolve("new_ontology.ttl").toFile();
        Files.write(newOntologyFile.toPath(), newOntologyData.getBytes());

        // Call enrichOntology method
        String jsonResponse = this.tripleStoreHandler.enrichOntology(newOntologyFile.getAbsolutePath());
        System.out.println(jsonResponse);
        // Check if the response indicates success
        assertTrue(jsonResponse.contains("\"status\": \"success\""));
        assertTrue(jsonResponse.contains("\"message\": \"Ontology enriched successfully.\""));

        // Check if the local ontology now contains the new data
        Model model = dataset.getDefaultModel();
        StringWriter serializedOntology = new StringWriter();
        model.write(serializedOntology, "TURTLE");
        String localOntologyString = serializedOntology.toString();
        assertTrue(localOntologyString.contains("subject3"));
        assertTrue(localOntologyString.contains("subject4"));
    }

}