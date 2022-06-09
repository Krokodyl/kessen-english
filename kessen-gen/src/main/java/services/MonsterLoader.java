package services;

import entities.Monster;
import entities.MonsterSkillSet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class MonsterLoader {

    Map<Integer, Monster> monsterMap = new HashMap<>();
    static Map<Integer, MonsterSkillSet> attackMagicMap = new HashMap<>();
    static Map<Integer, MonsterSkillSet> defenseMagicMap = new HashMap<>();
    
    static boolean verbose = true;

    public static void loadAttackMagicSets(byte[] data) {
        int start = Integer.parseInt("909FA",16);
        int end = Integer.parseInt("90A9D",16);
        int shift = Integer.parseInt("88000",16);
        loadMagicSets(data, start, end, shift, attackMagicMap);
    }

    public static void loadDefenseMagicSets(byte[] data) {
        int start = Integer.parseInt("90B92",16);
        int end = Integer.parseInt("90BD3",16);
        int shift = Integer.parseInt("88000",16);
        loadMagicSets(data, start, end, shift, defenseMagicMap);
    }

    public static void loadMagicSets(byte[] data, int start, int end, int shift, Map<Integer, MonsterSkillSet> map) {
        int i = start;
        while (i<=end) {
            int a = (data[i++] & 0xFF);
            int b = (data[i++] & 0xFF);
            int value = b * 256 + a;
            int count = (data[value + shift] & 0xFF);
            byte[] bytes = DataReader.readUntilOffset(data, value + shift + 1, value + shift + + 1 + count);
            int magicValue =  (i-start-1)/2;
            MonsterSkillSet skill = new MonsterSkillSet(
                    magicValue, i-2, count, bytes
            );
            if (verbose) {
                /*System.out.printf("%s - %s\n",Integer.toHexString(i-2), Integer.toHexString(value + shift));
                System.out.printf("(%s)\t%s\t", (i-start-1)/2,count);
                System.out.println(Utils.bytesToHex(bytes));*/
                System.out.println(skill);
            }
            map.put(magicValue, skill);
        }
    }
    
    public static void loadMonsters(byte[] data) throws IOException {
        loadAttackMagicSets(data);
        loadDefenseMagicSets(data);
        int start = Integer.parseInt("90000",16);
        int end = Integer.parseInt("900DD",16);
        int shift = Integer.parseInt("88000",16);
        List<Monster> monsterList = new ArrayList<>();
        int i = start;
        while (i<=end) {
            int a = (data[i++] & 0xFF);
            int b = (data[i++] & 0xFF);
            int value = b * 256 + a;
            byte[] bytes = DataReader.readUntilOffset(data, value + shift, value + shift + 21);

            Monster monster = new Monster(bytes);
            monster.setId(((i-2)-start)-1);
            monster.setOffset(value + shift);
            monsterList.add(monster);
        }
        /*Collections.sort(monsterList, new Comparator<Monster>() {
            @Override
            public int compare(Monster o1, Monster o2) {
                return o1.getOffset()-o2.getOffset();
            }
        });*/
        int k = 0;
        List<String> names = loadMonsterNames();
        for (Monster monster:monsterList) {
            monster.setId(k+1);
            int attackMagicValue = Integer.parseInt(monster.getAttackMagicValue(),16);
            int defenseMagicValue = Integer.parseInt(monster.getDefenseMagicValue(),16);
            if (attackMagicValue>0) {
                MonsterSkillSet skillSet = attackMagicMap.get(attackMagicValue);
                monster.setAttackMagic(getSkillNames(skillSet, loadAttackMagicNames()));
            }
            if (defenseMagicValue>0) {
                MonsterSkillSet skillSet = defenseMagicMap.get(defenseMagicValue);
                monster.setDefenseMagic(getSkillNames(skillSet, loadDefenseMagicNames()));
            }
            if (verbose) {
                //System.out.printf("%s - %s\n",Integer.toHexString(i-2), Integer.toHexString(monster.getOffset()));
                //System.out.println(Utils.bytesToHex(monster.getBytes()));
                System.out.printf("| %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s |\n",
                        monster.getId(),
                        names.get(k++),
                        monster.getHp(),
                        monster.getAttack(),
                        monster.getDefense(),
                        monster.getSpeed(),
                        monster.getMagic(),
                        monster.getXp(),
                        monster.getGold(),
                        Arrays.toString(monster.getAttackMagic()),
                        Arrays.toString(monster.getDefenseMagic()),
                        "<img src=\"/screenshots/monsters/png/"+Utils.padLeft(""+monster.getId(),'0',3)+".png\" alt=\"map\" height=\"208\" width=\"210\"/>"
                        );
            }
        }
    }

    public static List<String> loadNames(String file) throws IOException {
        List<String> names = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(Translator.class.getClassLoader().getResourceAsStream(file)), StandardCharsets.UTF_8));
        String line = br.readLine();

        while (line != null) {
            if (line.contains(Constants.TRANSLATION_KEY_VALUE_SEPARATOR)) {
                String[] split = line.split(Constants.TRANSLATION_KEY_VALUE_SEPARATOR);
                if (split.length>0) {
                    String key = split[0];
                    String value = split[1];
                    if (value.contains("{")) {
                        value = value.replaceAll("\\{DL-","");
                        value = value.replaceAll("}","");
                        String word1 = "";
                        String word2 = "";
                        for (int i=0;i<value.length()-1;i=i+2) {
                            word1 += value.charAt(i);
                            word2 += value.charAt(i+1);
                        }
                        value = word1.trim() + " "+ word2.trim();
                    }
                    names.add(value);
                }
            }
            line = br.readLine();
        }
        return names;
    }

    public static List<String> loadMonsterNames() throws IOException {
        return loadNames("references/06-monsters.txt");
    }
    
    public static List<String> loadAttackMagicNames() throws IOException {
        return loadNames("references/09-attack-magic.txt");
    }
    public static List<String> loadDefenseMagicNames() throws IOException {
        return loadNames("references/10-defense-magic.txt");
    }
    
    public static void addTransparency() throws MalformedURLException {
        String folder = "D:\\git\\kessen-english\\screenshots\\monsters";
        Path path = null;
        try {
            path = Paths.get("D:\\git\\kessen-english\\screenshots\\monsters");
            Files.list(path).forEach(
                    file -> {
                        if (file.toFile().isFile()) {
                            try {

                                BufferedImage image = ImageIO.read(file.toFile());
                                BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                int key = 0;
                                Color c = new Color(255, 255, 255, 0);
                                for (int x = 0; x < image.getWidth(); x++) {
                                    for (int y = 0; y < image.getHeight(); y++) {
                                        if (x == 0 && y == 0) key = image.getRGB(x, y);
                                        if (image.getRGB(x, y) == key) {
                                            out.setRGB(x, y, c.getRGB());
                                        } else {
                                            out.setRGB(x, y, image.getRGB(x, y));
                                        }
                                    }
                                }
                                ImageIO.write(out, "png", new File(folder + "/png/" + file.getFileName().toString().replace("Kessen! English","")));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String[] getSkillNames(MonsterSkillSet set, List<String> names) {
        List<String> list = new ArrayList<>();
        for (byte value : set.getValues()) {
            list.add(names.get(value-1));
        }
        return list.toArray(new String[0]);
    }

    public static String getStat(byte[] monsterData, int index) {
        int a = (monsterData[index] & 0xFF);
        int b = (monsterData[index+1] & 0xFF);
        return (Utils.toHexString(b)+Utils.toHexString(a));
    }    
    
    public static int getIntStat(byte[] monsterData, int index) {
        return Integer.parseInt(getStat(monsterData,index));
    }
}
