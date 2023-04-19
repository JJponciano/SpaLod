package info.ponciano.lab.spalodwfs.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.*;
import java.net.URI;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.*;
import org.apache.jena.datatypes.*;
import org.apache.jena.ontology.*;

public class Triplestore {
    private static Triplestore triplestore = null;

    public static final String directory = "dataset";
    private final Dataset dataset;
    public static final String URI = "http://lab.ponciano.info/ont/spalod";
    public static final String NS = URI + "#";
    public static final String PREFIX = "PREFIX schema: <http://schema.org/>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX hist: <http://wikiba.se/history/ontology#>\n"
            + "PREFIX wd: <http://www.wikidata.org/entity/>\n"
            + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n"
            + "PREFIX wikibase: <http://wikiba.se/ontology#>\n"
            + "PREFIX dct: <http://purl.org/dc/terms/>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX bd: <http://www.bigdata.com/rdf#>\n"
            + "PREFIX wds: <http://www.wikidata.org/entity/statement/>\n"
            + "PREFIX wdv: <http://www.wikidata.org/value/>\n"
            + "PREFIX p: <http://www.wikidata.org/prop/>\n"
            + "PREFIX ps: <http://www.wikidata.org/prop/statement/>\n"
            + "PREFIX psv: <http://www.wikidata.org/prop/statement/value/>\n"
            + "PREFIX pq: <http://www.wikidata.org/prop/qualifier/>\n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n"
            + "PREFIX dp: <http://dbpedia.org/resource/>\n"
            + "PREFIX dpp: <http://dbpedia.org/property/>\n"
            + "PREFIX spalod: <" + NS + ">\n"
            + "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>\n"
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n";

    public static Triplestore get() {
        if (triplestore == null) {
            triplestore = new Triplestore();
        }
        return triplestore;
    }

    public Triplestore() {
        new File(Triplestore.directory).mkdir();
        this.dataset = TDB2Factory.connectDataset(Triplestore.directory);
    }

    public static String getClassName(String datatypeURI) {
        if (datatypeURI.toLowerCase().contains("string")) {
            return String.class.getName();
        } else if (datatypeURI.toLowerCase().contains("float")) {
            return Float.class.getName();
        } else if (datatypeURI.toLowerCase().contains("double")) {
            return Double.class.getName();
        } else if (datatypeURI.toLowerCase().contains("int") || datatypeURI.toLowerCase().contains("integer")) {
            return Integer.class.getName();
        } else if (datatypeURI.toLowerCase().contains("bool") || datatypeURI.toLowerCase().contains("boolean")) {
            return Boolean.class.getName();
        } else if (datatypeURI.toLowerCase().contains("date")) {
            return Date.class.getName();
        } else {
            RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
            if (datatype != null) {
                int lastIndex = datatypeURI.lastIndexOf(".");
                int lastIndex2 = datatypeURI.lastIndexOf("#");
                lastIndex = Math.max(lastIndex, lastIndex2);
                if (lastIndex > -1) {
                    String substring = datatypeURI.substring(lastIndex + 1);
                    return substring;
                }
            }
            System.out.println(datatypeURI + " unknownd");
            return null;

        }
    }

    public static String escapeString(String string) {
        String escapedString = string.replaceAll("\\\\", "\\\\\\\\"); // Replace backslashes with double backslashes
        escapedString = escapedString.replaceAll("\"", "\\\\\""); // Replace double quotes with backslash-double quotes
        escapedString = escapedString.replaceAll("\n", "\\\\n"); // Replace newlines with backslash-n
        escapedString = escapedString.replaceAll("\r", "\\\\r"); // Replace carriage returns with backslash-r
        escapedString = escapedString.replaceAll("\t", "\\\\t"); // Replace tabs with backslash-t
        return escapedString;
    }

    public Triplestore(String directory) {
        this.dataset = TDB2Factory.connectDataset(directory);
    }

