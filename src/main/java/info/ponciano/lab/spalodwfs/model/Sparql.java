package info.ponciano.lab.spalodwfs.model;

import info.ponciano.lab.pitools.files.PiFile;
import info.ponciano.lab.spalodwfs.controller.storage.FileDownloadController;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sparql {
    //prefixes for SPARQL query
    String prefixes = "PREFIX schema: <http://schema.org/>\n"
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
             +"PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>\n"
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n";
    private List<String> header;
    private ArrayList<String[]> data;
    private String outputFilePath;



    private void init(String triplestore, String q) {
        String queryString = prefixes + q;
        //execute the query
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec;
        if (triplestore == null || triplestore.isBlank())
            qexec = QueryExecutionFactory.create(query, KB.get().getOnt().getOnt());
        else
            qexec = QueryExecutionFactory.sparqlService(triplestore, query);

        //store results in ResultSet format
        ResultSet resultset = qexec.execSelect();

        //get columns name as results var
        this.header = resultset.getResultVars();
        this.data = new ArrayList<>();

        //for all the QuerySolution in the ResultSet file
        while (resultset.hasNext()) {
            QuerySolution solution = resultset.next();
            String[] properties = new String[header.size()];
            for (int i = 0; i < header.size(); i++) {
                String columnName = header.get(i);
                RDFNode node = solution.get(columnName);

                String nodevalue = null;
                //test if resource
                if (node == null) nodevalue = "NC";
                else if (node.isResource()) {
                    nodevalue = node.asResource().getURI();
                } else
                    //test if literal
                    if (node.isLiteral()) {
                        nodevalue = node.asLiteral().toString();
                    }
                if (nodevalue == null) {
                    System.out.println(columnName + " = " + node);
                } else {
                    //clean literal
                    if (nodevalue.contains("^^http://www.w3.org/2001/XMLSchema#double")) {
                        nodevalue = nodevalue.replace("^^http://www.w3.org/2001/XMLSchema#double", "");
                    }
                    if (nodevalue.contains("^^http://www.w3.org/2001/XMLSchema#float")) {
                        nodevalue = nodevalue.replace("^^http://www.w3.org/2001/XMLSchema#float", "");
                    }
                    //remove ","
                    nodevalue = nodevalue.replace(",", ".");
                    properties[i] = nodevalue;
                }
            }
            data.add(properties);
        }
    }


    public String getOutputFilePath() throws Exception {
        return outputFilePath;
    }


    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public ArrayList<String[]> getData() {
        return data;
    }

    public void setData(ArrayList<String[]> data) {
        this.data = data;
    }

    public void select( String triplestore, String q) throws Exception {
        init(triplestore, q);
        String p = UUID.randomUUID().toString() + ".csv";
        String[] ts = this.header.toArray(new String[this.header.size()]);
        String[][] d = this.data.toArray(new String[this.data.size()][this.data.get(0).length]);
        Sparql.writesCSV(FileDownloadController.DIR + p, d, ",", ts);
        this.outputFilePath = FileDownloadController.DOWNLOAD_DATA + p;
    }

    public static void writesCSV(final String path, final String[][] array, final String separator,
                                 final String[] header) {
        String write = "";

        for (int i = 0; i < header.length; i++) {
            write += header[i];
            if (i + 1 < header.length) {
                write += separator;
            }
        }
        write += "\n";
        for (String[] array1 : array) {
            for (int j = 0; j < array1.length; j++) {
                write += array1[j];
                if (j + 1 < array1.length) {
                    write += separator;
                }
            }
            write += "\n";
        }
        new PiFile(path).writeTextFile(write);
    }

    public void updateOntology(String triplestore,TripleData tripleData) {
        // Load your ontology file
        String inputFileName = "path/to/your/ontology/file.ttl"; // Replace with your ontology file path
        Model model = ModelFactory.createDefaultModel();
        model.read(FileManager.get().open(inputFileName), null, "TURTLE");
    
        // Create resources for Subject, Predicate, and Object
        Resource subject = model.createResource(tripleData.getSubject());
        Property predicate = model.createProperty(tripleData.getPredicate());
        Resource objectResource = model.createResource(tripleData.getObject());
    
        // Add the new triple to the model
        Statement statement = model.createStatement(subject, predicate, objectResource);
        model.add(statement);
    
        // Save the updated model back to the file
        try (FileOutputStream fos = new FileOutputStream(inputFileName)) {
            model.write(fos, "TURTLE");
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }
    
}
