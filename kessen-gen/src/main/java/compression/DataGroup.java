package compression;

import java.util.ArrayList;
import java.util.List;

public class DataGroup {

    int offset = 0;
    int offsetCompressedData = 0;
    int repetition;
    String pattern = null;
    boolean compressed = false;

    public DataGroup(int offset, String pattern, int repetition) {
        this.offset = offset;
        this.pattern = pattern;
        this.repetition = repetition;
    }

    public void append(String s) {
        if (!compressed && pattern.equals(s)) {
            compressed = true;
            repetition++;
        }
        else if (compressed) {
            repetition++;
        } else {
            pattern += s;
        }
    }

    public int getOffsetCompressedData() {
        return offsetCompressedData;
    }

    public void setOffsetCompressedData(int offsetCompressedData) {
        this.offsetCompressedData = offsetCompressedData;
    }

    public String getDecompressedData() {
        return new String(new char[getRepetition()]).replace("\0", getPattern());
    }

    public List<CompressedByte> getCompressedByte(){
        List<CompressedByte> bytes = new ArrayList<>();
        String[] split = pattern.split("(?<=\\G.{2})");
        if (offsetCompressedData ==0)
        for (String s:split) {
            DataByte dataByte = new DataByte(Integer.parseInt(s,16));
            bytes.add(dataByte);
        }
        if (compressed) {
            if (repetition>16) {
                System.out.print("");
            }
            int byteCount = pattern.length() / 2;
            int size = byteCount * (repetition-1);
            int position = byteCount -1;
            if (offsetCompressedData !=0) {
                size = byteCount * repetition;
                position = (offset/2 - offsetCompressedData/2) - 1;
            }

            if (offset==490) {
                System.out.println();
            }

            if (byteCount==1 && repetition==1) {
                DataByte dataByte = new DataByte(Integer.parseInt(split[0],16));
                bytes.add(dataByte);
            }
            else {
                while (size > 0) {
                    if (size > 15) {
                        RepeatByte repeatByte = new RepeatByte(15 * byteCount - 2, position);
                        bytes.add(repeatByte);
                    } else {
                        if (byteCount==1 && size==1) {
                            DataByte dataByte = new DataByte(Integer.parseInt(split[0],16));
                            bytes.add(dataByte);
                        } else {
                            RepeatByte repeatByte = new RepeatByte(size - 2, position);
                            bytes.add(repeatByte);
                        }
                    }
                    size -= 15;
                }
            }
        }
        return bytes;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getRepetition() {
        return repetition;
    }

    public void setRepetition(int repetition) {
        this.repetition = repetition;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "DataGroup{" +
                "offset=" + offset +
                ", offsetCompressedData=" + offsetCompressedData +
                ", repetition=" + repetition +
                ", pattern='" + pattern + '\'' +
                ", compressed=" + compressed +
                '}';
    }
}
