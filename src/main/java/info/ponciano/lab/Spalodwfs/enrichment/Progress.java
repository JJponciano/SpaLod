package info.ponciano.lab.spalodwfs.enrichment;


public class Progress {
    private int value;
    private String message;

    public Progress(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    @Override
    public String toString() {
        return "Progress{" +
                "value=" + value +
                ", message='" + message + '\'' +
                '}';
    }
}
