package services;

import compression.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Compressor {

    List<CompressedByte> compressedBytes = new ArrayList<>();

    Map<String, CompressionItem> dict = new HashMap<>();
    Map<String, Segment> segments = new HashMap<>();

    /*public static void main(String[] args) {
        try {
            new Compressor().test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void test() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("../font/uncompressed-latin-bytes.txt"));
        String data = new String(encoded, StandardCharsets.US_ASCII);

        Map<String, Point> map = new HashMap<>();
        int patternLength = 16;

        int offset = 0;

        while (offset + patternLength < data.length()) {
            String read = data.substring(offset, offset + patternLength);
            int searchOffset = offset + patternLength;
            int indexOf = data.indexOf(read, searchOffset);
            if (indexOf>0) {
                while (indexOf>0) {
                    if (!map.containsKey(read)) map.put(read, 1);
                    else map.put(read, map.get(read)+1);
                    searchOffset = indexOf + patternLength;
                    indexOf = data.indexOf(read, searchOffset);
                }
            }
            offset += 2;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey()+" "+entry.getValue());
        }

        System.out.println(map.size());

    }*/

    public void analyzeData() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("../font/uncompressed-latin-bytes.txt"));
        String data = new String(encoded, StandardCharsets.US_ASCII);
        //data = "00000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC3FF99FF99FF99FF99FFFFFFFFFFFFFFE7FFC7FFE7FFE7FFE7FFFFFFFFFFFFFF83FF79FFF9FFF9FFF3FFFFFFFFFFFFFF81FFF3FFE7FFC3FFF9FFFFFFFFFFFFFFF3FFE3FFD3FFB3FF73FFFFFFFFFFFFFF81FF9FFF9FFF9FFF83FFFFFFFFFFFFFFE3FFCFFF9FFF9FFF83FFFFFFFFFFFFFF81FFF9FFF9FFF3FFE7FFFFFFFFFFFFFFC3FF99FF99FF99FFC3FFFFFFFFFFFFFFC3FF99FF99FF99FFC1FFFFFFFFFFFFFFFBFFFBFFF7FFF7FFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF81FF00807F807F9F609F609F679F679F67FF0000FF00FFFF00FF00FFFFFFFFFFFF00000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF99FF99FF99FFC3FFFFFFFFFFFFFFFFFFE7FFE7FFE7FFE7FFFFFFFFFFFFFFFFFFE7FFCFFF9FFF01FFFFFFFFFFFFFFFFFFF9FFF9FFB9FFC3FFFFFFFFFFFFFFFFFF01FFF3FFF3FFF3FFFFFFFFFFFFFFFFFFF9FFF9FFB9FFC3FFFFFFFFFFFFFFFFFF99FF99FF99FFC3FFFFFFFFFFFFFFFFFFE7FFE7FFE7FFE7FFFFFFFFFFFFFFFFFF99FF99FF99FFC3FFFFFFFFFFFFFFFFFFF9FFF9FFF3FFC7FFFFFFFFFFFFFFFFFFEFFFDFFFDFFFBFFFBFFFFFFFFFFFFFFF81FFFFFFFFFFFFFFFFFFFFFFFFFFFF9F679F679F679F679F679F679F679F67FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        //data = "0000000099FF99FF99FFB1B1B1B1B1B199FF99FFAAAA00000000B1B1B1";

        int patternLength = 2;

        List<DataGroup> current = getDataGroups(0, data, patternLength);
        Map<Integer, Integer> repetitions = new TreeMap<>();
        for (DataGroup group : current) {
            if (group.isCompressed()) {
                int byteCount = group.getPattern().length() / 2;
                int key = group.getRepetition()*byteCount;
                Integer integer = repetitions.get(key);
                if (integer==null) repetitions.put(key,1);
                else repetitions.put(key, integer+1);
            }
        }
        for (Map.Entry<Integer, Integer> integerIntegerEntry : repetitions.entrySet()) {
            System.out.println(integerIntegerEntry);
        }


        //System.out.println(current);
        while (patternLength<16) {
            List<DataGroup> temp = new ArrayList<>();
            System.out.println("DataGroup size = "+current.size()+" Compressed size = "+processCompressedBytes(getCompressedBytes(current, patternLength), false));
            for (DataGroup g : current) {
                if (g.isCompressed()) {
                    //System.out.println(g);
                    temp.add(g);
                }
                else {
                    List<DataGroup> dataGroups = getDataGroups(g.getOffset(), g.getPattern(), patternLength + 2);
                    if (dataGroups.isEmpty()) {
                        //System.out.println(g);
                        temp.add(g);
                    }
                    //System.out.println(dataGroup);
                    temp.addAll(dataGroups);
                }
            }
            current.clear();
            current.addAll(temp);
            temp.clear();
            patternLength = patternLength + 2;
        }


        /*String decompressedData = "";
        for (DataGroup g : current) {
            if (g.isCompressed()) decompressedData += g.getDecompressedData();
            else {
                List<DataGroup> dataGroups = getDataGroups(g.getOffset(), g.getPattern(), patternLength * 2);
                if (dataGroups.isEmpty()) decompressedData += g.getDecompressedData();
                for (DataGroup dataGroup : dataGroups) {
                    if (dataGroup.isCompressed()) decompressedData += g.getDecompressedData();
                }
            }
        }
        System.out.println();*/

        /*int offset = 0;// first flag byte
        int count = 0;
        for (DataGroup g : groups4) {
            if (count%8==0) {
                count=0;
                offset++;
            }
            g.setOffsetCompressedData(offset);
            if (g.isCompressed()) {
                offset += 2;
            }
            else offset++;
            count++;
            System.out.println(g);
        }*/
        System.out.println();

        Map<String, List<DataGroup>> mapPatternOffsets = new HashMap<>();
        for (DataGroup g : current) {
            String pattern = g.getPattern();
            List<DataGroup> alreadyWritten = mapPatternOffsets.get(pattern);
            if (alreadyWritten==null) {
                List<DataGroup> list = new ArrayList<>();
                list.add(g);
                mapPatternOffsets.put(pattern, list);
            } else {
                if (g.isCompressed()) {
                    int nearestOffset = 0;
                    DataGroup nearest = null;
                    for (DataGroup dg:alreadyWritten) {
                        if (dg.getPattern().equals(g.getPattern()) && dg.getRepetition()>=g.getRepetition() && dg.getOffset()>=nearestOffset) {
                            nearestOffset = dg.getOffset();
                            nearest = dg;
                        }
                    }
                    if (nearest!=null) {
                        g.setOffsetCompressedData(nearest.getOffset());
                    }
                }
                alreadyWritten.add(g);
            }
            System.out.println(g+" "+g.getCompressedByte());
        }
        System.out.println();

        compressedBytes = getCompressedBytes(current, patternLength);

        //System.out.println(compressedBytes);
        for (CompressedByte compressedByte : compressedBytes) {
            System.out.println(compressedByte);
        }


        processCompressedBytes(compressedBytes, true);

        //if (data.equals(decompressedData)) System.out.println("EQUAL");
        //repeated = new String(new char[n]).replace("\0", s);

        /*int position = Integer.parseInt("10111111111", 2);
        int size = Integer.parseInt("00000010", 2);
        byte rightPart = (byte)(position >> 8);
        byte leftPart = (byte)(size << 4);
        byte stitch = (byte) (leftPart | rightPart);
        System.out.println(Integer.toBinaryString(leftPart));
        System.out.println(Integer.toBinaryString(rightPart));
        System.out.println(Integer.toBinaryString(stitch));*/

    }

    private List<CompressedByte> getCompressedBytes(List<DataGroup> groups, int patternLength) {
        List<CompressedByte> compressedBytes = new ArrayList<>();
        for (DataGroup g : groups) {
            if (g.getPattern().equals("00")) {
                System.out.print("");
            }
            if (g.isCompressed()) compressedBytes.addAll(g.getCompressedByte());
            else {
                List<DataGroup> dataGroups = getDataGroups(g.getOffset(), g.getPattern(), patternLength * 2);
                if (dataGroups.isEmpty()) compressedBytes.addAll(g.getCompressedByte());
                for (DataGroup dataGroup : dataGroups) {
                    compressedBytes.addAll(dataGroup.getCompressedByte());
                }
            }
        }
        return compressedBytes;
    }

    private int processCompressedBytes(List<CompressedByte> compressedBytes, boolean verbose) {
        String instruction = "";
        System.out.println();
        int compressedBytesCount = 0;
        List<CompressedByte> currentRun = new ArrayList<>();
        while (!compressedBytes.isEmpty()) {
            CompressedByte compressedByte = compressedBytes.get(0);
            if (compressedByte instanceof DataByte) instruction = "1" + instruction;
            else instruction = "0" + instruction;
            currentRun.add(compressedByte);
            compressedBytes.remove(0);
            if (instruction.length()==8) {
                FlagByte flagByte = new FlagByte((byte)Integer.parseInt(instruction, 2));
                if (verbose) System.out.print(flagByte.getBytesHex()+" ");compressedBytesCount++;
                instruction = "";
                for (CompressedByte aByte : currentRun) {
                    if (verbose) System.out.print(aByte.getBytesHex()+" ");
                    compressedBytesCount+=aByte.getBytes().length;
                }
                currentRun.clear();
            }
        }
        if (!instruction.isEmpty()) {
            while (instruction.length()!=8) instruction = "1" + instruction;
            FlagByte flagByte = new FlagByte((byte)Integer.parseInt(instruction, 2));
            if (verbose) System.out.print(flagByte.getBytesHex()+" ");compressedBytesCount++;
            instruction = "";
            for (CompressedByte aByte : currentRun) {
                if (verbose) System.out.print(aByte.getBytesHex()+" ");compressedBytesCount+=aByte.getBytes().length;
            }
            currentRun.clear();
        }
        if (verbose) System.out.println();
        if (verbose) System.out.println("count = "+compressedBytesCount);
        return compressedBytesCount;
    }

    private List<DataGroup> getDataGroups(int baseOffset, String data, int patternLength) {
        List<DataGroup> groups = new ArrayList<>();
        int offset = 0;
        String lastByte = readByte(data, offset, offset + patternLength);
        int patternOffset = offset;
        DataGroup group = new DataGroup(baseOffset+offset, lastByte, 1);
        offset += patternLength;
        String readByte = readByte(data, offset, offset + patternLength);
        String futureByte = readByte(data,offset + patternLength, offset + patternLength*2);
        while (offset < data.length()) {
            if (readByte.equals(lastByte)) {
                while (readByte.equals(lastByte)) {
                    group.append(readByte);
                    lastByte = readByte;
                    offset += patternLength;
                    readByte = readByte(data, offset, offset + patternLength);
                    futureByte = readByte(data,offset + patternLength, offset + patternLength*2);
                }
                groups.add(group);
                lastByte = readByte;
                group = new DataGroup(baseOffset+offset, lastByte, 1);
                offset += patternLength;
                readByte = readByte(data, offset, offset + patternLength);
                futureByte = readByte(data, offset + patternLength, offset + patternLength*2);
            } else {
                while (!readByte.equals(lastByte) && !futureByte.equals(readByte)) {
                    group.append(readByte);
                    lastByte = readByte;
                    offset += patternLength;
                    readByte = readByte(data, offset, offset + patternLength);
                    futureByte = readByte(data, offset + patternLength, offset + patternLength*2);
                }
                groups.add(group);
                lastByte = readByte;
                group = new DataGroup(baseOffset+offset, lastByte, 1);
                offset += patternLength;
                readByte = readByte(data, offset, offset + patternLength);
                futureByte = readByte(data,offset + patternLength, offset + patternLength*2);
            }
        }
        if (data.length()%patternLength!=0 && !lastByte.isEmpty()) {
            group = new DataGroup(baseOffset+(offset-patternLength), lastByte, 1);
            groups.add(group);
        } else if (!lastByte.isEmpty()) {
            groups.add(group);
        }
        /*if (!readByte.isEmpty()) {
            group = new DataGroup(baseOffset+offset, lastByte, 1);
            groups.add(group);
        }*/
        return groups;
    }

    private String readByte(String data, int start, int end) {
        if (start>data.length()) return "";
        if (end>data.length()) return data.substring(start, data.length());
        else return data.substring(start, end);
    }

