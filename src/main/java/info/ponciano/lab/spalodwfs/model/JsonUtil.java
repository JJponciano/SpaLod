package info.ponciano.lab.spalodwfs.model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
public class JsonUtil {
    public static String setToJson(Set<String> set) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting set to JSON", e);
        }
    }
}