package info.ponciano.lab.spalodwfs.controller;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.model.FormData;
import info.ponciano.lab.spalodwfs.model.JsonUtil;
import info.ponciano.lab.spalodwfs.model.SparqlQuery;
import info.ponciano.lab.spalodwfs.model.TripleData;
import info.ponciano.lab.spalodwfs.model.TripleOperation;
import info.ponciano.lab.spalodwfs.model.Triplestore;
import info.ponciano.lab.spalodwfs.services.FormDataService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import info.ponciano.lab.spalodwfs.controller.storage.StorageProperties;
import info.ponciano.lab.spalodwfs.mvc.controllers.last.GeoJsonForm;
import info.ponciano.lab.pitools.files.PiFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api")
@RestController
public class ResController {
  @Value("${triplestore.sparqlEndpoint}")
  private String sparqlEndpoint;

  @Autowired
  private FormDataService formDataService;

  private final StorageService storageService;

  private static final String GRAPHDB_QUERY_ENDPOINT = "http://localhost:7200/repositories/graphDB_spalod";
  private static final String GRAPHDB_UPDATE_ENDPOINT = "http://localhost:7200/repositories/graphDB_spalod/statements";

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
   *         curl -X POST http://localhost:8081/api/sparql-select -H
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
    String query = sq.getResults();
    String triplestore = sq.getTriplestore();
    String results;
    if (triplestore == null || triplestore.isBlank())
      results = Triplestore.get().executeSelectQuery(query);
    else
      results = Triplestore.executeSelectQuery(query, triplestore);
      System.out.println(results);
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
   *         "http://example.com/object1"}}' http://localhost:8081/api/update
   */
  @PostMapping("/update")
  public ResponseEntity<Void> update(@RequestBody TripleOperation tripleOperation) {
    System.out.println("***********" + "/update" + "***********");
    System.out.print(tripleOperation);
    TripleData tripleData = tripleOperation.getTripleData();
    if ("add".equalsIgnoreCase(tripleOperation.getOperation())) {
      Triplestore.get().addTriple(tripleData.getSubject(), tripleData.getPredicate(), tripleData.getObject());
      
      // Insert a triple in graphdb
      String subject = "<" + tripleData.getSubject() + ">";
      String predicate = "<" + tripleData.getPredicate() + ">";
      String object = "<" + tripleData.getObject() + ">";
      System.out.println("--------------------------");
      System.out.println(subject+" "+predicate+" "+ object);
      ParameterizedSparqlString insertCommand = new ParameterizedSparqlString();
      insertCommand.setCommandText("INSERT DATA { "+subject+" "+predicate+" "+object+" }");
      UpdateRequest insertRequest = UpdateFactory.create(insertCommand.toString());
      UpdateProcessor insertProcessor = UpdateExecutionFactory.createRemoteForm(insertRequest, GRAPHDB_UPDATE_ENDPOINT);
      insertProcessor.execute();

      System.out.println("-> added!");
    } else if ("remove".equalsIgnoreCase(tripleOperation.getOperation())) {
      Triplestore.get().removeTriple(tripleData.getSubject(), tripleData.getPredicate(), tripleData.getObject());

      // Remove a triple in graphdb
      String subject = "<" + tripleData.getSubject() + ">";
      String predicate = "<" + tripleData.getPredicate() + ">";
      String object = "<" + tripleData.getObject() + ">";
      System.out.println("--------------------------");
      System.out.println(subject+" "+predicate+" "+ object);
      ParameterizedSparqlString removeCommand = new ParameterizedSparqlString();
      removeCommand.setCommandText("DELETE { "+subject+" "+predicate+" "+object+" }"+
      "WHERE  { "+subject+" "+predicate+" "+object+" }");
      UpdateRequest removeRequest = UpdateFactory.create(removeCommand.toString());
      UpdateProcessor removeProcessor = UpdateExecutionFactory.createRemoteForm(removeRequest, GRAPHDB_UPDATE_ENDPOINT);
      removeProcessor.execute();

      System.out.println("-> removed!");
    } else {
      throw new IllegalArgumentException("Invalid operation: " + tripleOperation.getOperation());
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Uplifts a GeoJSON file to RDF format and updates the ontology.
   *
   * @param file The GeoJSON file to be uplifted.
   * @return The path to the updated ontology file.
   * 
   *         Example: curl -X POST -F "file=@/path/to/your/geojson-file.geojson"
   *         http://localhost:8081/api/uplift
   * 
   */
  @PostMapping("/uplift")
  public String uplift(@RequestParam("file") MultipartFile file) {
    System.out.println("***********" + "/uplift" + "***********");
    try {
      // store file
      storageService.store(file);

      // File reading
      String filename = file.getOriginalFilename();
      String geojsonfilepath = KB.STORAGE_DIR + "/" + filename;

      // execute the uplift
      GeoJsonRDF.upliftGeoJSON(geojsonfilepath, KB.get().getOnt());
      KB.get().save();
      String out;
      if (filename != null)
        out = filename.substring(0, filename.lastIndexOf(".")) + ".owl";
      else
        out = "out.owl";
      String res = new StorageProperties().getLocation() + "/" + out;
      System.out.println(res);
      new PiFile(KB.OUT_ONTO).copy(res);
      return out;
    } catch (Exception ex) {
      return ex.getMessage();
    }
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
   *         http://localhost:8081/api/downlift
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
   *         http://localhost:8081/api/enrich
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
    Set<String> unknownPredicates = null;
    try {
      newOntology.read(new FileInputStream(filepath), null);
      unknownPredicates = Triplestore.get().getUnknownPredicates(newOntology);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String json = JsonUtil.setToJson(unknownPredicates);
    return json;
  }

}