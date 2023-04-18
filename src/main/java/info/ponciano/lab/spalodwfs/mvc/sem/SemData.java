package info.ponciano.lab.spalodwfs.mvc.sem;

import java.util.Arrays;
import java.util.List;

public class SemData {
   private String [][]data;
   private String output;

   public SemData(String[][] data, String ontologyPath) {
      this.data = data;
      this.output = ontologyPath;
   }

   public SemData(List<String> header, List<String[]> data, String ontologyPath) {
      this.data = new String[data.size()+1][header.size()];
      for (int i=0;i<header.size();i++) {
         this.data[0][i]=header.get(i);
      }
      for (int i=0;i<data.size();i++) {
         this.data[i+1]=data.get(i);
      }
      this.output = ontologyPath;
   }

   public String getOutput() {
      return output;
   }

   public void setOutput(String output) {
      this.output = output;
   }

   public String[][] getData() {
      return data;
   }

   public void setData(String[][] data) {
      this.data = data;
   }

   @Override
   public String toString() {
      return "DataLod{" +
              "data=" + Arrays.toString(data) +
              ", ontologyPath='" + output + '\'' +
              '}';
   }
}
