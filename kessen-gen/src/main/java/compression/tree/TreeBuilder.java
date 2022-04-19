package compression.tree;

import compression.CompressedByte;
import compression.DataByte;
import compression.FlagByte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TreeBuilder {

    public TreeBuilder() {

    }

    public void build(String data) {
        Node root = new Node(data, null);
        String pattern = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    }

    public static void main(String[] args) {
        new TreeBuilder().test("00ff00ff00ff00ff00ff00ff00ff00ff" +
                "f8ff80ff80ff80ff80ff80ff80ff80ff" +
                "01");
    }

    public void test(String data) {
        //data = "00000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC3FF99FF99FF99FF99FFFFFFFFFFFFFFE7FFC7FFE7FFE7FFE7FFFFFFFFFFFFFF83FF79FFF9FFF9FFF3FFFFFFFFFFFFFF81FFF3FFE7FFC3FFF9FFFFFFFFFFFFFFF3FFE3FFD3FFB3FF73FFFFFFFFFFFFFF81FF9FFF9FFF9FFF83FFFFFFFFFFFFFFE3FFCFFF9FFF9FFF83FFFFFFFFFFFFFF81FFF9FFF9FFF3FFE7FFFFFFFFFFFFFFC3FF99FF99FF99FFC3FFFFFFFFFFFFFFC3FF99FF99FF99FFC1FFFFFFFFFFFFFFFBFFFBFFF7FFF7FFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF81FF00807F807F9F609F609F679F679F67FF0000FF00FFFF00FF00FFFFFFFFFFFF";
        Node root = new Node(data, null);
        List<Node> untreatedNodes = root.getUntreatedNodes();
        String bestPattern = getBestPattern(untreatedNodes);
        System.out.println("best pattern "+bestPattern);
        while (bestPattern!=null && !untreatedNodes.isEmpty()) {
            root.split(bestPattern);
            //root.println();
            untreatedNodes = root.getUntreatedNodes();
            bestPattern = getBestPattern(untreatedNodes);
            System.out.println("best pattern "+bestPattern);
        }
        // Update Repeat
        Map<String, Integer> patternOffset = new HashMap<>();
        root.updateRepeat(patternOffset);
        System.out.println();
        root.println();
        checkTree(data, root);
        System.out.println("dataSize="+data.length());
        getCompressedSize(root);
        List<CompressedByte> compressedBytes = root.getCompressedByte();

        for (CompressedByte compressedByte : compressedBytes) {
            System.out.println(compressedByte);
        }


        CompressedByte[] buffer = new CompressedByte[8];
        int k = 0;
        for (CompressedByte compressedByte : compressedBytes) {
            buffer[k++] = compressedByte;
            if (k==buffer.length) {
                String bits = "";
                for (CompressedByte cb:buffer) {
                    if (cb == null || cb instanceof DataByte) bits = "1" + bits;
                    else bits = "0" + bits;
                }
                FlagByte f = new FlagByte((byte) (Integer.parseInt(bits,2) & 0xFF));
                System.out.print(f.getBytesHex()+" ");
                for (CompressedByte cb:buffer)
                System.out.print(cb.getBytesHex()+" ");
                k=0;
                buffer = new CompressedByte[8];
            }
        }
        if (k!=0) {
            String bits = "";
            for (CompressedByte cb:buffer) {
                if (cb == null || cb instanceof DataByte) bits = "1" + bits;
                else bits = "0" + bits;
            }
            FlagByte f = new FlagByte((byte) (Integer.parseInt(bits,2) & 0xFF));
            System.out.print(f.getBytesHex()+" ");
            for (CompressedByte cb:buffer)
                if (cb!=null) System.out.print(cb.getBytesHex()+" ");
        }

    }

    public int getCompressedSize(Node root) {
        int repeatCount = root.countType(NodeType.REPEAT);
        int untreatedSize = root.getTreeSize(NodeType.UNTREATED);
        System.out.println("repeatCount="+repeatCount);
        System.out.println("untreatedSize="+untreatedSize);
        System.out.println("flagCount="+(repeatCount+untreatedSize)/8);
        System.out.println("compressedSize="+((root.getTreeSize())+(repeatCount+untreatedSize)/8));
        return root.getTreeSize();
    }

    public void checkTree(String data, Node root) {
        String treeData = root.getTreeData();
        System.out.println("Check Tree Data : "+treeData.equals(data));
    }

    public String getBestPattern(List<Node> nodes) {
        List<Tuple> tuples = new ArrayList<>();

        String data = "";
        for (Node node : nodes) {
            data += node.getData() + "  ";
        }

        int length = 17;

        List<String> patterns = new ArrayList<>();
        while (length>3*2) {
            for (int i = 0; i < data.length() - length*2; i = i + 2) {
                String substring = data.substring(i, i + length*2);

                if (!substring.contains(" ") && !patterns.contains(substring)) patterns.add(substring);
            }
            length--;
        }

        for (String s : patterns) {
            int i = countMaxConsecutivePattern(data, s);
            int c = countNonOverlappingPattern(data, s);
            //System.out.println(i + " " + s);
            if (c>1) {
                int compression = (s.length() / 2) * (c - 1) - 2 * (c - 1);
                Tuple t = new Tuple(s, i, c, compression);
                tuples.add(t);
            }
        }

        Collections.sort(tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                return o2.getByteCompressed()-o1.getByteCompressed();
            }
        });
        int totalBytesCompressed = 0;
        for (Tuple t:tuples) {
            //System.out.println("pattern "+t.getPattern());
            //for (String s:split) System.out.println(s);
            //System.out.println(t);
            totalBytesCompressed += t.getByteCompressed();
        }
        if (tuples.isEmpty()) {
            return null;
        }
        return tuples.get(0).getPattern();
    }

    static class Tuple {
        String pattern;
        int maxCount;
        int count;
        int byteCompressed;

        public Tuple(String pattern, int maxCount, int count, int byteCompressed) {
            this.pattern = pattern;
            this.maxCount = maxCount;
            this.count = count;
            this.byteCompressed = byteCompressed;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }

        public int getByteCompressed() {
            return byteCompressed;
        }

        public void setByteCompressed(int byteCompressed) {
            this.byteCompressed = byteCompressed;
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "pattern='" + pattern + '\'' +
                    ", maxCount=" + maxCount +
                    ", count=" + count +
                    ", byteCompressed=" + byteCompressed +
                    '}';
        }
    }

    public static void testAlgoSplit() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("../font/uncompressed-latin-bytes.txt"));
        String data = new String(encoded, StandardCharsets.US_ASCII);
        //String data = "0000000099FF99FF99FFB1B1B1B1B1B199FF99FFAAAA00000000B1B1B1";
        int length = 17;

        List<Tuple> tuples = new ArrayList<>();
        while (length>3*2) {
            List<String> patterns = new ArrayList<>();
            for (int i = 0; i < data.length() - length*2; i = i + 2) {
                String substring = data.substring(i, i + length*2);
                //System.out.println(data.substring(i,j));
                if (!patterns.contains(substring)) patterns.add(substring);
            }

            for (String s : patterns) {
                if (s.equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
                    System.out.println();
                }
                int i = countMaxConsecutivePattern(data, s);
                int c = countNonOverlappingPattern(data, s);
                //System.out.println(i + " " + s);
                if (c>1) {
                    int compression = (s.length() / 2) * (c - 1) - 2 * (c - 1);
                    Tuple t = new Tuple(s, i, c, compression);
                    tuples.add(t);
                }
            }
            length--;
        }
        Collections.sort(tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                return o2.getByteCompressed()-o1.getByteCompressed();
            }
        });
        int totalBytesCompressed = 0;
        for (Tuple t:tuples) {
            //System.out.println("pattern "+t.getPattern());
            //for (String s:split) System.out.println(s);
            System.out.println(t);
            totalBytesCompressed += t.getByteCompressed();
        }

        System.out.println("bytes = "+data.length()/2);
        System.out.println("totalBytesCompressed = "+totalBytesCompressed);

        System.out.println();
    }

    private static int countMaxConsecutivePattern(String data, String pattern) {
        int lastIndex = data.indexOf(pattern);
        int index = data.indexOf(pattern, lastIndex+pattern.length());
        int maxCount = 1;
        int count = 1;
        while (index>0) {
            if (index-pattern.length()==lastIndex) {
                count++;
                if (maxCount<count) maxCount = count;
            } else {
                count=1;
            }
            lastIndex = index;
            index = data.indexOf(pattern, lastIndex+pattern.length());
        }
        return maxCount;
    }

    private static int countNonOverlappingPattern(String data, String pattern) {
        int lastIndex = data.indexOf(pattern);
        int index = data.indexOf(pattern, lastIndex+pattern.length());
        int count = 1;
        while (index>0) {
            if (index-pattern.length()>=lastIndex) {
                count++;
            }
            lastIndex = index;
            index = data.indexOf(pattern, lastIndex+pattern.length());
        }
        return count;
    }
}
