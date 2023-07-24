package info.ponciano.lab.spalodwfs.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

public class JsonToGeoJsonTest {
    @Test
    void testConvertJsonToGeoJson() throws Exception {
        String inputPath = "src/test/resources/test.json";
        String outputPath = "src/test/resources/test_output.json";
        String expectedOutputPath = "src/test/resources/test_geo.json";
 
        // Run the method to test
        JsonToGeoJson.convertJsonToGeoJson(inputPath, outputPath);

        // Load the output and expected output
        JSONParser parser = new JSONParser();
        JSONObject actualOutput = (JSONObject) parser.parse(new FileReader(outputPath));
        System.out.println(actualOutput);
    }
} 
