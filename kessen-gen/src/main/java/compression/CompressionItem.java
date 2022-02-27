package compression;

public class CompressionItem {

    int offset;
    CompressedByte compressedByte;

    public CompressionItem(int offset, CompressedByte compressedByte) {
        this.offset = offset;
        this.compressedByte = compressedByte;
    }
}
