package info.ponciano.lab.spalodwfs.enrichment;

import java.util.ArrayList;
import java.util.List;

public class MatchingDataCreationDto {
    List<MatchingDataModel> data;

    // default and parameterized constructor
    public MatchingDataCreationDto(List<MatchingDataModel> data) {
        this.data = data;
    }

    public MatchingDataCreationDto() {
        this.data = new ArrayList<>();
    }

    public List<MatchingDataModel> getData() {
        return data;
    }

    public void setData(List<MatchingDataModel> data) {
        this.data = data;
    }

    public void add(MatchingDataModel book) {
        this.data.add(book);
    }

    @Override
    public String toString() {
        String s = "MatchingDataCreationDto{\n";
        for (MatchingDataModel datum : data) {
            s += datum + "\n";
        }
        s += "}\n";
        return s;
    }

    /**
     * Test if this entity contains the specified element.
     * true if this list contains the specified element
     * @param mdm element whose presence in this entity is to be tested
     * @return true if this entity contains the specified element
     */
    public boolean contains(MatchingDataModel mdm) {
        return this.data.contains(mdm);
    }
}
