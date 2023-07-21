package info.ponciano.lab.spalodwfs.controller;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.model.FormData;
import info.ponciano.lab.spalodwfs.model.JsonUtil;
import info.ponciano.lab.spalodwfs.model.QueryResult;
import info.ponciano.lab.spalodwfs.model.SparqlQuery;
import info.ponciano.lab.spalodwfs.model.TripleData;
import info.ponciano.lab.spalodwfs.model.TripleOperation;
import info.ponciano.lab.spalodwfs.model.Triplestore;
import info.ponciano.lab.spalodwfs.services.FormDataService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OntoManagementException;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OwlManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import info.ponciano.lab.spalodwfs.controller.storage.StorageProperties;
import info.ponciano.lab.spalodwfs.mvc.controllers.last.GeoJsonForm;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiSparql;
import info.ponciano.lab.pitools.files.PiFile;

import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api")
@RestController
public class ResController {
  @Value("${triplestore.sparqlEndpoint}")
  private String sparqlEndpoint;

  @Autowired
  private FormDataService formDataService;

  private final StorageService storageService;

  private static final String GRAPHDB_QUERY_ENDPOINT = ""+KB.SERVER+":7200/repositories/Spalod";
  private static final String GRAPHDB_UPDATE_ENDPOINT = ""+KB.SERVER+":7200/repositories/Spalod/statements";

  @Autowired
  public ResController(StorageService storageService) {
    this.storageService = storageService;
  }

  @PostMapping("/sign-in")
  public ResponseEntity<Void> signIn(@RequestBody FormData formData) {
    System.out.println("***********" + "/sign-in" + "***********");
    System.out.println(formData);
    formDataService.saveFormData(formData);
    return ResponseEntity.ok().build();
  }

  /**
   * Query a triplestore and return the results in a 2D array of string.
   *
   * @param sq Sparql query where the "query" and the "triplestore" have to be
   *           defined. If triplestore is empty, the USK will be used.
   *           To use the map, the 4 first variables have to be the category, the
   *           label (display in the popup),the latitude, and, the longitude
   * @return String[][] corresponding to the query results with the first row as
   *         header.
   *         Example of curl query:
   *         curl -X POST "+KB.SERVER+":8081/api/sparql-select -H
   *         'Content-type:application/json' -d '{"query":"SELECT ?category
   *         ?itemLabel ?latitude ?longitude ?item WHERE { VALUES ?category{
   *         wd:Q3914} ?item wdt:P17 wd:Q183. ?item wdt:P31 ?category . ?item
   *         p:P625 ?statement . ?statement psv:P625 ?coordinate_node .
   *         ?coordinate_node wikibase:geoLatitude ?latitude . ?coordinate_node
   *         wikibase:geoLongitude ?longitude .FILTER(?latitude <=
   *         86.42397134276521).FILTER(?latitude >=
   *         -63.39152174400882).FILTER(?longitude <=
   *         219.02343750000003).FILTER(?longitude >= -202.85156250000003)}LIMIT
   *         500", "triplestore": "https://query.wikidata.org/sparql"}'
   */

  @PostMapping("/sparql-select")
  public String sparqlQuery(@RequestBody SparqlQuery sq) {
    System.out.println("***********" + "/sparql-select" + "***********");
    System.out.println("Query:");
    System.out.println(sq);
    String query = KB.PREFIX + " " + sq.getResults();
    String triplestore = sq.getTriplestore();
    String results;
    if (triplestore == null || triplestore.isBlank())
      results = Triplestore.get().executeSelectQuery(query);
    else
      results = Triplestore.executeSelectQuery(query, triplestore);
    System.out.println("***********" + "END /sparql-select" + "***********");

    // System.out.println(results);
    return results;
  }

