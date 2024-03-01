package info.ponciano.lab.spalodwfs.controller;

import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import info.ponciano.lab.spalodwfs.controller.storage.StorageService;
import info.ponciano.lab.spalodwfs.model.Enrichment;
import info.ponciano.lab.spalodwfs.model.FormData;
import info.ponciano.lab.spalodwfs.model.JsonUtil;
import info.ponciano.lab.spalodwfs.model.SparqlQuery;
import info.ponciano.lab.spalodwfs.model.TripleData;
import info.ponciano.lab.spalodwfs.model.TripleOperation;
import info.ponciano.lab.spalodwfs.model.Triplestore;
import info.ponciano.lab.spalodwfs.services.FormDataService;
import info.ponciano.lab.spalodwfs.mvc.models.geojson.GeoJsonRDF;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;
import info.ponciano.lab.spalodwfs.mvc.models.semantic.OwlManagement;

import java.io.File;
import java.io.FileNotFoundException;
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
  @Value("${spring.application.VITE_APP_GRAPH_DB}")
  private String graphDbUrl;

  @Value("${spring.application.VITE_APP_API_BASE_URL}")
  private String apiBaseUrl;

  @Value("${spring.application.VITE_APP_FRONT_BASE_URL}")
  private String frontBaseUrl;

  @Value("${spring.application.GRAPHDB_QUERY_ENDPOINT}")
  public   String GRAPHDB_QUERY_ENDPOINT;
   @Value("${spring.application.GRAPHDB_UPDATE_ENDPOINT}")
  public   String GRAPHDB_UPDATE_ENDPOINT;

  @Autowired
  private FormDataService formDataService;

  private final StorageService storageService;

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
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println(GRAPHDB_QUERY_ENDPOINT);
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    System.out.println("Query:");
    System.out.println(sq);
    String query = KB.PREFIX + " " + sq.getResults();
    String results;
    results = Triplestore.executeSelectQuery(query,  GRAPHDB_QUERY_ENDPOINT);
    System.out.println("***********" + "END /sparql-select" + "***********");

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
    String subject = KB.sparqlValue(tripleData.getSubject());
    String predicate = KB.sparqlValue(tripleData.getPredicate());
    String object = KB.sparqlValue(tripleData.getObject());

    

    String operation = tripleOperation.getOperation();
    String query;

    if ("add".equals(operation)) {
      query = KB.PREFIX + " INSERT DATA { " + subject + " " + predicate + " " + object + " }";
    } else if ("delete".equals(operation)) {
      query = KB.PREFIX + "DELETE DATA { " + subject + " " + predicate + " " + object + " }";
    } else {
      throw new IllegalArgumentException("Unknown operation: " + operation);
    }

    Triplestore.executeUpdateQuery(query,  GRAPHDB_UPDATE_ENDPOINT);

    return ResponseEntity.ok().build();
  }

  private void update_model(TripleOperation tripleOperation, String subject, String predicate, String object) {
    if ("add".equalsIgnoreCase(tripleOperation.getOperation())) {
  

      ParameterizedSparqlString insertCommand = new ParameterizedSparqlString();
      insertCommand.setCommandText(KB.PREFIX + " INSERT DATA { " + subject + " " + predicate + " " + object + " }");
      UpdateRequest insertRequest = UpdateFactory.create(insertCommand.toString());
      UpdateProcessor insertProcessor = UpdateExecutionFactory.createRemoteForm(insertRequest,
           GRAPHDB_UPDATE_ENDPOINT);
      insertProcessor.execute();

      System.out.println("-> added!");
    } else if ("remove".equalsIgnoreCase(tripleOperation.getOperation())) {


      ParameterizedSparqlString removeCommand = new ParameterizedSparqlString();
      removeCommand.setCommandText(KB.PREFIX + " DELETE { " + subject + " " + predicate + " " + object + " }" +
          "WHERE  { " + subject + " " + predicate + " " + object + " }");
      UpdateRequest removeRequest = UpdateFactory.create(removeCommand.toString());
      UpdateProcessor removeProcessor = UpdateExecutionFactory.createRemoteForm(removeRequest,
           GRAPHDB_UPDATE_ENDPOINT);
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

  @PostMapping("/check-ontology")
  public String check(@RequestParam("file") MultipartFile file) {
    System.out.println("***********" + "/check-ontology" + "***********");

    // store file
    storageService.store(file);
    // File reading
    String filename = file.getOriginalFilename();
    String filepath = KB.STORAGE_DIR + "/" + filename;
    try {
      Enrichment enrichment = new Enrichment(filepath);
      Set<String> unknownPredicates = enrichment.getUnknownPredicates();
      System.out.println(unknownPredicates);
      String query = "INSERT DATA { " + enrichment.getTriples() + " }";
      Triplestore.executeUpdateQuery(query,  GRAPHDB_UPDATE_ENDPOINT);

      String json = JsonUtil.setToJson(unknownPredicates);
      System.out.println("***********" + "DONE: check-ontology" + "***********");

      return json;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return "ERROR: " + e.getMessage();
    }
  }

}