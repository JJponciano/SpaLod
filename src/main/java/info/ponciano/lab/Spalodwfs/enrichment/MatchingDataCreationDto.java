package info.ponciano.lab.Spalodwfs.enrichment;

import java.util.ArrayList;
import java.util.List;

public class MatchingDataCreationDto {
    List<MatchingDataModel> data;

    // default and parameterized constructor
    public MatchingDataCreationDto(List<MatchingDataModel> data) {
        this.data = data;
    }

    public MatchingDataCreationDto() {
        this.data =new ArrayList<>();
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
}
