package info.ponciano.lab.spalodwfs.enrichment;

import java.util.Arrays;
import java.util.List;

public class Infer {
   private String what;

   public Infer(String what) {
      this.what = what;
   }

   public Infer() {
   }

   public String getWhat() {
      return what;
   }

   public void setWhat(String what) {
      this.what = what;
   }

   @Override
   public String toString() {
      return "Infer{" +
              "what='" + what + '\'' +
              '}';
   }
}