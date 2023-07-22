package info.ponciano.lab.spalodwfs.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

public class Enrichment {

    private OntModel ont;
    private Set<String> unknownPredicates = new HashSet<>();
    private List<TripleOperation> operations = new ArrayList<>();
    private String triples;

    public String getTriples() {
        return triples;
    }

    public List<TripleOperation> getOperations() {
        return operations;
    }

    public Set<String> getUnknownPredicates() {
        return unknownPredicates;
    }

    public Enrichment(String filepath) throws FileNotFoundException {
        this.ont = ModelFactory.createOntologyModel();
        this.ont.read(new FileInputStream(filepath), null);
        // OntClass dataset = ont.createClass(GeoJsonRDF.DCAT_DATASET);
        // String name = KB.NS + UUID.randomUUID().toString();
        // Individual mapThem = dataset.createIndividual(name);

        setOperations();
        
    }

    private void setOperations() {

        // Get all known predicates before loop starts
        Set<String> knownPredicates = new HashSet<>();
        String query = KB.PREFIX + "SELECT DISTINCT ?p WHERE { ?s ?p ?o }";
        String result = Triplestore.executeSelectQuery(query, KB.GRAPHDB_QUERY_ENDPOINT);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(result);

            JsonNode bindingsNode = jsonNode.at("/results/bindings");
            if (bindingsNode.isArray()) {
                for (JsonNode bindingNode : bindingsNode) {
                    String predicate = bindingNode.get("p").get("value").asText();
                    knownPredicates.add(predicate);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        StmtIterator stmtIterator = this.ont.listStatements();
        while (stmtIterator.hasNext()) {

            Statement statement = stmtIterator.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();

            RDFNode object = statement.getObject();
            String p = predicate.getURI();
            // test if the predicate is knwown
            // Test if the predicate is known
            if (!knownPredicates.contains(p)&&p.contains("spalod")) {
                String str = "#";
                if (!p.contains(str))
                    str = "/";
                String substring = p.substring(p.lastIndexOf(str) + 1, p.length());
                unknownPredicates.add("spalod:" + substring);
            }

            // addStatement(subject, predicate, object);

        }
        // Then serialize the model to a string in N-Triples format
        StringWriter writer = new StringWriter();
        this.ont.write(writer, "N-TRIPLES");
        this.triples = writer.toString();
    }

    private void addStatement(Resource subject, Property predicate, RDFNode object) {
        // add the statements
        String literal = object.toString();
        OntProperty ontProperty = this.ont.getOntProperty(predicate.getURI());
        if (ontProperty != null && ontProperty.isDatatypeProperty()) {
            if (object.isLiteral()) {
                RDFDatatype dataType = object.asLiteral().getDatatype();
                literal = this.sparqlValue(object);
            } else {
                System.out.println("The datatype of " + predicate + " is unknown");
            }
        }

        TripleOperation tripleOperation = new TripleOperation("add",
                new TripleData(subject.toString(), predicate.toString(), literal));
        this.operations.add(tripleOperation);
    }

    public String computeTriples() {
        Model model = ModelFactory.createDefaultModel();

        for (TripleOperation operation : operations) {
            if (operation.getOperation().equals("add")) {
                TripleData tripleData = operation.getTripleData();
                Resource subject = model.createResource(tripleData.getSubject());
                Property predicate = model.createProperty(tripleData.getPredicate());
                RDFNode object;
                if (tripleData.getObject().contains("http"))
                    object = model.createResource(tripleData.getObject());
                else
                    object = model.createLiteral(tripleData.getObject());
                model.add(subject, predicate, object);
            }
        }

        // Then serialize the model to a string in N-Triples format
        StringWriter writer = new StringWriter();
        model.write(writer, "N-TRIPLES");
        String triples = writer.toString();
        return triples;
    }

    public static void main(String[] args) {

        String query;
        try {
            query = "INSERT DATA { " + new Enrichment("PATH/ont.owl").getTriples() + " }";
            Triplestore.executeUpdateQuery(query, KB.GRAPHDB_UPDATE_ENDPOINT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String sparqlValue(RDFNode node) {
        if (node.isLiteral()) {
            Literal lit = node.asLiteral();
            if (lit.getDatatypeURI() != null) {
                // Typed literal
                String lex = lit.getLexicalForm();
                String datatype = lit.getDatatypeURI();
                return "\"" + lex + "\"^^<" + datatype + ">";
            } else if (lit.getLanguage() != null && !lit.getLanguage().equals("")) {
                // Language-tagged string
                String lex = lit.getLexicalForm();
                String lang = lit.getLanguage();
                return "\"" + lex + "\"@" + lang;
            } else {
                // Simple literal
                return "\"" + lit.getLexicalForm() + "\"";
            }
        } else if (node.isURIResource()) {
            return "<" + node.asResource().getURI() + ">";
        } else if (node.isAnon()) {
            return "_:" + node.asNode().getBlankNodeId().toString();
        } else {
            throw new IllegalArgumentException("Unknown type of RDFNode: " + node);
        }
    }

}
