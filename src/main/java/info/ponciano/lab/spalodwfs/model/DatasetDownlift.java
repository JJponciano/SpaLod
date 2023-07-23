package info.ponciano.lab.spalodwfs.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.ponciano.lab.spalodwfs.mvc.models.semantic.KB;

public class DatasetDownlift {
    String datasetId;
    JSONObject data = new JSONObject();

    public DatasetDownlift(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    // "SELECT ?f ?g ?wkt ?fp ?o WHERE { spalod:67504af5-5d32-4815-ae53-fb879f4bb0c7
    // spalod:hasFeature ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?wkt .
    // ?f ?fp ?o}";
    private JSONObject extractGeometry(String wkt) {
        String pointString = wkt;

        // Extract the coordinates from the point string
        int start = pointString.indexOf('(') + 1; // After '('
        int end = pointString.indexOf(')'); // Before ')'
        String coordinatesString = pointString.substring(start, end);

        // Split the coordinates string into longitude and latitude
        String[] coordinatesArray = coordinatesString.split(" ");
        double longitude = Double.parseDouble(coordinatesArray[0]);
        double latitude = Double.parseDouble(coordinatesArray[1]);

        // Build the JSON object
        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", new double[] { longitude, latitude });

        // System.out.println(geometry.toString());
        return geometry;
    }

    public JSONObject getData() {

        String query;
        String result;
        setMetadata();

        query = KB.PREFIX
                + "SELECT ?f ?wkt WHERE { spalod:" + this.datasetId
                + " spalod:hasFeature  ?f. ?f geosparql:hasGeometry ?g. ?g geosparql:asWKT ?wkt . } ";
        result = Triplestore.executeSelectQuery(query, KB.GRAPHDB_QUERY_ENDPOINT);

        // Parse the JSON
        JSONObject resultJson = new JSONObject(result);

        // Access the 'results' object
        JSONObject results = resultJson.getJSONObject("results");

        // Access the 'bindings' array
        JSONArray bindings = results.getJSONArray("bindings");

        // Iterate over the bindings
        JSONArray features = new JSONArray();

        for (int i = 0; i < bindings.length(); i++) {

            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            JSONObject propertiesJson = new JSONObject();
            JSONObject jsonGEO = new JSONObject();

            // GET THE GEOMETRY AND NAME
            JSONObject binding = bindings.getJSONObject(i);
            JSONObject f = binding.getJSONObject("f");
            String wkt = binding.getJSONObject("wkt").getString("value");
            String feature_uri = f.getString("value");
            feature.put("name", feature_uri);
            jsonGEO = extractGeometry(wkt);

            // GET FEATURES
            // get now each properties linked to the feature
            query = KB.PREFIX
                    + "SELECT ?p ?o WHERE {<" + feature_uri + "> ?p ?o}";
            result = Triplestore.executeSelectQuery(query, KB.GRAPHDB_QUERY_ENDPOINT);
            JSONObject features_resultJson = new JSONObject(result);

            // Access the 'results' object
            JSONObject features_results = features_resultJson.getJSONObject("results");

            // Access the 'bindings' array
            JSONArray features_bindings = features_results.getJSONArray("bindings");

            // Iterate over the bindings
            for (int j = 0; i < features_bindings.length(); i++) {
                JSONObject features_binding = features_bindings.getJSONObject(i);
                String p = features_binding.getJSONObject("p").getString("value");
                String o = features_binding.getJSONObject("o").getString("value");
                propertiesJson.put(p, o);
            }
            feature.put("geometry", jsonGEO);
            feature.put("properties", propertiesJson);
            features.put(feature);
        }
        data.put("features", features);

        return data;

    }

    private void setMetadata() throws JSONException {
        String query = KB.PREFIX
                + "SELECT ?p ?o WHERE { spalod:" + this.datasetId +
                "?p ?o . FILTER( ?p != spalod:hasFeature ) }";
        String result = Triplestore.executeSelectQuery(query, KB.GRAPHDB_QUERY_ENDPOINT);
        JSONObject resultJson = new JSONObject(result);
        JSONObject results = resultJson.getJSONObject("results");
        JSONArray bindings = results.getJSONArray("bindings");
        for (int i = 0; i < bindings.length(); i++) {
            JSONObject binding = bindings.getJSONObject(i);
            String p = binding.getJSONObject("p").getString("value");
            String o = binding.getJSONObject("o").getString("value");
            this.data.put(p, o);
        }
    }

    public static void main(String[] args) {
        JSONObject data = new DatasetDownlift("67504af5-5d32-4815-ae53-fb879f4bb0c7").getData();
         System.out.println(data);
    }
}