  /**
   * Updates the local file with the given triple operation (add, modify, or
   * remove).
   *
   * @param tripleOp A JSON object representing the triple operation.
   * @return An HTTP response indicating the success of the operation.
   * 
   *         EXAMPLE:
   *         curl -X POST -H "Content-Type: application/json" -d '{"operation":
   *         "add", "tripleData": {"subject": "http://example.com/subject1",
   *         "predicate": "http://example.com/predicate1", "object":
   *         "http://example.com/object1"}}' "+KB.SERVER+":8081/api/update
   */
  @PostMapping("/update")
  public ResponseEntity<Void> update(@RequestBody TripleOperation tripleOperation) {
    System.out.println("***********" + "/update" + "***********");
    System.out.print(tripleOperation);
    TripleData tripleData = tripleOperation.getTripleData();
    String subject = "<" + tripleData.getSubject() + ">";
    String predicate = "<" + tripleData.getPredicate() + ">";
    String object = tripleData.getObject();
    if (object.startsWith("http"))
      object = "<" + object + ">";

    // try to add until it is done
    boolean inprocess = true;
    while (inprocess) {
      try {
        update_model(tripleOperation, subject, predicate, object);
        Thread.sleep(100);
        inprocess = false;
      } catch (Exception e) {
        if (!e.getMessage().equals("Currently in an active transaction")) {
          inprocess = false;
          System.err.println(":::::::::::::::::ERROR:::::::::::::::::");
          System.err.println("DATA : " + tripleOperation);
          System.err.println(e.getMessage());
          System.err.println(":::::::::::::::::END ERROR:::::::::::::::::");
        }

      }
    }
    return ResponseEntity.ok().build();
  }

  private void update_model(TripleOperation tripleOperation, String subject, String predicate, String object) {
    if ("add".equalsIgnoreCase(tripleOperation.getOperation())) {
      // Triplestore.get().addTriple(tripleData.getSubject(),
      // tripleData.getPredicate(), tripleData.getObject());
      // Insert a triple in graphdb

      // String queryString = "SELECT ?type where { <" + tripleData.getPredicate()
      // + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type }";
      // String type = Triplestore.executeSelectQuery(queryString,
      // GRAPHDB_QUERY_ENDPOINT);

      // JSONObject jsonResult = new JSONObject(type);
      // JSONObject resultsObject = jsonResult.getJSONObject("results");
      // JSONArray bindings = resultsObject.getJSONArray("bindings");
      // String predicateType = "";
      // for (int i = 0; i < bindings.length(); i++) {
      // JSONObject binding = bindings.getJSONObject(i);
      // JSONObject predicateTypeObject = binding.getJSONObject("type");
      // // System.out.println(predicateTypeObject.getString("value"));
      // if
      // (predicateTypeObject.getString("value").equals("http://www.w3.org/2002/07/owl#DatatypeProperty"))
      // predicateType = predicateTypeObject.getString("value");
      // }
      // // bindings.length() > 1 ?
      // // bindings.getJSONObject(1).getJSONObject("type").getString("value") :
      // // bindings.getJSONObject(0).getJSONObject("type").getString("value");
      // // System.out.println(tripleData.getPredicate()+" : "+predicateType);
      // if (predicateType.equals("http://www.w3.org/2002/07/owl#DatatypeProperty")) {
      // try {
      // // TEST IF INT
      // Integer.parseInt(object);
      // object = "\"" + tripleData.getObject() + "\"^^xsd:integer";
      // } catch (NumberFormatException e1) {
      // try {
      // // TEST IF FLOAT
      // Float.parseFloat(object);
      // object = "\"" + tripleData.getObject() + "\"^^xsd:float";
      // } catch (NumberFormatException e2) {
      // // IF STRING
      // if (object.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
      // object = "\"" + tripleData.getObject() + "\"^^xsd:dateTime";
      // System.out.println("MATCH TIME ----------------------------------");
      // } else {
      // object = "\"" + tripleData.getObject() + "\"^^xsd:string";
      // }
      // }
      // }
      // } else {
      // if (object.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
      // object = "\"" + tripleData.getObject() + "\"^^xsd:dateTime";
      // System.out.println("MATCH TIME ----------------------------------");
      // } else {
      // object = "<" + tripleData.getObject() + ">";
      // }
      // }

      ParameterizedSparqlString insertCommand = new ParameterizedSparqlString();
      insertCommand.setCommandText(KB.PREFIX + " INSERT DATA { " + subject + " " + predicate + " " + object + " }");
      UpdateRequest insertRequest = UpdateFactory.create(insertCommand.toString());
      UpdateProcessor insertProcessor = UpdateExecutionFactory.createRemoteForm(insertRequest, GRAPHDB_UPDATE_ENDPOINT);
      insertProcessor.execute();

      System.out.println("-> added!");
    } else if ("remove".equalsIgnoreCase(tripleOperation.getOperation())) {

      // String queryString = "SELECT ?type where { <" + tripleData.getPredicate()
      // + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type }";
      // String type = Triplestore.executeSelectQuery(queryString,
      // GRAPHDB_QUERY_ENDPOINT);

      // JSONObject jsonResult = new JSONObject(type);
      // JSONObject resultsObject = jsonResult.getJSONObject("results");
      // JSONArray bindings = resultsObject.getJSONArray("bindings");
      // String predicateType =
      // bindings.getJSONObject(0).getJSONObject("type").getString("value");
      // System.out.println(predicateType);
      // if (predicateType == "http://www.w3.org/2002/07/owl#DatatypeProperty") {
      // try {
      // // TEST IF INT
      // int intValue = Integer.parseInt(object);
      // object = "\"" + tripleData.getObject() + "\"^^xsd:integer";
      // } catch (NumberFormatException e1) {
      // try {
      // // TEST IF FLOAT
      // float floatValue = Float.parseFloat(object);
      // object = "\"" + tripleData.getObject() + "\"^^xsd:float";
      // } catch (NumberFormatException e2) {
      // // IF STRING
      // object = "\"" + tripleData.getObject() + "\"^^xsd:string";
      // }
      // }
      // } else {
      // object = "<" + tripleData.getObject() + ">";
      // }

      ParameterizedSparqlString removeCommand = new ParameterizedSparqlString();
      removeCommand.setCommandText(KB.PREFIX + " DELETE { " + subject + " " + predicate + " " + object + " }" +
          "WHERE  { " + subject + " " + predicate + " " + object + " }");
      UpdateRequest removeRequest = UpdateFactory.create(removeCommand.toString());
      UpdateProcessor removeProcessor = UpdateExecutionFactory.createRemoteForm(removeRequest, GRAPHDB_UPDATE_ENDPOINT);
      removeProcessor.execute();

      System.out.println("-> removed!");
    } else {
      throw new IllegalArgumentException("Invalid operation: " + tripleOperation.getOperation());
    }
  }

