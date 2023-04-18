package info.ponciano.lab.spalodwfs.model;
public class TripleOperation {
    private String operation; // "add" or "remove"
    private TripleData tripleData;

    public TripleOperation() {
    }

    public TripleOperation(String operation, TripleData tripleData) {
        this.operation = operation;
        this.tripleData = tripleData;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public TripleData getTripleData() {
        return tripleData;
    }

    public void setTripleData(TripleData tripleData) {
        this.tripleData = tripleData;
    }

    @Override
    public String toString() {
        return "TripleOperation{" +
                "operation='" + operation + '\'' +
                ", tripleData=" + tripleData +
                '}';
    }
}
