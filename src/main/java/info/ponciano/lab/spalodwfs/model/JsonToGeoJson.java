package info.ponciano.lab.spalodwfs.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToGeoJson {

    /**
     * Converts a JSON file to a GeoJSON file.
     *
     * @param inputPath  The path of the input JSON file.
     * @param outputPath The path of the output GeoJSON file.
     * @throws Exception if the input file cannot be parsed into a JSONArray.
     * @author Dr. Jean-Jacques Ponciano
     */
    public static void convertJsonToGeoJson(String inputPath, String outputPath) throws Exception {
        // Creates an instance of JSONParser to parse JSON data.
        JSONParser parser = new JSONParser();

        // Parses the content of the input file.
        Object object = parser.parse(new FileReader(inputPath));

        // Checks if the parsed object is an instance of JSONArray.
        if (!(object instanceof JSONArray)) {
            // Throws an exception if the parsed object is not an instance of JSONArray.
            throw new Exception("The JSON is not an array");
        }

        // Creates a new JSONObject for the final GeoJSON object.
        JSONObject geoJson = new JSONObject();

        // Sets the type attribute of the GeoJSON object to "FeatureCollection".
        geoJson.put("type", "FeatureCollection");

        // Creates a new JSONArray to hold the GeoJSON features.
        JSONArray features = new JSONArray();

        // Casts the parsed object to JSONArray.
        JSONArray inputArray = (JSONArray) object;

        // Starts a loop through each object in the inputArray.
        for (Object o : inputArray) {
            // Converts each object from the input array to a JSONObject.
            JSONObject inputObject = (JSONObject) o;

            // Creates a new JSONObject for each GeoJSON feature.
            JSONObject feature = new JSONObject();

            // Sets the type of each feature to "Feature".
            feature.put("type", "Feature");

            // Creates a new JSONObject for the geometry of each GeoJSON feature.
            JSONObject geometry = new JSONObject();

            // Sets the type of the geometry to "Point".
            geometry.put("type", "Point");

            // Creates a new JSONArray for the coordinates of the point geometry.
            geometry.put("coordinates", new JSONArray());

            double longitude = 0;
            double latitude = 0;
            Set<String> reserved2geometry = new HashSet();
            reserved2geometry.add("latitude");
            reserved2geometry.add("longitude");
            reserved2geometry.add("lat");
            reserved2geometry.add("long");
            reserved2geometry.add("coord");
            if (inputObject.get("coord") != null) {
                // Define the regular expression pattern for extracting double values
                Pattern pattern = Pattern.compile("Point\\((-?\\d+\\.\\d+) (-?\\d+\\.\\d+)\\)");

                // Create a Matcher object to find the pattern in the input string
                Matcher matcher = pattern.matcher((String) inputObject.get("coord"));

                // Check if the pattern is found and extract the double values
                if (matcher.find()) {
                    longitude = Double.parseDouble(matcher.group(1));
                    latitude = Double.parseDouble(matcher.group(2));

                }
            } else if (inputObject.get("longitude") != null && inputObject.get("latitude") != null) {

                // Parses the longitude from the input JSON object and adds it to the
                // coordinates array.
                longitude = Double.parseDouble((String) inputObject.get("longitude"));

                // Parses the latitude from the input JSON object and adds it to the coordinates
                // array.
                latitude = Double.parseDouble((String) inputObject.get("latitude"));
            } else if (inputObject.get("lon") != null && inputObject.get("lat") != null) {
                // Parses the longitude from the input JSON object and adds it to the
                // coordinates array.
                longitude = Double.parseDouble((String) inputObject.get("lon"));
                // Parses the latitude from the input JSON object and adds it to the coordinates
                // array.
                latitude = Double.parseDouble((String) inputObject.get("lat"));
            }

            ((JSONArray) geometry.get("coordinates"))
                    .add(longitude);
            ((JSONArray) geometry.get("coordinates")).add(latitude);

            // Adds the geometry object to the GeoJSON feature.
            feature.put("geometry", geometry);

            // Creates a new JSONObject for the properties of the GeoJSON feature.
            JSONObject properties = new JSONObject();

            // --------------------------------------------------------------------------------------------------------------------------------------------------------------
            // Iterate through each key-value pair in the inputObject
            for (Object keyObj : inputObject.keySet()) {
                String key = (String) keyObj;
                if (!reserved2geometry.contains(key)) {
                    // Adds the "item" from the input JSON object to the properties of the GeoJSON
                    // feature.
                    properties.put(key, inputObject.get(key));

                    // Adds the properties object to the GeoJSON feature.
                    feature.put("properties", properties);
                }
            }
            // Adds the GeoJSON feature to the features array.
            features.add(feature);

        }

        // Adds the features array to the GeoJSON object.
        geoJson.put("features", features);

        // Writes the GeoJSON object to the output file.
        try (FileWriter file = new FileWriter(outputPath)) {
            // Logs to the console that the writing process has started.
            System.out.println("Writing" + outputPath);

            // Writes the GeoJSON object to the file.
            file.write(geoJson.toJSONString());

            // Flushes any buffered output to the file.
            file.flush();
        } catch (IOException e) {
            // Prints the stack trace for any IOException that might occur during file
            // operations.
            e.printStackTrace();
        }
    }

}