  /**
   * Uplifts a GeoJSON file to RDF format and updates the ontology.
   *
   * @param file The GeoJSON file to be uplifted.
   * @return The path to the updated ontology file.
   * 
   *         Example: curl -X POST -F "file=@/path/to/your/geojson-file.geojson"
   *         "+KB.SERVER+":8081/api/uplift
   * @throws Exception
   * @throws PiOntologyException
   * @throws ParseException
   * @throws IOException
   * @throws FileNotFoundException
   * 
   */
  @PostMapping("/uplift")
  public String uplift(@RequestParam("file") MultipartFile file)
      throws FileNotFoundException, IOException, ParseException, PiOntologyException, Exception {
    PiSparql ont = new OwlManagement(KB.DEFAULT_ONTO).getOnt();

    System.out.println("***********" + "/uplift" + "***********");
    // store file
    storageService.store(file);

    // File reading
    String filename = file.getOriginalFilename();
    String geojsonfilepath = KB.STORAGE_DIR + "/" + filename;

    // execute the uplift
    GeoJsonRDF.upliftGeoJSON(geojsonfilepath, ont);
    // KB.get().save();
    String out;
    if (filename != null)
      out = filename.substring(0, filename.lastIndexOf(".")) + ".owl";
    else
      out = "out.owl";
    String res = new StorageProperties().getLocation() + "/" + out;
    ont.write(res);
    // Set the appropriate Content-Type header based on the file's MIME type
    Path path = new File(res).toPath();
    // Set the Content-Disposition header to prompt the user to download the file
    String result = path.getFileName().toString();
    // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + res);
    System.out.println("results: " + result);
    return "/files/" + result;
  }

