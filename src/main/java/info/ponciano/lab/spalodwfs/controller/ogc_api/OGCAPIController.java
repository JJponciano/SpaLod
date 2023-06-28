package info.ponciano.lab.spalodwfs.controller.ogc_api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import info.ponciano.lab.spalodwfs.model.Triplestore;
import org.json.JSONObject;
import org.apache.jena.base.Sys;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;


@RequestMapping("/api/spalodWFS")
@RestController
public class OGCAPIController {

    /**
     * Return the landing page of the API
     * @return String[][] corresponding to the landing page
     */
    @PostMapping("/")
    public String landingPage() {
        String results = "{\"head\":{\"vars\":\n";
        results += "[\"Feature\", \"HTML\", \"JSON\"]},\"results\":{\"bindings\":[\n";
        results += "{\"Feature\": {\"value\": \"Conformance\"},\"HTML\": {\"value\": \"https://localhost:8080/spalodWFS/conformance\"}, \"JSON\": {\"value\": \"https://localhost:8081/api/spalodWFS/conformance\"}},\n";
        results += "{\"Feature\": {\"value\": \"Collections\"},\"HTML\": {\"value\": \"https://localhost:8080/spalodWFS/collections\"}, \"JSON\": {\"value\": \"https://localhost:8081/api/spalodWFS/collections\"}}\n";
        results += "]}}";
        System.out.println(results);

        return results;
    }

    /**
     * Return the list of the collections
     * @return String[][] corresponding to the collection list
     */
    @PostMapping("/collections")
    public String collectionsList() {
        System.out.println("***********" + "/collections" + "***********");
        String query = "SELECT DISTINCT ?collections ?name WHERE {\n?collections <http://www.w3.org/ns/dcat#dataset> ?dataset .\n ?collections <http://purl.org/dc/terms/title> ?name .}";
        System.out.println(query);
        String results;
        results = Triplestore.executeSelectQuery(query, "http://localhost:7200/repositories/Spalod");
        //results = Triplestore.get().executeSelectQuery(query);
        return results;
    }

    /**
     * Return a specific collection
     * @param collectionId Unique identifier of the collection
     * @return String[][] corresponding to the specific collection
     */
    @PostMapping("/collections/{collectionId}")
    public String collectionQuery(@PathVariable String collectionId) {
        System.out.println("***********" + "/collections/" + collectionId + "***********");
        String query = "SELECT ?title ?description ?publisher ?dataset WHERE {\n?collection <http://purl.org/dc/terms/title> ?title .\n?collection <http://purl.org/dc/terms/description> ?description .\n?collection <http://purl.org/dc/terms/publisher> ?publisher .\n?collection <http://www.w3.org/ns/dcat#dataset> ?dataset .\nFILTER(?collection = <http://lab.ponciano.info/ont/spalod#" + collectionId + ">)\n}";
        System.out.println(query);
        String results;
        results = Triplestore.executeSelectQuery(query, "http://localhost:7200/repositories/Spalod");
        //results = Triplestore.get().executeSelectQuery(query);
        return results;
    }

    /**
     * Return the list of the datasets of a specific collection
     * @param collectionId Unique identifier of the collection
     * @return String[][] corresponding to the specific collection
     */
    @PostMapping("/collections/{collectionId}/items")
    public String datasetList(@PathVariable String collectionId) {
        System.out.println("***********" + "/collections/" + collectionId + "/items/***********");
        String query = "SELECT ?dataset ?title ?description ?publisher ?distribution WHERE {\n?collection <http://www.w3.org/ns/dcat#dataset> ?dataset .\nFILTER(?collection = <http://lab.ponciano.info/ont/spalod#" + collectionId + ">)\n?dataset <http://purl.org/dc/terms/title> ?title .\n?dataset <http://purl.org/dc/terms/description> ?description .\n?dataset <http://purl.org/dc/terms/publisher> ?publisher .\n?dataset <http://www.w3.org/ns/dcat#distribution> ?distribution .\n}";
        System.out.println(query);
        String results;
        results = Triplestore.executeSelectQuery(query, "http://localhost:7200/repositories/Spalod");
        //results = Triplestore.get().executeSelectQuery(query);
        return results;
    }

