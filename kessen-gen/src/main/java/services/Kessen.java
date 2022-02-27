package services;

import characters.JapaneseChar;
import entities.Config;
import entities.Dictionnary;
import entities.PointerTable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Constants.*;
import static services.Utils.toHexString;

public class Kessen {


    static byte[] data;
    static Config config;

    public static boolean REVERSE_FLAG_BITS = false;
    public static boolean FLIP_FLAG_BITS = false;

    public static void main(String[] args) {

        LatinLoader latinLoader = new LatinLoader();
        Translator translator = new Translator(latinLoader);
        config = JsonLoader.loadConfig();
        latinLoader.loadLatin();

        System.out.println("Loading config");
        System.out.println("rom-input="+config.getRomInput());
        System.out.println("rom-output="+config.getRomOutput());
        System.out.println("bps-patch-output="+config.getBpsPatchOutput());

        try {
            data = Files.readAllBytes(new File(config.getRomInput()).toPath());
        } catch (IOException ex) {
            Logger.getLogger(Kessen.class.getName()).log(Level.SEVERE, null, ex);
        }
        data = DataWriter.fillDataWithPlaceHolders(data);

        DataWriter.writeCodePatches(JsonLoader.loadCodePatches(), data, false);

        List<JapaneseChar> japaneseChars = JsonLoader.loadJapanese();
        Dictionnary japanese = new Dictionnary(japaneseChars);

        //JsonLoader.generateJsonJapanese(config.getFileDicoJap());
        //JsonLoader.generateJsonLatin(config.getFileDicoLatin());

        //lookingForTables(data, JsonLoader.loadJapanese());
        /*new TablePrinter().generateMenuTable(data, japanese);
        if (true) return;*/

        List<PointerTable> tables = JsonLoader.loadTables();
        for (PointerTable table:tables) {
            if (table.getId()==1) {
                //new TablePrinter().generateTranslationFile(table, data, japanese);
                //new TablePrinter().generateMenuTable(data, japanese);
            }
        }

        for (String s:JsonLoader.loadTranslationFiles()) {
            try {
                translator.loadTranslationFile(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        translator.setSpecialCharMap(JsonLoader.loadSpecialChars());

        for (PointerTable table:tables) {
            DataReader.readTable(table, data);
        }
        for (PointerTable table:tables) {
            DataReader.generateEnglish(translator, table, data);
        }

        for (PointerTable table:tables) {
            DataWriter.writeEnglish(table, data);
        }

        /*for (InputPatch ip:JsonLoader.loadInputPatches()) {
            ip.generateCode(latinLoader.getLatinChars());
            if (!ip.isDebug()) ip.writePatch(data);
        }*/

        System.out.println("Saving rom-output...");
        DataWriter.saveData(config.getRomOutput(), data);
        //System.out.println("Saving bps-patch-output...");
        //Patcher.generatePatch(new File(config.getRomInput()), new File(config.getRomOutput()), new File(config.getBpsPatchOutput()), "https://github.com/Krokodyl/dokapon-english");
        System.out.println("Process complete");

        /*
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get("../font/uncompressed-latin-bytes.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = new String(encoded, StandardCharsets.US_ASCII);

        data = testFontImageReader();

        new TreeBuilder().test(data);
        */


    }



    public static void lookingForTables(byte[] data, List<JapaneseChar> japaneseChars) {
        Map<String, JapaneseChar> map = new HashMap<>();
        for (JapaneseChar jc:japaneseChars) map.put(jc.getCode().toUpperCase(), jc);
        List<String> candidates = new ArrayList<>();
        int offset = 0;
        int maxCount = 0;
        int count = 0;
        boolean modeF1 = false;
        String prefix = "";
        String line = toHexString(offset,2)+" ";
        for (byte b:data) {
            if (offset%Integer.parseInt("100",16)==0) System.out.println("\noffset "+ toHexString(offset,2));

            if (b == (byte)(Integer.parseInt("f1",16) & 0xFF)) {
                modeF1 = true;
                prefix = "f1";
            }
            else if (b == (byte)(Integer.parseInt("f0",16) & 0xFF)) {
                modeF1 = false;
                prefix = "";
            } else {
                JapaneseChar japaneseChar = map.get(prefix.toUpperCase()+ toHexString(b));
                if (japaneseChar==null) {
                    if (count>0 && count>maxCount) {
                        maxCount = count;
                        count = 0;
                    }
                    System.out.print("?");
                    line+="?";
                }
                else {
                    count++;
                    System.out.print(japaneseChar.getValue());
                    line+=japaneseChar.getValue();
                }
                if (b == (byte)Integer.parseInt("FF",16)) {
                    System.out.println();
                    if (maxCount>20) {
                        candidates.add(line);
                    }
                    System.out.println(maxCount);
                    System.out.println();
                    count = 0;
                    maxCount = 0;
                    line = toHexString(offset,2)+" ";
                }
            }


            offset++;
        }
        for (String s:candidates) {
            System.out.println(s);
        }
    }

    private static String testFontImageReader() {
        FontImageReader fontImageReader = new FontImageReader();
        return fontImageReader.loadFontImage("../font/vram-latin.png");
    }



    private static void testCompressor() {
        Compressor compressor = new Compressor();
        try {
            compressor.analyzeData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testDecompressor() {
        Decompressor decompressor = new Decompressor(data, Integer.parseInt("58003",16), Integer.parseInt("58003",16)+Integer.parseInt("1147",16));

        String lzssTest = "DF 22 33 88 88 88 00 02 88 88 F7 66 66 66 00 02 55 55 55 55 FD 55 A0 12 44 44 44 44 11".replaceAll(" ","");
        String lz11Test = "0E 22 AA AA AA 20 02 50 05 30 03 55 3C 55 55 20 02 50 05 30 03 F0 18 33".replaceAll(" ","");
        String lzxEvb = "09 22 33 88 88 50 01 66 66 30 01 30 55 55 20 01 C0 12 44 44 44 44 00 11";
        String lzxEvl = "F7 22 33 88 88 26 00 66 66 24 00 CE 55 55 23 00 3D 01 44 44 22 00 11 80 00 00";
        String lzxEwb = "15 22 33 88 60 00 66 40 00 55 30 00 A0 C0 12 44 20 00 11";
        String lzxEwl = "EB 22 33 88 17 00 66 15 00 55 14 00 58 3D 01 44 13 00 11 00 00";


        // LZX EVB  true true
        // LZX EVL  true false
        String compressedTest = lzxEwb;
        Kessen.REVERSE_FLAG_BITS = true;
        Kessen.FLIP_FLAG_BITS = true;


        compressedTest = compressedTest.replaceAll(" ","");
        byte[] s = Utils.hexStringToByteArray(compressedTest);
        decompressor = new Decompressor(s, 0, s.length);
        System.out.println("TEST");
        decompressor.decomp();
        decompressor.printSingleLineOutput();
    }

    public void bordel() {
        Decompressor decompressor = new Decompressor(data, Integer.parseInt("58003",16), Integer.parseInt("58003",16)+Integer.parseInt("1147",16));

        Compressor compressor = new Compressor();
        try {
            compressor.analyzeData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes1 = null;
        try {
            byte[] uncompressedData = Files.readAllBytes(new File("../font/uncompressed-latin-bytes.bin").toPath());

            ByteArrayOutputStream compressed = null;

            bytes1 = compressed.toByteArray();

            System.out.println(Utils.bytesToHex(bytes1));
            System.out.println("count="+bytes1.length);
        } catch (IOException e) {
            e.printStackTrace();
        }




        //byte[] data = Utils.hexStringToByteArray("00000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEDFFFFDEFFDEFFFFFFFFFFFFFBFFFFFBFFFBFF");
        try {
            //byte[] uncompressedData = Files.readAllBytes(new File("../font/uncompressed-latin-bytes.bin").toPath());
            byte[] uncompressedData = Files.readAllBytes(new File("../font/bytes.sfc").toPath());
            //System.out.println(Utils.bytesToHex(uncompressedData));
            /*Compressor compressor = new Compressor(uncompressedData);
            byte[] comp = compressor.comp();
            String s = Utils.bytesToHex(comp);
            System.out.println(s);*/

            /*LZSS compressor = new LZSS(new ByteArrayInputStream(uncompressedData));
            ByteArrayOutputStream compressed = compressor.compress();
            byte[] bytes1 = compressed.toByteArray();
            System.out.println(Utils.bytesToHex(bytes1));

            LZSS lzss = new LZSS(new ByteArrayInputStream(bytes1));
            ByteArrayOutputStream decompressedStream = lzss.uncompress();
            byte[] byteArray = decompressedStream.toByteArray();
            System.out.println(Utils.bytesToHex(byteArray));*/


            /*decompressor = new Decompressor(bytes1, Integer.parseInt("0",16), bytes1.length);
            decompressor.decomp();*/

            /*byte[] bytes = Utils.hexStringToByteArray("ff49276d20626c7565ff2064612062612064bd65f5f661610a44f8ff2c40f6ff1c0f050f2c0f520f3b0b0aeeff003e0f650fa90fbb0fa40fcb0ff10fda06");
            LZSS lzss = new LZSS(new ByteArrayInputStream(bytes));
            ByteArrayOutputStream decompressedStream = lzss.uncompress();
            byte[] byteArray = decompressedStream.toByteArray();
            System.out.println();
            System.out.println(new String(byteArray));

            decompressor = new Decompressor(bytes, Integer.parseInt("0",16), bytes.length);
            decompressor.decomp();*/

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