    public String executeSelectQuery(String queryString) {
        dataset.begin(ReadWrite.READ);
        Query query = QueryFactory.create(PREFIX + queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
        ResultSet execSelect = qexec.execSelect();
        String results = QueryResult.convertResultSetToJavaObject(execSelect);
        dataset.end();
        return results;
    }

    public static String executeSelectQuery(String sparqlQuery, String triplestore) {

        String queryString = PREFIX + sparqlQuery;
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecution.service(triplestore).query(query).build();
        // QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore, query);
        if (query.isSelectType()) {
            ResultSet resultSet = qexec.execSelect();
            String results = QueryResult.convertResultSetToJavaObject(resultSet);
            return results;
        } else {
            throw new IllegalArgumentException("Only SELECT queries are supported.");
        }
    }

    public void executeUpdateQuery(String updateString) {
        dataset.begin(ReadWrite.WRITE);
        UpdateRequest updateRequest = UpdateFactory.create(PREFIX + updateString);
        UpdateProcessor updateProcessor = UpdateExecutionFactory.create(updateRequest, dataset);
        updateProcessor.execute();
        dataset.commit();
        dataset.end();
    }

    public void addTriple(String subject, String predicate, String object) {
        String updateString = String.format("INSERT DATA { <%s> <%s> <%s> }",  Triplestore.ensureUriWithNamespace(subject),  Triplestore.ensureUriWithNamespace(predicate),  Triplestore.ensureUriWithNamespace(object));
        executeUpdateQuery(updateString);
    }

    public void addTriple(String subject, String predicate, String object, String xsdtype) {
        String updateString = PREFIX
                + String.format("INSERT DATA { <%s> <%s> \"%s\"^^xsd:%s }", Triplestore.ensureUriWithNamespace(subject),  Triplestore.ensureUriWithNamespace(predicate), object, xsdtype);
        executeUpdateQuery(updateString);
    }

    public void removeTriple(String subject, String predicate, String object) {
        String updateString = String.format("DELETE DATA { <%s> <%s> <%s> }", subject, predicate, object);
        executeUpdateQuery(updateString);
    }

    public boolean ask(String sparqlAskQuery) {
        dataset.begin(ReadWrite.READ);
        QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(sparqlAskQuery), dataset);
        boolean result = qexec.execAsk();
        qexec.close();
        dataset.end();
        return result;
    }

    public static String ensureUriWithNamespace(String input) {
        try {
            new URI(input);
            return input;
        } catch (Exception e) {
            return NS + input;
        }
    }
    public Set<String> getUnknownPredicates(Model model) {
        StmtIterator stmtIterator = model.listStatements();
        Set<String> unknownPredicates = new HashSet<>();

        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            String predicateURI = statement.getPredicate().getURI();
            String sparqlAskQuery = String.format("ASK { ?s <%s> ?o }", predicateURI);

            if (!ask(sparqlAskQuery)) {
                unknownPredicates.add(predicateURI);
            }
        }
        return unknownPredicates;
    }

    public void addOntology(Model model) {
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.nextStatement();
            Resource subject = statement.getSubject();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();
            if (object.isLiteral()) {
                String datatypeURI = getClassName(object.asLiteral().getDatatypeURI()).toLowerCase();
                int lastIndex = datatypeURI.lastIndexOf(".");
                if (lastIndex > -1) {
                    datatypeURI = datatypeURI.substring(lastIndex + 1);
                }
                String lexicalForm = escapeString(object.asLiteral().getLexicalForm());
                this.addTriple(subject.getURI(), predicate.getURI(), lexicalForm, datatypeURI);
            } else
                this.addTriple(subject.getURI(), predicate.getURI(), object.asResource().getURI());
        }
    }

    public static void main(String[] args) {
        Triplestore triplestore = new Triplestore(directory);
        // Perform update operations on the dataset
        triplestore.addTriple("http://example.com/subject1", "http://example.com/predicate1",
                "http://example.com/object1");
        triplestore.addTriple("http://example.com/subject2", "http://example.com/predicate2",
                "http://example.com/object2");
                String results = triplestore.executeSelectQuery("SELECT ?s ?p ?o WHERE{ ?s ?p ?o}");
        System.out.println(results);
    }
}