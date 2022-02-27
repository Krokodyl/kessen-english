package compression;

public class Segment {

    int offset;
    String data;

    public Segment(int offset, String data) {
        this.offset = offset;
        this.data = data;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getOffset() {
        return offset;
    }

    public String getData() {
        return data;
    }
}
