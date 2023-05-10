package info.ponciano.lab.spalodwfs.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

public class KBmanagerLocal {
    Dataset dataset;
    String inputFileName;

    public KBmanagerLocal() {
        this.inputFileName = KB.get().getOutputPath();
        this.dataset = DatasetFactory.create();
    }

    public KBmanagerLocal(String absolutePath) {
        this.inputFileName = absolutePath;
        this.dataset = DatasetFactory.create();
    }

    public KBmanagerLocal(String absolutePath, Dataset dataset2) {
        this.inputFileName = absolutePath;
        this.dataset = dataset2;
    }

    public void updateLocalFile(TripleOperation tripleOperation) {
        this.dataset.begin(ReadWrite.WRITE);
        this.dataset.setDefaultModel(KB.get().getOnt().getOnt());
        //getDefaultModel().read(FileManager.get().open(inputFileName), null, "TURTLE");
        TripleData tripleData = tripleOperation.getTripleData();
        String updateQuery;
        if ("add".equalsIgnoreCase(tripleOperation.getOperation())) {
            updateQuery = String.format("INSERT DATA { <%s> <%s> <%s> }",
                    tripleData.getSubject(), tripleData.getPredicate(), tripleData.getObject());
        } else if ("remove".equalsIgnoreCase(tripleOperation.getOperation())) {
            updateQuery = String.format("DELETE DATA { <%s> <%s> <%s> }",
                    tripleData.getSubject(), tripleData.getPredicate(), tripleData.getObject());
        } else {
            throw new IllegalArgumentException("Invalid operation: " + tripleOperation.getOperation());
        }

        UpdateRequest updateRequest = UpdateFactory.create(updateQuery);
        UpdateProcessor updateProcessor = UpdateExecutionFactory.create(updateRequest, dataset);
        updateProcessor.execute();

        dataset.commit();
        dataset.end();
        try{
            KB.get().save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String executeSparqlQuery(String sparqlQuery, String triplestore) {
        String queryString = sparqlQuery;
        Query query = QueryFactory.create(queryString);
        Model model = dataset.getDefaultModel();

        QueryExecution qexec;
        if (triplestore == null || triplestore.isBlank())
            qexec = QueryExecutionFactory.create(query, model);
        else
            //qexec = QueryExecutionFactory.sparqlService(triplestore, query);
            qexec = QueryExecution.service(triplestore).query(query).build();
        if (query.isSelectType()) {
            ResultSet resultSet = qexec.execSelect();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, resultSet);
            return outputStream.toString();
        } else {
            throw new IllegalArgumentException("Only SELECT queries are supported.");
        }

    }
    public String enrichOntology(String filepath) throws IOException {
        Model localOntology = dataset.getDefaultModel();
        OntModel newOntology = ModelFactory.createOntologyModel();
        newOntology.read(new FileInputStream(filepath), null, "TURTLE");
    
        Set<Resource> unknownClasses = new HashSet<>();
        Set<Resource> unknownProperties = new HashSet<>();
    
        for (OntClass ontClass : newOntology.listClasses().toList()) {
            if (!ontClass.isURIResource()) continue;
            if (!localOntology.containsResource(ontClass)&&!ontClass.getNameSpace().contains("www.w3.org")) {
                unknownClasses.add(ontClass);
            }
        }
    
        for (OntProperty ontProperty : newOntology.listAllOntProperties().toList()) {
            if (!ontProperty.isURIResource()) continue;
            if (!localOntology.containsResource(ontProperty)&&!ontProperty.getNameSpace().contains("www.w3.org")) {
                unknownProperties.add(ontProperty);
            }
        }
    
        if (unknownClasses.isEmpty() && unknownProperties.isEmpty()) {
           localOntology.add(newOntology);
            return "{\"status\": \"success\", \"message\": \"Ontology enriched successfully.\"}";
        } else {
            JsonObject responseJson = new JsonObject();
            JsonArray unknownClassesArray = new JsonArray();
            JsonArray unknownPropertiesArray = new JsonArray();
    
            unknownClasses.forEach(unknownClass -> unknownClassesArray.add(unknownClass.getURI()));
            unknownProperties.forEach(unknownProperty -> unknownPropertiesArray.add(unknownProperty.getURI()));
    
            responseJson.put("unknownClasses", unknownClassesArray);
            responseJson.put("unknownProperties", unknownPropertiesArray);
    
            return responseJson.toString();
        }
    }

}