/*
    public byte[] comp() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        LZSS lzss = new LZSS(inputStream);
        ByteArrayOutputStream outputStream = lzss.compress();
        return outputStream.toByteArray();
    }
*/
    /*public void compress(String txtFilename) throws IOException {
        //byte[] encoded = Files.readAllBytes(Paths.get(txtFilename));
        //String data = new String(encoded, StandardCharsets.US_ASCII);
        int SEGMENT_LENGTH = 32;
        int ITEM_LENGTH = 2;
        int MIN_SEGMENT_LENGTH = 4;
        String data = "00000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC3FF99FF99FF99FF99";
        StringBuffer output = new StringBuffer();
        data = data.replaceAll(" ","");
        int offset = 0;

        while (offset<data.length()) {
            int windowSize = SEGMENT_LENGTH;
            boolean found = false;
            while (!found && windowSize>MIN_SEGMENT_LENGTH) {
                String substring = data.substring(offset, offset + windowSize);
                int lastIndexOf = output.lastIndexOf(substring);
                if (lastIndexOf>=0) {
                    found = true;
                    int position = offset - lastIndexOf;
                    int size = windowSize;
                    System.out.println("REPEAT size "+size+" pos "+position);
                    offset += size * ITEM_LENGTH;
                }
                else {
                    windowSize-= ITEM_LENGTH;
                }
            }
            if (!found) {

                String add = data.substring(offset, offset + ITEM_LENGTH);
                output.append(add);

                System.out.println("DATA "+ add);

                String next = data.substring(offset+ITEM_LENGTH, offset+ITEM_LENGTH*2);

                int size = 0;
                while (next.equals(add)) {
                    offset += ITEM_LENGTH;
                    size ++;
                    next = data.substring(offset+ITEM_LENGTH, offset+ITEM_LENGTH*2);
                }


                if (size>0) {
                    System.out.println("REPEAT size "+size+" pos 1");
                    for (int i=0;i<=size;i++) output.append(add);
                    offset += ITEM_LENGTH;
                }
            }

        }*/


    /*public void compress() {
        int index = 0;
        int size = 15;
        int[] decompressedOutput = new int[data.length];
        Arrays.fill(decompressedOutput,-1);
        int position = 0;
        while (index< data.length) {
            boolean found = false;
            int[] pattern = null;
            while (!found && size>2) {
                pattern = Arrays.copyOfRange(data, index, index + size);
                position = Utils.findArray(decompressedOutput, pattern);
                if (position>=0) {
                    found = true;
                } else {
                    size--;
                }
            }
            if (!found) {
                DataByte dataByte = new DataByte(data[index]);
                System.out.println("BUILDING DATA : "+Utils.toHexString(data[index],2));
                compressedBytes.add(dataByte);
                decompressedOutput[index++] = data[index];
                size = 15;
            }
            else {
                System.out.println("BUILDING REPEAT : size "+size+" position "+(index-position));
                index+=size;
            }
        }*/


}
