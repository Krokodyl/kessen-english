package services;


import characters.*;
import entities.*;
import enums.CharSide;
import enums.CharType;
import enums.PointerOption;
import enums.PointerRangeType;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static services.Utils.bytesToHex;

public class JsonLoader {

    static String file = "config.json";

    private static String loadJson() {
        InputStream is =
                JsonLoader.class.getClassLoader().getResourceAsStream(file);
        String jsonTxt = null;
        try {
            jsonTxt = IOUtils.toString( is );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonTxt;
    }

    public static List<String> loadTranslationFiles() {

        List<String> files = new ArrayList<>();
        JSONObject json = new JSONObject(loadJson());

        JSONArray c = json.getJSONArray("translations-files");
        for (Object o : c) {
            String next = (String) o;
            files.add(next);
        }

        return files;
    }

    public static Config loadConfig() {

        Config config = new Config();

        JSONObject json = new JSONObject(loadJson());

        JSONObject c = json.getJSONObject("config");
        config.setRomInput(c.getString("rom-input"));
        config.setRomOutput(c.getString("rom-output"));
        config.setBpsPatchOutput(c.getString("bps-patch-output"));
        config.setFileDicoJap(c.getString("file.jap"));
        config.setFileDicoLatin(c.getString("file.latin"));
        config.setFileDicoNames(c.getString("file.names"));

        return config;
    }

    public static Map<String, SpecialChar> loadSpecialChars() {
        Map<String, SpecialChar> specialCharMap = new HashMap<>();

        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("specials");

        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            String code = next.getString("code");
            int inGameLength = next.getInt("in-game-length");
            int dataLength = next.getInt("data-length");
            SpecialChar c = new SpecialChar(code);
            c.setInGameLength(inGameLength);
            c.setDataLength(dataLength);
            specialCharMap.put(c.getCode(), c);
        }
        return specialCharMap;
    }

