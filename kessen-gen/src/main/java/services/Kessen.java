package services;

import characters.JapaneseChar;
import entities.*;
import enums.PaletteText;
import services.sprites.FontImageReader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Utils.toHexString;

public class Kessen {


    static byte[] data;
    static Config config;

    public static boolean REVERSE_FLAG_BITS = false;
    public static boolean FLIP_FLAG_BITS = false;

    public static void main(String[] args) throws IOException {

        LatinLoader latinLoader = new LatinLoader();
        Translator translator = new Translator(latinLoader);
        config = JsonLoader.loadConfig();
        latinLoader.loadLatin();

        System.out.println("Loading config");
        System.out.println("rom-input="+config.getRomInput());
        System.out.println("rom-output="+config.getRomOutput());
        System.out.println("bps-patch-output="+config.getBpsPatchOutput());
        
        // Call this once whenever the vram-latin.png image is modified.
        //new FontImageReader().generateSpriteLatinCharacters();

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
            System.out.println(String.format("---------------- Table %s ---------------------",table.getId()));
            /*if (table.isMenu()) {
                new TablePrinter().generateMenuTable(data, japanese);
            } else {
                new TablePrinter().generateTranslationFile(table, data, japanese);
            }*/
            /*if (table.getId()==3) {
                DataReader.generateReferenceFile(table, "translations/backup/Table 3.txt", "src/main/resources/tables/references.txt");
            }*/
            System.out.println("--------------------------------------");
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
            //else DataReader.readTableFromTranslationFile(table,"translations/Table 3.txt");
        }

        for (PointerTable table:tables) {
            new TablePrinter().generateTranslationFile2(table, data, japanese, "src/main/resources/gen/Table "+table.getId()+".txt");
        }
        
        
        for (PointerTable table:tables) {
            DataReader.generateEnglish(translator, table, data);
        }
        
        for (PointerTable table:tables) {
            DataWriter.writeEnglish(table, data);
        }

        for (InputPatch ip:JsonLoader.loadInputPatches()) {
            if (!ip.isDebug()) {
                ip.generateCode(latinLoader.getLatinChars());
                ip.writePatch(data);
            }
        }

        /*for (InputPatch ip:JsonLoader.loadInputPatches()) {
            ip.generateCode(latinLoader.getLatinChars());
            if (!ip.isDebug()) ip.writePatch(data);
        }*/

        String jpn = "モンスターに支配されてしま";
        System.out.println("JPN="+jpn);
        System.out.print("CODE=");
        for (char c : jpn.toCharArray()) {
            String japaneseCharFromJapanese = japanese.getJapaneseCharFromJapanese(c + "");
            System.out.print(japaneseCharFromJapanese+" ");
        }
        System.out.println();

        String eng = "itMP";
        System.out.println("ENG="+eng);
        System.out.print("CODE="+translator.getCodesFromEnglish(eng));
        System.out.println();

        String prefix = "8";
        for (int k=1;k<=26+1;k++) {
            String s = Utils.padLeft(Integer.toString(k), '0', 6);
            System.out.print(prefix+s+" ");
        }
        System.out.println();

        DataReader.generateDualLetters("tables/dual-words.txt");

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

    private static String testFontImageReader() throws IOException {
        FontImageReader fontImageReader = new FontImageReader();
        String image = "src/main/resources/images/vram-latin.png";
        String output = "src/main/resources/data/sprite-uncompressed.data";
        //image = "../font/vram-latin.png";

        String s = fontImageReader.loadFontImage2bpp(image, new PaletteText());
        byte[] bytes = fontImageReader.getBytes();

        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(bytes);
            fos.close();
            //There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }

        return s;
    }

}
