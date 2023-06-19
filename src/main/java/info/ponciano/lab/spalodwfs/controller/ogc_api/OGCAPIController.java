package info.ponciano.lab.spalodwfs.controller.ogc_api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import info.ponciano.lab.spalodwfs.model.Triplestore;

@RequestMapping("/api/spalodWFS")
@RestController
public class OGCAPIController {

    @PostMapping("/")
    public String landingPage() {
        String results = "{\"head\":{\"vars\":\n";
        results += "[\"Feature\", \"URL\", \"JSON\"]},\"results\":{\"bindings\":[\n";
        results += "{\"Feature\": {\"value\": \"Conformance\"},\"URL\": {\"value\": \"https://localhost:8081/api/spalodWFS/conformance\"}, \"JSON\": {\"value\": \"https://localhost:8081/api/spalodWFS/conformance\"}},\n";
        results += "{\"Feature\": {\"value\": \"Collections\"},\"URL\": {\"value\": \"https://localhost:8081/api/spalodWFS/collections\"}, \"JSON\": {\"value\": \"https://localhost:8081/api/spalodWFS/collections\"}}\n";
        results += "]}}";
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
        String query = "SELECT ?dataset WHERE {\n?collection <http://www.w3.org/ns/dcat#dataset> ?dataset .\nFILTER(?collection = <http://lab.ponciano.info/ont/spalod#" + collectionId + ">)\n}";
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
    public String datasetItems(@PathVariable String collectionId, @PathVariable String datasetId) {
        System.out.println("***********" + "/collections/" + collectionId + "/items/" + datasetId + "***********");
        String query = "SELECT ?item ?itemLabel ?category ?coordinates WHERE {\n?dataset <http://lab.ponciano.info/ont/spalod#hasItem> ?item .\nFILTER(?dataset = <http://lab.ponciano.info/ont/spalod#" + datasetId + ">)\n?item <http://lab.ponciano.info/ont/spalod#itemLabel> ?itemLabel .\n?item <http://lab.ponciano.info/ont/spalod#category> ?category .\n?item <http://lab.ponciano.info/ont/spalod#coordinates> ?coordinates\n}";
        System.out.println(query);
        String results;
        results = Triplestore.executeSelectQuery(query, "http://localhost:7200/repositories/Spalod");
        //results = Triplestore.get().executeSelectQuery(query);
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
        return results;
    }
}