    public static List<LatinChar> loadLatin() {
        List<LatinChar> latinChars = new ArrayList<>();

        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("latin");
        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            LatinChar c = new LatinChar();
            String value = next.getString("value");
            if (next.has("code")) {
                c.setCode(next.getString("code"));
            }
            c.setValue(value);
            if (next.has("sprite")) {
                JSONObject sprite = next.getJSONObject("sprite");
                if (sprite.has("image")) {
                    c.setSprite(new LatinSprite(sprite.getString("image")));
                } else {
                    c.setSprite(new LatinSprite(sprite.getString("image-top"), sprite.getString("image-bot")));
                }
            }
            if (next.has("location")) {
                JSONObject location = next.getJSONObject("location");
                c.setSpriteLocation(new SpriteLocation(Integer.parseInt(location.getString("offset"), 16), CharSide.valueOf(location.getString("side"))));
            }
            latinChars.add(c);
        }
        return latinChars;
    }

    public static List<CodePatch> loadCodePatches() {
        List<CodePatch> codePatches = new ArrayList<>();

        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("code-patches");
        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            String code = next.getString("code");
            int offset = Integer.parseInt(next.getString("offset"), 16);
            if (next.has("file") && next.getBoolean("file")) {
                try {
                    System.out.println("Loading code patch "+"src/main/resources/data/output/" + next.getString("offset") + ".data");
                    byte[] bytes = Files.readAllBytes(new File("src/main/resources/data/output/" + next.getString("offset") + ".data").toPath());
                    code = bytesToHex(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                code = next.getString("code");
            }
            CodePatch codePatch = new CodePatch(code, offset);
            if (next.has("debug")) codePatch.setDebug(next.getBoolean("debug"));
            codePatches.add(codePatch);
        }
        return codePatches;
    }

    public static List<InputPatch> loadInputPatches() {
        List<InputPatch> patches = new ArrayList<>();

        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("input-patches");
        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            int offset = Integer.parseInt(next.getString("offset"), 16);
            InputPatch patch = new InputPatch(offset);
            patch.setLatin(next.getString("latin"));
            patch.setType(InputPatch.InputPatchType.valueOf(next.getString("type")));
            if (next.has("debug")) patch.setDebug(next.getBoolean("debug"));
            patches.add(patch);
        }
        return patches;
    }

    public static List<PointerTable> loadTables() {
        List<PointerTable> tables = new ArrayList<>();

        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("tables");
        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            int id = next.getInt("id");
            PointerTable table = new PointerTable(id);
            table.setNewDataStart(Integer.parseInt(next.getString("new-data-start"),16));
            table.setNewDataShift(Integer.parseInt(next.getString("new-data-shift"),16));
            JSONArray pointersArray = next.getJSONArray("pointers");
            for (Object value : pointersArray) {
                JSONObject pointerObject = (JSONObject) value;
                PointerRange range = new PointerRange(
                        Integer.parseInt(pointerObject.getString("start"),16),
                        Integer.parseInt(pointerObject.getString("end"),16),
                        Integer.parseInt(pointerObject.getString("shift"),16));
                if (pointerObject.has("type")) {
                    String type = pointerObject.getString("type");
                    if (type.equals("COUNTER")) {
                        range.setType(PointerRangeType.COUNTER);
                    }
                    if (type.equals("MENU")) {
                        range.setType(PointerRangeType.MENU);
                    }
                    if (type.equals("FIXED_LENGTH")) {
                        range.setType(PointerRangeType.FIXED_LENGTH);
                        table.setStopAtNextPointer(true);
                        table.setKeepOldPointerValues(false);
                    }
                }
                /*if (pointerObject.has("options")) {
                    JSONObject options = (JSONObject)pointerObject.get("options");
                    Map<PointerOption, Object> map = getOptions(options);
                    range.setOptions(map);
                    if (options.has("target-options")) {
                        JSONObject targetOptions = (JSONObject)options.get("target-options");
                        Map<PointerOption, Object> mapTarget = getOptions(targetOptions);
                        range.setTargetOptions(mapTarget);
                    }
                }*/
                table.addPointerRange(range);
            }
            /*if (next.has("menu")) {
                table.setMenu(next.getBoolean("menu"));
            }*/
            if (next.has("counter")) {
                table.setCounter(next.getBoolean("counter"));
            }
            if (next.has("even-length")) {
                table.setEvenLength(next.getBoolean("even-length"));
            }
            if (next.has("overflow")) {
                JSONObject overflow = next.getJSONObject("overflow");
                Overflow ow = new Overflow();
                ow.setLimit(overflow.getInt("limit"));
                ow.setDataStart(overflow.getInt("data-start"));
                ow.setDataShift(overflow.getInt("data-shift"));
                table.setOverflow(ow);
            }
            if (!next.has("skip") || !next.getBoolean("skip")) {
                tables.add(table);
            }
        }
        return tables;
    }
    
    private static Map<PointerOption, Object> getOptions(JSONObject options) {
        Map<PointerOption, Object> map = new HashMap<>();
        if (options.has("menu"))
            map.put(PointerOption.MENU, options.getBoolean("menu"));
        if (options.has("counter"))
            map.put(PointerOption.COUNTER, options.getBoolean("counter"));
        if (options.has("indirect"))
            map.put(PointerOption.INDIRECT, options.getBoolean("indirect"));
        return map;
    }

    public static List<JapaneseChar> loadJapanese() {
        List<JapaneseChar> chars = new ArrayList<>();
        JSONObject json = new JSONObject(loadJson());

        JSONArray array = json.getJSONArray("japanese");
        for (Object o : array) {
            JSONObject next = (JSONObject) o;
            JapaneseChar c = new JapaneseChar();
            String value = next.getString("value");
            if (next.has("code")) {
                c.setCode(next.getString("code"));
            }
            c.setValue(value);
            String type = next.getString("type");
            c.setType(CharType.valueOf(type));
            chars.add(c);
        }
        return chars;
    }

    public static void generateJsonJapanese(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> strings = new ArrayList<>(properties.stringPropertyNames());
        strings.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length()!=o2.length()) return o1.length()-o2.length();
                return o1.compareTo(o2);
            }
        });
        for (String name : strings) {
            String value = properties.getProperty(name);
            CharType type = CharType.MODE_F0;
            if (name.length()==4 && name.startsWith("f1")) {
                type = CharType.MODE_F1;
            }
            System.out.println("{");
            System.out.println("\"value\":\""+value+"\",");
            System.out.println("\"code\":\""+name+"\",");
            System.out.println("\"type\":\""+type+"\"");
            System.out.println("},");
        }
    }

    public static void generateJsonLatin(String filename) {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> strings = new ArrayList<>(properties.stringPropertyNames());
        strings.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length()!=o2.length()) return o1.length()-o2.length();
                return o1.compareTo(o2);
            }
        });
        for (String name : strings) {
            String value = properties.getProperty(name);
            System.out.println("{");
            System.out.println("\"value\":\""+value+"\",");
            System.out.println("\"code\":\""+name+"\"");
            System.out.println("},");
        }
    }

}
