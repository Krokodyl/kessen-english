package services;


import entities.PointerData;
import entities.PointerRange;
import entities.PointerTable;
import entities.Translation;
import enums.PointerOption;
import enums.PointerRangeType;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static services.Constants.*;
import static services.Constants.TRANSLATION_KEY_ENG;
import static services.Utils.bytesToHex;
import static services.Utils.toHexString;

public class DataReader {

    public static boolean verbose = false;

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
    
    public static void generateCredits(String name) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(name)), StandardCharsets.UTF_8));
        String line = br.readLine();
        System.out.printf("{F0}");
        int alt = 1;
        while (line != null) {
            line = line.trim();
            int count = line.length() - line.replace("{", "").length();
            int length = line.length() - count*4;
            int gap = 30-length;
            int left = gap/2;
            int right = gap-left;
            String suffix = "{NL}";
            if (alt==3) {
                int shift = 29-length-left;
                suffix = "{BF "+Utils.toHexString(shift)+" FE}";
                alt=0;
            }
            System.out.printf("%s",StringUtils.repeat(" ", left)+line+suffix);
            line = br.readLine();
            alt++;
        }
        System.out.printf("{EL}\n");
    }

    public static void generateReferenceFile(PointerTable table, String name, String output) throws IOException {
        PrintWriter writer = new PrintWriter(output, "UTF-8");
        System.out.println("Reading table from translation File : "+name);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(name)), StandardCharsets.UTF_8));
        PointerData p = new PointerData();
        String line = br.readLine();
        String jpn = "";
        String eng = "";
        while (line != null) {
            if (line.contains(Constants.TRANSLATION_KEY_VALUE_SEPARATOR)) {
                String[] split = line.split(Constants.TRANSLATION_KEY_VALUE_SEPARATOR);
                if (split.length>0) {
                    if (split[0].equals(Constants.TRANSLATION_KEY_OFFSET)) {
                        p.setOffset(Integer.parseInt(split[1], 16));
                    }
                    if (split[0].equals(TRANSLATION_KEY_JPN)) {
                        if (split.length>1) jpn = split[1];
                    }
                    if (split[0].equals(TRANSLATION_KEY_ENG)) {
                        if (split.length>1) eng = split[1];
                    }
                    if (!eng.isEmpty() && !jpn.isEmpty()) {
                        String[] splitJpn = jpn.split("(?<=\\G.{8})");
                        String[] splitEng = eng.split("(?<=\\G.{8})");
                        if (p.getOffset()>Integer.parseInt("BFA7",16))
                        for (int k=0;k<splitJpn.length;k++) {
                            writer.println(splitEng[k]+"="+splitJpn[k]);
                        }
                        eng="";
                        jpn="";
                    }
                }
            }
            line = br.readLine();
        }
        writer.close();
    }
    
    public static void generateDualLetters(String name) throws IOException {
        System.out.println("generateDualLetters from File : "+name);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(name)), StandardCharsets.UTF_8));
        PointerData p = new PointerData();
        String line = br.readLine();
        String jpn = "";
        List<String> chars = new ArrayList<>();
        List<Integer> skip = new ArrayList<>();
        skip.add(Integer.parseInt("AC",16));
        skip.add(Integer.parseInt("BE",16));
        skip.add(Integer.parseInt("BF",16));
        skip.add(Integer.parseInt("C0",16));
        skip.add(Integer.parseInt("C1",16));
        skip.add(Integer.parseInt("C2",16));
        skip.add(Integer.parseInt("C3",16));
        skip.add(Integer.parseInt("C4",16));
        skip.add(Integer.parseInt("C5",16));
        skip.add(Integer.parseInt("C6",16));
        skip.add(Integer.parseInt("C7",16));
        skip.add(Integer.parseInt("CA",16));
        skip.add(Integer.parseInt("CB",16));
        skip.add(Integer.parseInt("F8",16));
        skip.add(Integer.parseInt("F9",16));
        skip.add(Integer.parseInt("FA",16));
        skip.add(Integer.parseInt("FB",16));
        skip.add(Integer.parseInt("FE",16));
        skip.add(Integer.parseInt("FF",16));
        Map<String, Integer> mapPairValue = new LinkedHashMap<>();
        Map<String, String> names = new HashMap<>();
        int value = Integer.parseInt("90",16);
        while (line != null) {
            if (!line.isEmpty()) {
                //line = line.toUpperCase();
                String eng = "";
                String[] split = line.split("(?<=\\G.{7})");
                String left = split[0];
                String right = split[1];
                for (int k=0;k<left.length();k++) {
                    String s = left.charAt(k)+""+right.charAt(k)+"";
                    if (s.trim().isEmpty()) eng+=" ";
                    else eng += "{DL-"+s+"}";
                    if (!s.trim().isEmpty() && mapPairValue.get(s)==null) {
                        mapPairValue.put(s, value++);
                    }
                    while (skip.contains(value)) value++;
                    //if (!chars.contains(s)) chars.add(s); 
                }
                System.out.println(line+"\t"+eng);
            }
            line = br.readLine();
        }
        for (Map.Entry<String, Integer> e : mapPairValue.entrySet()) {
            //System.out.println(e.getKey()+"\t"+Utils.toHexString(e.getValue()));
            String json = String.format(",\n" +
                    "    {\n" +
                    "      \"value\":\"%s\",\n" +
                    "      \"code\":\"%s\"\n" +
                    "    }", "{DL-"+e.getKey()+"}", Utils.toHexString(e.getValue()));
            System.out.print(json);
        }


        System.out.println("Double chars : "+chars.size());
    }

    private static List<PointerData> readValues(PointerRange range, byte[] data) {
        List<PointerData> pointers = new ArrayList<>();
        if (range.getType()== PointerRangeType.COUNTER) {
            int i = range.getStart();
            while (i <= range.getEnd()) {
                byte counter = data[i++];
                while (counter-->0) {
                    PointerData p = new PointerData();
                    int offset = i;
                    int a = (data[i++] & 0xFF);
                    int b = (data[i++] & 0xFF);
                    int value = b * 256 + a;
                    p.setValue(value);
                    p.setOffset(offset);
                    int offsetData = value + range.getShift();
                    p.setOffsetData(offsetData);
                    pointers.add(p);
                }
            }
        }
        else if (range.getType()==PointerRangeType.MENU) {
            int i = range.getStart();
            while (i <= range.getEnd()) {
                byte cursor = data[i];
                if (cursor == 2 && data[i+5]==0) {
                    PointerData p = new PointerData();
                    int a = (data[i+6] & 0xFF);
                    int b = (data[i+7] & 0xFF);
                    int value = b * 256 + a;
                    p.setValue(value);
                    p.setOffset(i+6);
                    p.setOffsetMenuData(i);
                    int offsetData = value + range.getShift();
                    byte[] bytes = readUntilEndOfLine(data, offsetData);
                    
                        p.setOffsetData(offsetData);
                        //String[] readData = readPointerData(value, data);
                        p.setData(bytesToHex(bytes).trim().split(" "));
                        pointers.add(p);
                    
                    i=i+8;
                } else
                i++;
            }
        }
        else {
            int i = range.getStart();
            while (i <= range.getEnd()) {
                PointerData p = new PointerData();
                int offset = i;
                int a = (data[i++] & 0xFF);
                int b = (data[i++] & 0xFF);
                int value = b * 256 + a;
                p.setValue(value);
                p.setOffset(offset);
                int offsetData = value + range.getShift();
                p.setOffsetData(offsetData);
                pointers.add(p);
            }
        }
        return pointers;
    }

    public static void readData(PointerRange range, List<PointerData> pointers, byte[] data) {
        for (PointerData p : pointers) {
            int offsetData = p.getOffsetData();
            int offsetMenuData = p.getOffsetMenuData();

            /*int offsetData = p.getValue() + range.getShift();
            p.setOffsetData(offsetData);*/
            byte[] bytes;

            if (range.getShift()==0) {
                bytes = readUntil(data, offsetData, END_OF_LINE_6C_CHARACTER_BYTE);
            } else bytes = readUntilEndOfLine(data, offsetData);
            p.setData(bytesToHex(bytes).trim().split(" "));
            if (range.getType()==PointerRangeType.MENU) {
                String[] menuData = new String[6];
                menuData[0] = toHexString(data[offsetMenuData]);
                menuData[1] = toHexString(data[offsetMenuData+1]);
                menuData[2] = toHexString(data[offsetMenuData+2]);
                menuData[3] = toHexString(data[offsetMenuData+3]);
                menuData[4] = toHexString(data[offsetMenuData+4]);
                menuData[5] = toHexString(data[offsetMenuData+5]);
                p.setMenuData(menuData);
            }
        }

    }

    public static void readDataFixedLength(PointerRange range, List<PointerData> pointers, byte[] data) {
        for (int k=0;k<pointers.size();k++) {

            PointerData p = pointers.get(k);
            int offsetEnd = 0;
            if (k<pointers.size()-1) offsetEnd = pointers.get(k+1).getOffsetData();
            
            int offsetData = p.getOffsetData();

            byte[] bytes = null;

            if (offsetEnd>0) {
                bytes = readUntilOffset(data, offsetData, offsetEnd);
            } else {
                bytes = readUntil(data, offsetData, END_OF_LINE_00_CHARACTER_BYTE);
            }
            p.setData(bytesToHex(bytes).trim().split(" "));
            
        }

    }
    
    public static PointerTable readTable(PointerTable table, byte[] data) {
        for (PointerRange range : table.getRanges()) {
            
            /*
            Read values
             */
            List<PointerData> pointers;
            List<PointerData> pointerData = readValues(range, data);
            
            /*
            Read data
             */
            if (range.getType()==PointerRangeType.FIXED_LENGTH) readDataFixedLength(range, pointerData, data);
            else readData(range, pointerData, data);
            
            /*
            Add pointers
             */
            for (PointerData pointerDatum : pointerData) {
                if (
                        (
                       pointerDatum.getData()[0].equals("F0") ||
                               pointerDatum.getData()[0].equals("F1")||
                               pointerDatum.getData()[0].equals("FE")
                        ) || range.getShift()==0 || range.getType()==PointerRangeType.FIXED_LENGTH) {
                    table.addPointerDataJap(pointerDatum);
                    if (verbose) System.out.println(pointerDatum);
                }
                
            }

            
            
            
             /*else {
                if (!table.isStopAtNextPointer()) {
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
                } else {
                    int previousOffset = 0;
                    for (int i = range.getEnd()+1; i >= range.getStart(); i = i - 2) {
                        PointerData p = new PointerData();
                        int a = (data[i] & 0xFF);
                        int b = (data[i + 1] & 0xFF);
                        int c = b * 256 + a;
                        p.setValue(c);
                        p.setOffset(i);
                        String[] readData = null;
                        if (previousOffset==0) {
                            readData = readPointerData0(c + range.getShift(), data);
                        } else {
                            readData = readPointerData(c + range.getShift(), data, previousOffset);
                        }
                        p.setData(readData);
                        p.setOffsetData(c + range.getShift());
                        table.addPointerDataJap(p);
                        previousOffset = i;
                    }
                }

            }*/
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

    public static String[] readPointerData0(int offset, byte[] data) {
        boolean end = false;
        List<String> res = new ArrayList<String>();
        int i = offset;
        while (!end) {
            int a = (data[i] & 0xFF);
            String s = Utils.toHexString(a);
            if (s.equals(END_OF_LINE_00_CHARACTER_BYTE)) {
                end = true;
            }
            res.add(s);
            i = i + 1;
        }
        return res.toArray(new String[0]);
    }

    public static String[] readPointerData(int offset, byte[] data, int stopOffset) {
        boolean end = false;
        List<String> res = new ArrayList<String>();
        int i = offset;
        while (!end) {
            int a = (data[i] & 0xFF);
            String s = Utils.toHexString(a);
            if (i >= stopOffset) {
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

    public static byte[] readUntil(byte[] data, int start, byte b) {
        int count = 1;
        int offset = start;
        while (data[offset++]!=b) {
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

    public static byte[] readUntilOffset(byte[] data, int start, int end) {
        int offset = start;
        byte[] read = new byte[end-start];
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
            
            String[] translation = translator.getTranslation(p, table.isEvenLength(), false);
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
            if (!table.getKeepOldPointerValues()) {
                newP.setOffsetData(newDataStart);
            } else newP.setOffsetData(offsetData);
            newP.setOffsetMenuData(p.getOffsetMenuData());
            int oldValue = p.getValue();
            if (!mapValues.containsKey(oldValue)) {
                int value = newDataStart - newDataShift + Integer.parseInt("8000",16);
                if (table.getKeepOldPointerValues()) {
                    value = oldValue;
                }
                newP.setValue(value);
                mapValues.put(oldValue, value);
                if (newP.getData()==null) {
                    System.out.println();
                }
                double l = newP.getData().length;
                tableDataLength += l;
                int longueur = (int) l;
                newDataStart += longueur;
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

    public static void generateTownPairs() {
        System.out.println();
        String s = "HokaMoriWateGimiTakiSataKufuBaraChitMagoSamiBageToshNaraManaNonaNikaFuzaWakaKuuiFuzyWittArchEmmySigaKiotLanaOhanHyotAkanLithNemaYamaImanGuciTokuDawaHemeUkohCuoaGasaNagiMamoOitaMizaGosiRyu WihaSapiAgum";
        String[] split = s.toUpperCase().split("(?<=\\G.{2})");
        Set<String> set = new HashSet<>(Arrays.asList(split));
        System.out.println("Pair count : "+set.size());

        int z = 7001;
        String jpnc = "ホッカ アーモリワイテ ギーミ タキーアサンタガクフマシバラギンチット マグーンサマイタバッチ トーシュナガワ マナシ ノーナンニーガ フザン ワカーシクーイ フギー ジョーカアーチ エミー シガッツキオットラナーンオーハンヒョー ワッカンリット ネマシーオーヤマロシマンマグッチトクシ ガワーカヒーメイウコッチクオカ ガサールナガキンマモットオイッタミザッキゴッシムリュウ ワイハーサパインアグム ";

        Map<String,String> katakanas = new HashMap<String, String>() {{
            
            put("ッ", "");
            put("ー", "");
            put("ン", "N");//N
            put("シ", "SI");
            put("イ", "NI");
            put("ガ", "KA");
            put("ワ", "WA");
            put("カ", "KA");
            put("ア", "NA");
            put("マ", "MA");
            put("ト", "TO");
            
            // 5
            put("ナ", "NA");
            put("オ", "NO");
            put("チ", "KI");
            
            // 4
            put("キ", "KI");
            put("ク", "KU");
            put("サ", "SA");
            put("タ", "TA");
            
            // 3
            put("ギ", "GI");
            put("グ", "GU");
            put("リ", "RI");
            put("フ", "FU");
            put("ミ", "MI");
            
            // 2
            put("ウ", "NU");
            put("ザ", "ZA");
            put("ハ", "HA");
            put("バ", "BA");
            put("ヒ", "HI");
            put("ュ", "YU");
            put("ョ", "YO");
            put("ラ", "RA");
            put("モ", "MO");
            put("ム", "MU");
            
            // 1
            put("エ", "TE");
            put("コ", "KO");
            put("ゴ", "GO");
            put("ジ", "NI");
            put("ツ", "SU");
            put("テ", "TE");
            put("ニ", "NI");
            put("ネ", "NE");
            put("ノ", "NO");
            put("ホ", "HO");
            put("パ", "PA");
            put("メ", "ME");
            put("ヤ", "YA");
            put("ル", "RU");
            put("ロ", "RO");
        }};
        


        String cities = "HokaMoriWateGimiTakiSataKufuBaraChitMagoSamiBageToshNaraManaNonaNikaFuzaWakaKuuiFuzyWittArchEmmySigaKiotLanaOhanHyotAkanLithNemaYamaImanGuciTokuDawaHemeUkohCuoaGasaNagiMamoOitaMizaGosiRyu WihaSapiAgum";
        split = jpnc.split("(?<=\\G.{4})");
        //String[] split1 = cities.split("(?<=\\G.{4})");
        s = "";
        Set<String> pairs = new HashSet<>();
        
        for (int k=0;k<split.length;k++) {
            String name = split[k];
            for (Map.Entry<String, String> e : katakanas.entrySet()) {
                if (!e.getValue().isEmpty() && name.contains(e.getKey())) {
                    pairs.add(e.getValue());
                }
                name = name.replace(e.getKey(),e.getValue());
            }
            s+=z;
            System.out.printf("%s=%s%n", z++, name);
        }
        System.out.println(s);

        System.out.println("Pair count : "+pairs.size());

        for (String pair : pairs) {
            System.out.print(pair+"_");
        }
        System.out.println();


        Map<Character, Integer> counts = new HashMap<>();
        for (char c : jpnc.toCharArray()) {
            if (counts.containsKey(c)) {
                counts.put(c, counts.get(c)+1);
            } else counts.put(c,1);
        }
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue().toString());
        }
        
        
        

    }

    public static void analyzeTownNames(String name) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(name)), StandardCharsets.UTF_8));
        String line = br.readLine();
        Set<String> pairs = new HashSet<>();
        int z = 5000042;
        while (line != null) {
            for (String s : line.split("(?<=\\G.{2})")) {
                pairs.add(s.toUpperCase());
            }
            System.out.println((z++)+"=  "+StringUtils.capitalize(line)+" ");
            line = br.readLine();
        }
        System.out.println(pairs.size());
        for (String pair : pairs) {
            System.out.println(pair);
        }

    }
}
