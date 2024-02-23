package info.ponciano.lab.spalodwfs.model;

import java.io.File;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.*;

import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import org.apache.jena.datatypes.*;

public class Triplestore {


    private static Triplestore triplestore = null;

    public static final String directory = "dataset";
    private final Dataset dataset;


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
        Query query = QueryFactory.create(KB.PREFIX + queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
        ResultSet execSelect = qexec.execSelect();
        String results = QueryResult.convertResultSetToJavaObject(execSelect);
        dataset.end();
        return results;
    }

    public static String executeSelectQuery(String sparqlQuery, String triplestore) {
        if(!sparqlQuery.contains("PREFIX schema:"))
            sparqlQuery = KB.PREFIX + sparqlQuery;
        System.out.println(sparqlQuery);
        Query query = QueryFactory.create(sparqlQuery);
        // if (triplestore == GRAPHDB_QUERY_ENDPOINT) {
            ParameterizedSparqlString queryCommand = new ParameterizedSparqlString();
            queryCommand.setCommandText(sparqlQuery);

            QueryExecutionHTTP qe = QueryExecutionHTTP.service(triplestore, queryCommand.asQuery());
            ResultSet graphResults = qe.execSelect();
            String queryResults = QueryResult.convertResultSetToJavaObject(graphResults);
            return queryResults;
        // // } else {
                

            // QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore,
            // query);
            //   QueryExecution qexec = QueryExecution.service(triplestore).query(query).build();
            // // QueryExecution qexec = QueryExecutionFactory.sparqlService(triplestore,
            // // query);
            // if (query.isSelectType()) {
            //     ResultSet resultSet = qexec.execSelect();
            //     String results = QueryResult.convertResultSetToJavaObject(resultSet);
            //     return results;
            // } else {
            //     throw new IllegalArgumentException("Only SELECT queries are supported.");
            // }
        // }

    }

    public void executeUpdateQuery(String updateString) {
        dataset.begin(ReadWrite.WRITE);
        UpdateRequest updateRequest = UpdateFactory.create(KB.PREFIX + updateString);
        UpdateProcessor updateProcessor = UpdateExecutionFactory.create(updateRequest, dataset);
        updateProcessor.execute();
        dataset.commit();
        dataset.end();
    }

    public void addTriple(String subject, String predicate, String object) {
        String updateString = String.format("INSERT DATA { <%s> <%s> <%s> }",
                Triplestore.ensureUriWithNamespace(subject), Triplestore.ensureUriWithNamespace(predicate),
                Triplestore.ensureUriWithNamespace(object));
        executeUpdateQuery(updateString);
    }

    public void addTriple(String subject, String predicate, String object, String xsdtype) {
        String updateString = KB.PREFIX
                + String.format("INSERT DATA { <%s> <%s> \"%s\"^^xsd:%s }", Triplestore.ensureUriWithNamespace(subject),
                        Triplestore.ensureUriWithNamespace(predicate), object, xsdtype);
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
            return KB.NS + input;
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

    public static void executeUpdateQuery(String query, String graphdbUpdateEndpoint) {
        try {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(graphdbUpdateEndpoint))
                .header("Content-Type", "application/sparql-update")
                .POST(BodyPublishers.ofString(query))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        System.out.println("Response status code: " + response.statusCode());
        System.out.println("Response body: " + response.body());
        // boolean inprocess = true;
        // while (inprocess) {
        //     try {
        //         UpdateRequest insertRequest = UpdateFactory.create(query);
        //         UpdateProcessor insertProcessor = UpdateExecutionFactory.createRemoteForm(insertRequest,
        //                 graphdbUpdateEndpoint);
        //         insertProcessor.execute();
        //         Thread.sleep(100);
        //         inprocess = false;
        } catch (Exception e) {
            if (!e.getMessage().equals("Currently in an active transaction")) {
                System.err.println(":::::::::::::::::ERROR:::::::::::::::::");
                System.err.println(query);
                System.err.println("----------");
                System.err.println(e.getMessage());
                System.err.println(":::::::::::::::::END ERROR:::::::::::::::::");
            }
        }
        // }
    }
}