    /**
     * Return the list of the items inside a specific dataset
     * @param collectionId Unique identifier of the collection
     * @param datasetId Unique identifier of the dataset
     * @return String[][] corresponding to the list of the items of the dataset
     */
    @PostMapping("/collections/{collectionId}/items/{datasetId}")
    public String datasetItems(@PathVariable String collectionId, @PathVariable String datasetId, @RequestParam(value = "bbox", required = false) String bbox, @RequestParam(value = "time", required = false) String time) {
        System.out.println("***********" + "/collections/" + collectionId + "/items/" + datasetId + "***********");
        
        // Get the item predicates
        String query = "SELECT ?predicate WHERE {\n?dataset <http://lab.ponciano.info/ont/spalod#hasItem> ?item .\nFILTER(?dataset = <http://lab.ponciano.info/ont/spalod#" + datasetId + ">)\n?item ?predicate ?object\n}";
        System.out.println(query);
    //    String query="SELECT ?object WHERE {?dataset <http://lab.ponciano.info/ont/spalod#hasItem> ?item . FILTER(?dataset = <http://lab.ponciano.info/ont/spalod#" + datasetId + ">) ?item ?predicate ?object .}";
        String results;
        results = Triplestore.get().executeSelectQuery(query);
        System.out.println(results);
        JSONObject jsonResult = new JSONObject(results);
        JSONObject resultsObject = jsonResult.getJSONObject("results");
        JSONArray bindings = resultsObject.getJSONArray("bindings");
        String[] predicates = new String[bindings.length()];
        for (int i = 0; i < bindings.length(); i++) {
            JSONObject binding = bindings.getJSONObject(i);
            JSONObject predicate = binding.getJSONObject("predicate");
            predicates[i] = predicate.getString("value").split("#")[1];
        }

        // Get the item values
        query = "SELECT ?itemID ";
        for (int i = 0; i < predicates.length; i++) {
            
            try {
                query += "?" + URLDecoder.decode(predicates[i], "UTF-8").replace(" ", "").replace("-", "") + " ";
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        query += "WHERE {\n?dataset <http://lab.ponciano.info/ont/spalod#hasItem> ?itemID .\nFILTER(?dataset = <http://lab.ponciano.info/ont/spalod#" + datasetId + ">)\n";
        for (int i = 0; i < predicates.length; i++) {
            try {
                query += "?itemID <http://lab.ponciano.info/ont/spalod#" + predicates[i] + "> ?" + URLDecoder.decode(predicates[i],"UTF-8").replace(" ", "").replace("-", "") + " .\n";
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Filter by bbox
        if (bbox != null && !bbox.isEmpty()) {
            String[] bboxCoordinates = bbox.split(",");
            if (bboxCoordinates.length == 4) {
                String latitude1 = bboxCoordinates[0];
                String longitude1 = bboxCoordinates[1];
                String latitude2 = bboxCoordinates[2];
                String longitude2 = bboxCoordinates[3];

                query += "\nFILTER(?latitude >= " + latitude1 + " && ?latitude <= " + latitude2 +
                        " && ?longitude >= " + longitude1 + " && ?longitude <= " + longitude2 + ")";
            }
        }
        
        query += "}";
        System.out.println(query);
        //results = Triplestore.get().executeSelectQuery(query);
        results = Triplestore.executeSelectQuery(query, "http://localhost:7200/repositories/Spalod");
        System.out.println(results);

        return results;
    }

    /**
     * Return the conformance
     * @return String[][] corresponding to the conformance
     */
    @PostMapping("/conformance")
    public String conformance() {
        String results = "{\"head\":{\"vars\":\n";
        results += "[\"Feature\", \"URL\"]},\"results\":{\"bindings\":[\n";
        results += "{\"Feature\": {\"value\": \"Core of OGC API Records 1.0\"},\"URL\": {\"value\": \"http://www.opengis.net/spec/ogcapi-records-1/1.0/conf/record-core\"}},\n";
        results += "{\"Feature\": {\"value\": \"JSON of OGC API Records 1.0\"},\"URL\": {\"value\": \"http://www.opengis.net/spec/ogcapi-records-1/1.0/conf/json\"}},\n";
        results += "{\"Feature\": {\"value\": \"HTML of OGC API Records 1.0\"}, \"URL\": {\"value\": \"http://www.opengis.net/spec/ogcapi-records-1/1.0/conf/html\"}}\n";
        results += "]}}";
        System.out.println(results);

        return results;
    }
}
