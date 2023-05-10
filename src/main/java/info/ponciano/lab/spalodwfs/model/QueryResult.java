package info.ponciano.lab.spalodwfs.model;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;


public class QueryResult {
    private List<Map<String, Object>> resultList;
    private String json;

    public QueryResult(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }

    public List<Map<String, Object>> getResultList() {
        return resultList;
    }

    public Map<String, Object> get(int index) {
        return resultList.get(index);
    }

    public void add(Map<String, Object> bindingMap) {
        resultList.add(bindingMap);
    }

    public void remove(int index) {
        resultList.remove(index);
    }

    public boolean contains(Map<String, Object> bindingMap) {
        return resultList.contains(bindingMap);
    }
    

    @Override
    public String toString() {
        return json;
    }
    public static String convertResultSetToJavaObject(ResultSet results) {
        // Create a QueryResult object to wrap the results list
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        return outputStream.toString();
    }

    public void setResultList(List<Map<String, Object>> resultList) {
        this.resultList = resultList;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