  /**
   * Downlifts an RDF resource to GeoJSON format.
   *
   * @param di A JSON object containing the URI of the RDF resource to be
   *           downlifted.
   * @return The path to the generated GeoJSON file.
   * 
   *         Example: curl -X POST -H "Content-Type: application/json" -d
   *         '{"name": "http://example.com/your-uri"}'
   *         "+KB.SERVER+":8081/api/downlift
   * 
   */
  @PostMapping("/downlift")
  public String downlift(GeoJsonForm di) {
    System.out.println("***********" + "/downlift" + "***********");
    String uri = di.getName();
    try {
      // downlift
      String downlift = GeoJsonRDF.downlift(KB.get().getOnt(), uri);
      // save the file
      String out = uri.substring(uri.lastIndexOf('#') + 1, uri.length()) + ".geojson";
      String res = new StorageProperties().getLocation() + "/" + out;
      System.out.println(res);
      new PiFile(res).writeTextFile(downlift);
      return "/files/" + out;
    } catch (Exception ex) {
      return ex.getMessage();
    }
  }

  /**
   * Enriches the current ontology with another ontology if all classes and
   * properties are known.
   *
   * @param file The ontology file to enrich the current ontology with.
   * @return A JSON string with either a success message or a list of unknown
   *         classes and properties.
   * 
   *         Example: curl -X POST -F "file=@/path/to/your/ontology-file.ttl"
   *         "+KB.SERVER+":8081/api/enrich
   * 
   */
  @PostMapping("/enrich")
  public ResponseEntity<Void> enrich(@RequestParam("file") MultipartFile file) {
    System.out.println("***********" + "/enrich" + "***********");

    // store file
    storageService.store(file);
    // File reading
    String filename = file.getOriginalFilename();
    String filepath = KB.STORAGE_DIR + "/" + filename;
    OntModel newOntology = ModelFactory.createOntologyModel();
    try {
      newOntology.read(new FileInputStream(filepath), null);
      Triplestore.get().addOntology(newOntology);
      return ResponseEntity.ok().build();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/check-ontology")
  public String check(@RequestParam("file") MultipartFile file) {
    System.out.println("***********" + "/check-ontology" + "***********");

    // store file
    storageService.store(file);
    // File reading
    String filename = file.getOriginalFilename();
    String filepath = KB.STORAGE_DIR + "/" + filename;
    OntModel newOntology = ModelFactory.createOntologyModel();
    Set<String> unknownPredicates = new HashSet<>();
    try {
      newOntology.read(new FileInputStream(filepath), null);

      StmtIterator stmtIterator = newOntology.listStatements();
      while (stmtIterator.hasNext()) {

        Statement statement = stmtIterator.nextStatement();
        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();
        RDFNode object = statement.getObject();
        String p = predicate.getURI();
        // test if the predicate is knwown
        if (p.toLowerCase().contains("spalod")) {
          String query = KB.PREFIX + " SELECT ?s ?o WHERE {?s <" + p + "> ?o}";
          String result = Triplestore.executeSelectQuery(query, GRAPHDB_QUERY_ENDPOINT);
          try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);

            JsonNode bindingsNode = jsonNode.at("/results/bindings");
            boolean isEmpty = bindingsNode.isArray() && bindingsNode.isEmpty();
            String substring = p.substring(p.lastIndexOf("#") + 1, p.length());
            if (isEmpty) {
              unknownPredicates.add("spalod:" + substring);
            }

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
       
        // add the statements
        TripleOperation tripleOperation = new TripleOperation("add",
            new TripleData(subject.toString(), predicate.toString(), object.toString()));
        this.update(tripleOperation);

      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return "ERROR: " + e.getMessage();
    }
    String json = JsonUtil.setToJson(unknownPredicates);
    System.out.println("***********" + "DONE: check-ontology" + "***********");

    return json;
  }

}