package lz.entities;

public class Header {

    int flagLengthBits = 8;

    byte decompressedLengthHighByte;
    byte decompressedLengthLowByte;
    byte repeatLengthBits;

    boolean optionStopFirstPattern = true;

    public static Header LATIN_SPRITES_HEADER = new Header(
            (byte) Integer.parseInt("00", 16),
            (byte) Integer.parseInt("28", 16),
            (byte) Integer.parseInt("04", 16),
            false);

    public Header(byte decompressedLengthLowByte, byte decompressedLengthHighByte, byte repeatLengthBits) {
        this.decompressedLengthLowByte = decompressedLengthLowByte;
        this.decompressedLengthHighByte = decompressedLengthHighByte;
        this.repeatLengthBits = repeatLengthBits;
    }

    public Header(byte decompressedLengthLowByte, byte decompressedLengthHighByte, byte repeatLengthBits, boolean optionStopFirstPattern) {
        this(decompressedLengthLowByte, decompressedLengthHighByte, repeatLengthBits);
        this.optionStopFirstPattern = optionStopFirstPattern;
    }

    public int getDecompressedLength() {
        int length = (decompressedLengthHighByte & 0xFF) << 8;
        length = (length & 0xFFFF) | (decompressedLengthLowByte & 0xFF);
        return length;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[3];
        bytes[0] = decompressedLengthLowByte;
        bytes[1] = decompressedLengthHighByte;
        bytes[2] = repeatLengthBits;
        return bytes;
    }

    public int getPositionMask() {
        int i = 0xFF;
        i = i << getSizeShift();
        return i | 0xFF;
    }

    public int getSizeShift() {
        return 8 - repeatLengthBits;
    }

    public int getMaxSize() {
        int maxSize = (0xFF >> getSizeShift())+2;
        if (maxSize%2!=0) maxSize--;
        return maxSize;
    }

    public int getMaxPosition() {
        return getPositionMask();
    }

    public void setFlagLength(int flagLengthBits) {
        this.flagLengthBits = flagLengthBits;
    }

    public int getFlagLength() {
        return flagLengthBits;
    }

    public boolean isOptionStopFirstPattern() {
        return optionStopFirstPattern;
    }

    public void setOptionStopFirstPattern(boolean optionStopFirstPattern) {
        this.optionStopFirstPattern = optionStopFirstPattern;
    }
}
