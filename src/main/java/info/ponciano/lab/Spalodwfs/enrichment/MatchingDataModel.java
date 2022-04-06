package info.ponciano.lab.Spalodwfs.enrichment;

import java.util.Objects;

public class MatchingDataModel {
    public String input;
    public String value;

    public MatchingDataModel(String input, String value) {
        this.input = input;
        this.value = value;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingDataModel that = (MatchingDataModel) o;

        return Objects.equals(input, that.input);
    }

    @Override
    public int hashCode() {
        return input != null ? input.hashCode() : 0;
    }
}
