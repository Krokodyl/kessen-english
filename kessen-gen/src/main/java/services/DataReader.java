package services;


import entities.PointerData;
import entities.PointerRange;
import entities.PointerTable;
import entities.Translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static services.Constants.*;
import static services.Constants.TRANSLATION_KEY_ENG;
import static services.Utils.bytesToHex;
import static services.Utils.toHexString;

public class DataReader {

    public static boolean verbose = true;

    public static PointerTable readTableFromTranslationFile(PointerTable table, String name) throws IOException {
        System.out.println("Reading table from translation File : "+name);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(name)), StandardCharsets.UTF_8));
        PointerData p = new PointerData();
        String line = br.readLine();
        while (line != null) {
            if (line.contains(Constants.TRANSLATION_KEY_VALUE_SEPARATOR)) {
                String[] split = line.split(Constants.TRANSLATION_KEY_VALUE_SEPARATOR);
                if (split.length>0) {
                    if (split[0].equals(Constants.TRANSLATION_KEY_OFFSET)) {
                        p.setOffset(Integer.parseInt(split[1], 16));
                    }
                    if (split[0].equals(Constants.TRANSLATION_KEY_OFFSETDATA)) {
                        p.setOffsetData(Integer.parseInt(split[1], 16));
                    }
                    if (split[0].equals(Constants.TRANSLATION_KEY_MENUDATA)) {
                        if (split.length>1) p.setMenuData(split[1].split(" "));
                    }
                    if (split[0].equals(Constants.TRANSLATION_KEY_VALUE)) {
                        p.setValue(Integer.parseInt(split[1], 16));
                    }
                    if (split[0].equals(Constants.TRANSLATION_KEY_DATA)) {
                        if (split.length>1) p.setData(split[1].split(" "));
                    }
                }
            } else {
                if (p.getData().length>0) {
                    table.addPointerDataJap(p);
                    p = new PointerData();
                }
            }
            line = br.readLine();
        }
        return table;
    }

    public static PointerTable readTable(PointerTable table, byte[] data) {
        for (PointerRange range : table.getRanges()) {
            if (table.isMenu()) {
                int i = range.getStart();
                while (i <= range.getEnd()) {
                    byte cursor = data[i];
                    if (cursor == 2 && data[i+5]==0) {
                        PointerData p = new PointerData();
                        int a = (data[i+6] & 0xFF);
                        int b = (data[i+7] & 0xFF);
                        int value = b * 256 + a;
                        int offsetData = value + range.getShift();
                        byte[] bytes = readUntilEndOfLine(data, offsetData);
                        if (bytes[0]==MODE_F0_BYTE || bytes[0]==MODE_F1_BYTE) {
                            p.setValue(value);
                            p.setOffset(i);
                            p.setOffsetData(offsetData);
                            //String[] readData = readPointerData(value, data);
                            p.setData(bytesToHex(bytes).trim().split(" "));
                            String[] menuData = new String[6];
                            menuData[0] = toHexString(data[i]);
                            menuData[1] = toHexString(data[i+1]);
                            menuData[2] = toHexString(data[i+2]);
                            menuData[3] = toHexString(data[i+3]);
                            menuData[4] = toHexString(data[i+4]);
                            menuData[5] = toHexString(data[i+5]);
                            p.setMenuData(menuData);
                            table.addPointerDataJap(p);
                        }
                    }
                    i++;
                }
            } else {
                for (int i = range.getStart(); i <= range.getEnd(); i = i + 2) {
                    PointerData p = new PointerData();
                    int a = (data[i] & 0xFF);
                    int b = (data[i + 1] & 0xFF);
                    int c = b * 256 + a;
                    p.setValue(c);
                    p.setOffset(i);
                    String[] readData = readPointerData(c + range.getShift(), data);
                    p.setData(readData);
                    p.setOffsetData(c + range.getShift());
                    table.addPointerDataJap(p);
                }
            }
        }
        return table;
    }

    public static String[] readPointerData(int offset, byte[] data) {
        boolean end = false;
        List<String> res = new ArrayList<String>();
        int i = offset;
        while (!end) {
            int a = (data[i] & 0xFF);
            String s = Utils.toHexString(a);
            if (s.equals(END_OF_LINE_CHARACTER_HEXA)) {
                end = true;
            }
            res.add(s);
            i = i + 1;
        }
        return res.toArray(new String[0]);
    }

    public static byte[] readUntilEndOfLine(byte[] data, int start) {
        int count = 1;
        int offset = start;
        while (data[offset++]!=END_OF_LINE_CHARACTER_BYTE) {
            count++;
        }
        byte[] read = new byte[count];
        offset = start;
        int k = 0;
        while (k<read.length) {
            read[k++] = data[offset++];
        }
        return read;
    }

    public static PointerTable generateEnglish(Translator translator, PointerTable table, byte[] data) {
        System.out.println("Generate English for table "+table.getId());
        Map<Integer, Integer> mapValues = new HashMap<Integer, Integer>();

        int tableDataLength = 0;

        int newDataStart = table.getNewDataStart();
        int newDataShift = table.getNewDataShift();

        if (table.getNewDataStart() == 0) return table;
        for (PointerData p : table.getDataJap()) {

            int offset = p.getOffset();
            int offsetData = p.getOffsetData();

            PointerData newP = new PointerData();
            newP.setOldPointer(p);
            String[] translation = translator.getTranslation(p, table.isEvenLength());
            String[] menuData = translator.getMenuData(p);
            if (translation != null && translation.length > 0) {
                newP.setData(translation);
            } else {
                newP.setData(p.getData());
            }
            if (menuData != null) {
                newP.setMenuData(menuData);
            } else {
                newP.setMenuData(p.getMenuData());
            }
            newP.setOffset(offset);
            newP.setOffsetData(newDataStart);
            newP.setOffsetOldMenuData(offsetData);
            int oldValue = p.getValue();
            if (!mapValues.containsKey(oldValue)) {
                int value = newDataStart - newDataShift;
                newP.setValue(value);
                mapValues.put(oldValue, value);
                if (newP.getData()==null) {
                    System.out.println();
                }
                double l = newP.getData().length;
                tableDataLength += l;
                int longueur = (int) l;
                /*if (table.getId() == 4) {
                    longueur = (int) ((Math.ceil(l / 8)) * 16);
                    if (longueur % 32 == 16) longueur += 16;
                }*/
                newDataStart += longueur;
                /*if (table.getId() == 5) {
                    if (offsetData % 16 == 0) offsetData += 2;
                }*/
            } else {
                newP.setValue(mapValues.get(oldValue));
            }
            table.addPointerDataEng(newP);
            if (verbose) System.out.println(newP);
        }
        /*System.out.println("TABLE "+table.getId());
        System.out.println("TABLE DATA LENGTH "+tableDataLength);
        for (Map.Entry<Integer, Integer> e : mapValues.entrySet()) {
            System.out.println(Integer.toHexString(e.getKey())+" -> "+Integer.toHexString(e.getValue()));
        }
        System.out.println("EVEN="+mapLegnths.get("EVEN"));
        System.out.println("ODD="+mapLegnths.get("ODD"));*/
        return table;
    }
}
