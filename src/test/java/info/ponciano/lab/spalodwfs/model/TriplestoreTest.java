package info.ponciano.lab.spalodwfs.model;

import org.apache.jena.ontology.*;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.function.library.print;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TriplestoreTest {

    private static final File FILE = new File("dataset_test");
    Path tempDir = FILE.toPath();
    Triplestore triplestore = new Triplestore(tempDir.toString());

    @Test
    public void testExecuteSelectQuery() {
        triplestore.executeUpdateQuery("CLEAR ALL");
        // Insert some test data into the dataset
        triplestore.addTriple("http://example.org/subject1", "http://example.org/predicate1", "object1", "string");
        triplestore.addTriple("http://example.org/subject2", "http://example.org/predicate2", "object2", "string");

        // Execute a SELECT query and check the result
        String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        String queryResult = triplestore.executeSelectQuery(queryString);
        assertEquals("{\"head\":{\"vars\":[\"s\",\"p\",\"o\"]},\"results\":{\"bindings\":[{\"s\":{\"type\":\"uri\",\"value\":\"http://example.org/subject1\"},\"p\":{\"type\":\"uri\",\"value\":\"http://example.org/predicate1\"},\"o\":{\"type\":\"literal\",\"value\":\"object1\"}},{\"s\":{\"type\":\"uri\",\"value\":\"http://example.org/subject2\"},\"p\":{\"type\":\"uri\",\"value\":\"http://example.org/predicate2\"},\"o\":{\"type\":\"literal\",\"value\":\"object2\"}}]}}".replaceAll("\\s",""), queryResult.replaceAll("\\s",""));
    }

    @Test
    public void testRemoveTriple() {
        triplestore.executeUpdateQuery("CLEAR ALL");
        // Add a triple to the dataset
        triplestore.addTriple("http://example.org/subject1", "http://example.org/predicate1", "object1");

        // Remove the triple from the dataset using the removeTriple method
        triplestore.removeTriple("http://example.org/subject1", "http://example.org/predicate1", "object1");

        // Check that the triple was removed from the dataset
        String queryString = "SELECT ?o WHERE { <http://example.org/subject1> <http://example.org/predicate1> ?o }";
        String queryResult = triplestore.executeSelectQuery(queryString);
        assertEquals("{ \"head\": {    \"vars\": [ \"o\" ]  } ,  \"results\": {    \"bindings\": [          ]  }}".replaceAll("\\s",""), queryResult.replaceAll("\\s",""));
    }

    @Test
    public void testAddOntology() {
        triplestore.executeUpdateQuery("CLEAR ALL");
        // Create an ontology model
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        // Create a namespace for the ontology
        String NS = "http://example.com/";

        // Create a resource for the class "Person"
        OntClass personClass = model.createClass(NS + "Person");

        // Create a resource for the property "hasName"
        OntProperty hasName = model.createOntProperty(NS + "hasName");

        // Add a domain and range to the property
        hasName.addDomain(personClass);
        hasName.addRange(XSD.xstring);

        // Create a resource for an individual of the "Person" class
        Individual john = personClass.createIndividual(NS + "John");

        // Set the value of the "hasName" property for the individual
        john.addProperty(hasName, "John Smith");

        // Add an ontology to the dataset using the addOntology method
        triplestore.addOntology(model);

        // Check that the ontology triples were added to the dataset
        String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        String queryResult = triplestore.executeSelectQuery(queryString);
        assertTrue(queryResult.length() > 0);
        System.out.println(queryResult);
    }
    @Test
    public void testAsk() {
        triplestore.executeUpdateQuery("CLEAR ALL");
        String existingPredicateURI = "http://example.com/existingPredicate";
        triplestore.addTriple("http://example.org/subject1", existingPredicateURI, "object1");
        String nonExistingPredicateURI = "http://example.com/nonExistingPredicate";

        // Test with an existing predicate
        String askQuery1 = String.format("ASK { ?s <%s> ?o }", existingPredicateURI);
        assertTrue(triplestore.ask(askQuery1));

        // Test with a non-existing predicate
        String askQuery2 = String.format("ASK { ?s <%s> ?o }", nonExistingPredicateURI);
        assertFalse(triplestore.ask(askQuery2));
    }

    @Test
    public void testGetUnknownPredicates() {
        Model model = ModelFactory.createDefaultModel();
        triplestore.executeUpdateQuery("CLEAR ALL");
        // Add known and unknown predicates
        Resource subject = model.createResource("http://example.com/subject");
        Property knownPredicate = model.createProperty("http://example.com/knownPredicate");
        Property unknownPredicate = model.createProperty("http://example.com/unknownPredicate");
        Resource object1 = model.createResource("http://example.com/object1");
        Resource object2 = model.createResource("http://example.com/object2");

        model.add(subject, knownPredicate, object1);
        model.add(subject, unknownPredicate, object2);
        triplestore.addTriple(subject.getURI(), knownPredicate.getURI(), object1.getURI());
        Set<String> unknownPredicates = triplestore.getUnknownPredicates(model);

        assertTrue(unknownPredicates.contains(unknownPredicate.getURI()));
        assertFalse(unknownPredicates.contains(knownPredicate.getURI()));
    }
}
