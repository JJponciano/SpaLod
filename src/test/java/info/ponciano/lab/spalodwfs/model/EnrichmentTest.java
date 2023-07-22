package info.ponciano.lab.spalodwfs.model;

import org.apache.jena.ontology.*;
import org.junit.jupiter.api.*;
import java.io.FileNotFoundException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EnrichmentTest {
    private Enrichment enrichment;

        @BeforeEach
    void setUp() {
        // Set the path of your ontology file.
        String ontologyFilePath = "src/test/resources/test.owl";
        try {
            enrichment = new Enrichment(ontologyFilePath);
        } catch (FileNotFoundException e) {
            fail("Ontology file not found.", e);
        }
    }

    @Test
    void testEnrich() {
            String operations = enrichment.getTriples();
            assertFalse(operations.isEmpty(), "Operations  should not be empty after run");
            System.out.println(enrichment.getUnknownPredicates());
        
    }
}
