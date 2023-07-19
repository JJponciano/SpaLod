package info.ponciano.lab.spalodwfs.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.*;

public class JsonToGeoJson {

    public static void convertJsonToGeoJson(String inputPath, String outputPath) throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(inputPath));
        if (!(object instanceof JSONArray)) {
            throw new Exception("The JSON is not an array");
        }

        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();

        JSONArray inputArray = (JSONArray) object;
        for (Object o : inputArray) {
            JSONObject inputObject = (JSONObject) o;
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", new JSONArray());
            ((JSONArray) geometry.get("coordinates")).add(Double.parseDouble((String) inputObject.get("longitude")));
            ((JSONArray) geometry.get("coordinates")).add(Double.parseDouble((String) inputObject.get("latitude")));
            feature.put("geometry", geometry);

            JSONObject properties = new JSONObject();
            properties.put("item", inputObject.get("item"));
            properties.put("itemLabel", inputObject.get("itemLabel"));
            feature.put("properties", properties);

            features.add(feature);
        }

        geoJson.put("features", features);

        // Write JSON file
        try (FileWriter file = new FileWriter(outputPath)) {
            System.out.println("Writing"+outputPath);
            file.write(geoJson.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
