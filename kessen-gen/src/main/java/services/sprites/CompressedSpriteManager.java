package services.sprites;


import entities.Config;
import lz.compression.Compressor;
import lz.compression.CopyCompressor;
import lz.decompression.Decompressor;
import lz.entities.Header;
import services.JsonLoader;
import services.Kessen;
import services.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompressedSpriteManager {

    byte[] data;

    public CompressedSpriteManager(byte[] data){
        this.data = data;
    }

    public static void main(String[] args) {
        Config config = JsonLoader.loadConfig();
        try {
            byte[] data = Files.readAllBytes(new File(config.getRomInput()).toPath());
            new CompressedSpriteManager(data).decompressStuff();
        } catch (IOException ex) {
            Logger.getLogger(Kessen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*private void testCopyCompressor() throws IOException {
        String uncomp = "src/main/resources/data/jpn/BBA39.data";
        String outputFile = "src/main/resources/data/output/1E0000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressCopyFile(uncomp, Header.MAP_ORDER_HEADER, outputFile);
    }*/

    void compressFile(String input, Header header, String output) throws IOException {
        byte[] data = Files.readAllBytes(new File(input).toPath());

        System.out.println("Time : "+(Kessen.getTime()));
        Compressor compressor = new Compressor(data, header);
        compressor.compress();
        byte[] compressedBytes = compressor.getCompressedBytes();

        System.out.println();
        System.out.println("Compressed length : "+ compressedBytes.length);
        System.out.println();
        System.out.println("Compressed bytes : "+ Utils.bytesToHex(compressedBytes));
        System.out.println("Time : "+(Kessen.getTime()));

        Files.write(new File(output).toPath(), compressedBytes);
    }

    void compressCopyFile(String input, Header header, String output) throws IOException {
        byte[] data = Files.readAllBytes(new File(input).toPath());

        

        CopyCompressor compressor = new CopyCompressor(header, data);
        compressor.compress();
        byte[] compressedBytes = compressor.getCompressedBytes();

        /*System.out.println();
        System.out.println("Compressed length : "+ compressedBytes.length);
        System.out.println();
        System.out.println("Compressed bytes : "+Utils.bytesToHex(compressedBytes));
        System.out.println("Time : "+(Dokapon.getTime()));*/

        System.out.println("Compressing data - output : "+output);

        Files.write(new File(output).toPath(), compressedBytes);
    }

    public void decompressFile(String inputFile, String outputFile) throws IOException {
        byte[] data = Files.readAllBytes(new File(inputFile).toPath());
        Decompressor decompressor = new Decompressor(data, 3);
        decompressor.decompressData();
        byte[] decompressedBytes = decompressor.getDecompressedBytes();
        System.out.println();
        System.out.println("Decompressed bytes : "+Utils.bytesToHex(decompressedBytes));
        Files.write(new File(outputFile).toPath(), decompressedBytes);
    }

    public void compressMapData() throws IOException {
        String uncomp = "src/main/resources/data/map-uncompressed.data";
        String outputFile = "src/main/resources/data/output/110000.data";
        compressFile(uncomp, Header.MAP_DATA_HEADER, outputFile);
    }

    public void decompressTilesData(String input, String output) throws IOException {
        String s = input;
        int start = Integer.parseInt(s, 16)+3;
        Decompressor decompressor = new Decompressor(data, start);
        decompressor.decompressData();
        byte[] decompressedBytes = decompressor.getDecompressedBytes();
        System.out.println("From "+Utils.toHexString(start-3,6)+" to "+Utils.toHexString(decompressor.getEnd(),6));
        System.out.println("Header expected size "+decompressor.getHeader().getDecompressedLength());
        System.out.println("Decompressed bytes : "+Utils.bytesToHex(decompressedBytes));
        try (FileOutputStream fos = new FileOutputStream(
                "src/main/resources/gen/"+s+".data"
        )) {
            fos.write(decompressedBytes);
        }
    }

    public void compressTilesData() throws IOException {
        String uncomp = "src/main/resources/data/tiles-uncompressed.data";
        String outputFile = "src/main/resources/data/output/120000.data";
        compressFile(uncomp, Header.TILES_DATA_HEADER, outputFile);
    }

    public void compressScoreScreenData() throws IOException {
        String uncomp = "src/main/resources/data/score-screen-uncompressed.data";
        String outputFile = "src/main/resources/data/output/12A000.data";
        compressFile(uncomp, Header.SCORE_SCREEN_DATA_HEADER, outputFile);
    }
    
    public void decompressMapData(String input, String output) throws IOException {
        String s = input;
        int start = Integer.parseInt(s, 16)+3;
        Decompressor decompressor = new Decompressor(data, start);
        decompressor.decompressData();
        byte[] decompressedBytes = decompressor.getDecompressedBytes();
        System.out.println("From "+Utils.toHexString(start-3,6)+" to "+Utils.toHexString(decompressor.getEnd(),6));
        System.out.println("Header expected size "+decompressor.getHeader().getDecompressedLength());
        System.out.println("Decompressed bytes : "+Utils.bytesToHex(decompressedBytes));
        if (decompressor.getHeader().getDecompressedLength()==53764)
        {
            byte[] mapData = Arrays.copyOfRange(decompressedBytes, 4, decompressedBytes.length);
            PrintWriter pw = new PrintWriter(new File("src/main/resources/gen/map.txt"));
            String[] split = Utils.bytesToHex(mapData).split("(?<=\\G.{960})");
            for (String s1 : split) {
                pw.write(s1+"\n");
                System.out.println(s1);
            }
            pw.close();
        }
        try (FileOutputStream fos = new FileOutputStream(
                "src/main/resources/gen/"+s+".data"
        )) {
            fos.write(decompressedBytes);
        }
    }

    public static String MAP_OVERWORLD_OFFSET = "C4ED5";
    public static String MAP_OVERWORLD_OFFSET_EN = "110000";
    public static int MAP_OVERWORLD_WIDTH = 160;

    public void decompressMapData16bits(String s, int width) throws IOException {
        
        int start = Integer.parseInt(s, 16)+3;
        Decompressor decompressor = new Decompressor(data, start);
        decompressor.decompressData();
        byte[] decompressedBytes = decompressor.getDecompressedBytes();
        byte[] mapData = Arrays.copyOfRange(decompressedBytes, 4, decompressedBytes.length);
        String regex = "(?<=\\G.{"+width*6+"})";
        String[] split = Utils.bytesToHex(mapData).split(regex);

        BufferedImage gameImage = ImageIO.read(new File("src/main/resources/images/tiles16bits/game-map.png"));

        BufferedImage image = new BufferedImage(MAP_OVERWORLD_WIDTH * 16, split.length * 16, BufferedImage.TYPE_INT_RGB);
        
        List<String> codeUsed = new ArrayList<>();
        int lineIndex = 0;
        for (String s1 : split) {

            String[] split1 = s1.split("(?<=\\G.{6})");
            int colIndex = 0;
            for (String s2 : split1) {
                if (codeUsed.contains(s2)) {
                    //pw.write("      ");
                }
                else {
                    codeUsed.add(s2);
                    //writeSubImage(gameImage, colIndex*16, lineIndex*16, 16, 16, "src/main/resources/images/tiles16bits/"+s2+".png");
                    //pw.write(s2);
                }
                File file = new File("src/main/resources/images/tiles16bits/" + s2 + ".png");
                if (!file.exists()) {
                    file = new File("src/main/resources/images/tiles16bits/00 00.png");
                }
                BufferedImage tile = ImageIO.read(file);
                
                addImage(image, tile, 1, colIndex * 16, lineIndex * 16);
                colIndex++;
            }
            //pw.write("\n");

            //System.out.println(s1);
            lineIndex++;
        }
        //pw.close();
        ImageIO.write(image, "png", new File("src/main/resources/gen/map16.png"));
        
        //
        
    }
    
    public void writeSubImage(BufferedImage image, int x,int y, int w,int h, String file) throws IOException {
        BufferedImage subimage = image.getSubimage(x, y, w, h);
        ImageIO.write(subimage, "png", new File(file));
    }
    
    public void loadTiles() {
        Map<String, BufferedImage> imageMap = new HashMap<>();
        Path path = null;
        try {
            path = Paths.get("D:\\git\\kessen-english\\screenshots\\monsters");
            Files.list(path).forEach(
                    file -> {
                        if (file.toFile().isFile()) {

                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void decompressMapData(String s, int width) throws IOException {
        int start = Integer.parseInt(s, 16)+3;
        Decompressor decompressor = new Decompressor(data, start);
        decompressor.decompressData();
        byte[] decompressedBytes = decompressor.getDecompressedBytes();
        System.out.println("From "+Utils.toHexString(start-3,6)+" to "+Utils.toHexString(decompressor.getEnd(),6));
        System.out.println("Header expected size "+decompressor.getHeader().getDecompressedLength());
        System.out.println("Decompressed bytes : "+Utils.bytesToHex(decompressedBytes));
        byte[] mapData = Arrays.copyOfRange(decompressedBytes, 4, decompressedBytes.length);
        PrintWriter pw = new PrintWriter(new File("src/main/resources/gen/map-"+s+".txt"));
        String regex = "(?<=\\G.{"+width*6+"})";
        String[] split = Utils.bytesToHex(mapData).split(regex);
        BufferedImage image = new BufferedImage(MAP_OVERWORLD_WIDTH * 8, split.length * 8, BufferedImage.TYPE_INT_RGB);

        BufferedImage image0 = ImageIO.read(new File("src/main/resources/images/tiles/00 00.png"));
        BufferedImage imageSea = ImageIO.read(new File("src/main/resources/images/tiles/02 00.png"));
        BufferedImage imageGrass = ImageIO.read(new File("src/main/resources/images/tiles/grass1.png"));
        BufferedImage imageGrassVRoad = ImageIO.read(new File("src/main/resources/images/tiles/grass2.png"));
        BufferedImage imageGrassHRoad = ImageIO.read(new File("src/main/resources/images/tiles/grass3.png"));
        BufferedImage imageGrassCrossroad = ImageIO.read(new File("src/main/resources/images/tiles/grass4.png"));
        Map<String, BufferedImage> imageMap = new HashMap<>();
        imageMap.put("02 00 ", imageSea);
        imageMap.put("E2 00 ", imageSea);
        imageMap.put("E3 00 ", imageSea);
        imageMap.put("E4 00 ", imageSea);
        imageMap.put("E5 00 ", imageSea);
        imageMap.put("E6 00 ", imageSea);
        imageMap.put("E7 00 ", imageSea);
        imageMap.put("E8 00 ", imageSea);
        imageMap.put("E9 00 ", imageSea);
        imageMap.put("EB 00 ", imageSea);
        imageMap.put("EC 00 ", imageSea);
        imageMap.put("ED 00 ", imageSea);
        imageMap.put("EE 00 ", imageSea);
        imageMap.put("EF 00 ", imageSea);
        imageMap.put("EA 00 ", imageSea);
        imageMap.put("C5 00 ", imageSea);
        imageMap.put("D6 00 ", imageSea);
        imageMap.put("D7 00 ", imageSea);
        imageMap.put("01 00 ", imageGrass);
        imageMap.put("DE 00 ", imageGrass);
        imageMap.put("24 00 ", imageGrass);
        imageMap.put("2C 00 ", imageGrass);
        imageMap.put("40 00 ", imageGrass);
        imageMap.put("23 00 ", imageGrass);
        imageMap.put("2B 00 ", imageGrass);
        imageMap.put("C8 00 ", imageGrass);
        imageMap.put("38 00 ", imageGrass);
        imageMap.put("C9 00 ", imageGrass);
        imageMap.put("CA 00 ", imageGrass);
        imageMap.put("CB 00 ", imageGrass);
        imageMap.put("CC 00 ", imageGrass);
        imageMap.put("CD 00 ", imageGrass);
        imageMap.put("CE 00 ", imageGrass);
        imageMap.put("D8 00 ", imageGrass);
        imageMap.put("D9 00 ", imageGrass);
        imageMap.put("DB 00 ", imageGrass);
        imageMap.put("DA 00 ", imageGrass);
        imageMap.put("DC 00 ", imageGrass);
        imageMap.put("DD 00 ", imageGrass);
        imageMap.put("DF 00 ", imageGrass);
        imageMap.put("11 00 ", imageGrassVRoad);
        imageMap.put("10 00 ", imageGrassHRoad);
        imageMap.put("12 00 ", imageGrassCrossroad);
        imageMap.put("0A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass5.png")));
        imageMap.put("0B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass6.png")));
        imageMap.put("09 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass7.png")));
        imageMap.put("08 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass8.png")));
        imageMap.put("03 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass9.png")));
        imageMap.put("04 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass10.png")));
        imageMap.put("0F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass11.png")));
        imageMap.put("0E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass12.png")));
        imageMap.put("05 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass13.png")));
        imageMap.put("0C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass14.png")));
        imageMap.put("06 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass15.png")));
        imageMap.put("0D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/grass16.png")));
        imageMap.put("49 01 ", ImageIO.read(new File("src/main/resources/images/tiles/coin.png")));

        imageMap.put("94 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("98 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CF 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("41 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("95 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("96 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D8 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D9 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C8 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C0 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C3 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C8 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("DE 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));


        imageMap.put("97 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("99 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("9B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C1 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C2 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C4 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C5 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C6 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C9 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CA 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CB 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CC 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CD 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CE 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("CF 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D0 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D1 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D2 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D3 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D4 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D6 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D5 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("D7 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("DA 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("DC 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("DD 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("C7 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));
        imageMap.put("DF 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mountain.png")));

        imageMap.put("85 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("87 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("80 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("81 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("83 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("8D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("84 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("8C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("8F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest1.png")));
        imageMap.put("86 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest2.png")));
        imageMap.put("8E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest2.png")));
        imageMap.put("82 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest2.png")));
        imageMap.put("88 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest3.png")));
        imageMap.put("89 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest3.png")));
        imageMap.put("8B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest3.png")));
        imageMap.put("8A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/forest4.png")));

        imageMap.put("04 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("05 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("06 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("07 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("10 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("08 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("09 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("0E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp1.png")));
        imageMap.put("13 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp2.png")));
        imageMap.put("14 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp2.png")));
        imageMap.put("15 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp2.png")));
        imageMap.put("16 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp3.png")));
        imageMap.put("12 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp3.png")));
        imageMap.put("11 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp3.png")));
        imageMap.put("17 01 ", ImageIO.read(new File("src/main/resources/images/tiles/swamp4.png")));

        imageMap.put("4E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/secret1.png")));
        imageMap.put("4F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/secret1.png")));
        imageMap.put("33 00 ", ImageIO.read(new File("src/main/resources/images/tiles/secret2.png")));
        imageMap.put("3B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/secret2.png")));
        imageMap.put("47 00 ", ImageIO.read(new File("src/main/resources/images/tiles/secret3.png")));
        imageMap.put("37 00 ", ImageIO.read(new File("src/main/resources/images/tiles/secret4.png")));
        imageMap.put("3F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/secret4.png")));
        imageMap.put("D9 01 ", ImageIO.read(new File("src/main/resources/images/tiles/secret5.png")));
        imageMap.put("4A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/secret5.png")));
        
        imageMap.put("19 00 ", ImageIO.read(new File("src/main/resources/images/tiles/chest1.png")));
        imageMap.put("18 00 ", ImageIO.read(new File("src/main/resources/images/tiles/chest2.png")));
        imageMap.put("1A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/chest3.png")));

        imageMap.put("58 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town1.png")));
        imageMap.put("59 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town1.png")));
        imageMap.put("56 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town2.png")));
        imageMap.put("97 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("98 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("99 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("9A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("9B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("9C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("9D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        imageMap.put("5A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town4.png")));
        imageMap.put("5B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/town4.png")));

        imageMap.put("90 00 ", ImageIO.read(new File("src/main/resources/images/tiles/one-way1.png")));
        imageMap.put("92 00 ", ImageIO.read(new File("src/main/resources/images/tiles/one-way2.png")));
        imageMap.put("E0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/one-way3.png")));
        imageMap.put("C6 00 ", ImageIO.read(new File("src/main/resources/images/tiles/one-way3.png")));
        imageMap.put("C7 00 ", ImageIO.read(new File("src/main/resources/images/tiles/one-way3.png")));


        imageMap.put("D0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col1.png")));
        imageMap.put("D1 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col2.png")));
        imageMap.put("D2 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col3.png")));
        imageMap.put("D3 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col4.png")));
        imageMap.put("D4 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col5.png")));
        imageMap.put("D5 00 ", ImageIO.read(new File("src/main/resources/images/tiles/col6.png")));

        imageMap.put("13 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church1.png")));
        imageMap.put("14 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church2.png")));
        imageMap.put("15 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church3.png")));
        imageMap.put("1E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church4.png")));
        imageMap.put("1C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("25 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("26 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("60 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("2D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("2E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("1B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church5.png")));
        imageMap.put("1F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/church6.png")));

        imageMap.put("78 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop1.png")));
        imageMap.put("79 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop2.png")));
        imageMap.put("7C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop3.png")));
        imageMap.put("89 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop3.png")));
        imageMap.put("75 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("6D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("82 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("65 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("7E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("66 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("85 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("74 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("80 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("96 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("64 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("8C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("69 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("6C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("6E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("81 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("84 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("6F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("76 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("86 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("87 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("67 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("77 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("7F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("83 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("2F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("70 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("73 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop4.png")));
        imageMap.put("7D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop5.png")));
        imageMap.put("8A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop5.png")));
        imageMap.put("71 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop6.png")));
        imageMap.put("6B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop7.png")));
        imageMap.put("6A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop7.png")));
        imageMap.put("7A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop8.png")));
        imageMap.put("7B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/shop8.png")));


        imageMap.put("AB 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("A2 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("AF 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("AD 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("4C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("A0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("A6 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        imageMap.put("A4 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs1.png")));
        
        imageMap.put("18 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("19 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("1F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("20 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("21 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("22 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("23 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("24 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("25 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("26 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("27 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("28 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("29 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("2F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("30 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("31 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("32 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("33 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("34 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("35 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("36 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("37 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("38 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("39 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3A 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3C 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3D 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3E 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("3F 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("40 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("41 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("42 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("43 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("44 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("45 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("46 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("47 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("48 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs2.png")));
        imageMap.put("A1 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("AC 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("A3 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("AE 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("4B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("A5 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        imageMap.put("A7 00 ", ImageIO.read(new File("src/main/resources/images/tiles/signs3.png")));
        
        imageMap.put("34 00 ", ImageIO.read(new File("src/main/resources/images/tiles/circle.png")));

        

        imageMap.put("07 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("49 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("48 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("56 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("55 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("50 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("51 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("54 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("52 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert2.png")));
        imageMap.put("4D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert2.png")));
        imageMap.put("4E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert2.png")));
        imageMap.put("53 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert1.png")));
        imageMap.put("4A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert3.png")));
        imageMap.put("57 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert3.png")));
        imageMap.put("4F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert3.png")));
        imageMap.put("4B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/desert4.png")));


        //imageMap.put("5A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks1.png")));
        imageMap.put("64 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks2.png")));
        imageMap.put("66 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks2.png")));
        imageMap.put("72 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks2.png")));
        imageMap.put("63 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks3.png")));
        imageMap.put("62 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks3.png")));
        imageMap.put("71 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks3.png")));
        imageMap.put("73 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks4.png")));
        imageMap.put("65 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("67 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("76 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B1 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B2 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B3 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B4 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B5 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B6 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B7 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BE 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("C0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("C1 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("79 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("75 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("78 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("A9 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("AA 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B8 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("A8 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("77 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("70 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("68 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("6A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BD 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("C4 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("7D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("C3 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BA 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BB 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BC 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("BF 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("B9 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("C2 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("61 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        imageMap.put("69 00 ", ImageIO.read(new File("src/main/resources/images/tiles/rocks5.png")));
        
        
        imageMap.put("5A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea2.png")));
        imageMap.put("59 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea3.png")));
        imageMap.put("58 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea4.png")));
        imageMap.put("5D 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea5.png")));
        imageMap.put("5E 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea6.png")));
        imageMap.put("5C 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea7.png")));
        imageMap.put("5B 00 ", ImageIO.read(new File("src/main/resources/images/tiles/sea8.png")));


        imageMap.put("F0 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F2 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F1 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F3 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F4 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F5 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F6 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F7 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F8 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("F9 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("FA 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("FB 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("FC 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud1.png")));
        imageMap.put("00 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mud2.png")));
        imageMap.put("01 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mud2.png")));
        imageMap.put("FF 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud2.png")));
        imageMap.put("02 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mud3.png")));
        imageMap.put("FD 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud3.png")));
        imageMap.put("FE 00 ", ImageIO.read(new File("src/main/resources/images/tiles/mud3.png")));
        imageMap.put("03 01 ", ImageIO.read(new File("src/main/resources/images/tiles/mud4.png")));


        imageMap.put("43 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave1.png")));
        imageMap.put("39 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave1.png")));
        imageMap.put("3A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave2.png")));
        imageMap.put("17 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave3.png")));
        imageMap.put("5F 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave3.png")));
        imageMap.put("27 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave3.png")));
        imageMap.put("42 00 ", ImageIO.read(new File("src/main/resources/images/tiles/cave4.png")));


        imageMap.put("8B 01 ", ImageIO.read(new File("src/main/resources/images/tiles/office.png")));


        imageMap.put("20 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle1.png")));
        imageMap.put("21 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle2.png")));
        imageMap.put("22 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle3.png")));
        imageMap.put("28 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle4.png")));
        imageMap.put("29 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle5.png")));
        imageMap.put("2A 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle6.png")));
        imageMap.put("30 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle7.png")));
        imageMap.put("31 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle8.png")));
        imageMap.put("32 00 ", ImageIO.read(new File("src/main/resources/images/tiles/castle9.png")));
        
        String[] towns = {
                "8E 01 ", "8F 01 ", "90 01 ", "91 01 ", "92 01 ", "93 01 ", "94 01 ", "95 01 ", "9E 01 ", "9F 01 ", "A0 01 ", "A1 01 ", "A2 01 ", "A3 01 ", "A4 01 ", "A5 01 ", "A6 01 ", "A7 01 ", "A8 01 ", "A9 01 ", "AA 01 ", "AB 01 ", "AC 01 ", "AD 01 ", "AE 01 ", "AF 01 ", "B0 01 ", "B1 01 ", "B2 01 ", "B3 01 ", "B4 01 ", "B5 01 ", "B6 01 ", "B7 01 ", "B8 01 ", "B9 01 ", "BA 01 ", "BB 01 ", "BC 01 ", "BD 01 ", "BE 01 ", "BF 01 "
        };
        for (String town : towns) {
            imageMap.put(town, ImageIO.read(new File("src/main/resources/images/tiles/town3.png")));
        }
        
/*
        78 01 71 01 79 01
        7C 01 75 01 7D 01
        
        78 01 6B 01 79 01
        7C 01 6D 01 8A 01
        
        78 01 7B 01 79 01
        7C 01 82 01 7D 01
        
 */
        int lineIndex = 0;
        Set<String> codes = new HashSet<>();
        for (String s1 : split) {
            
            
            String[] split1 = s1.split("(?<=\\G.{6})");
            int colIndex = 0;
            for (String s2 : split1) {
                if (imageMap.containsKey(s2)) pw.write("      ");
                else {
                    codes.add(s2);
                    pw.write(s2);
                }
                if (imageMap.containsKey(s2)) 
                    addImage(image, imageMap.getOrDefault(s2, image0), 1, colIndex * 8, lineIndex * 8);
                colIndex++;
            }
            pw.write("\n");

            //System.out.println(s1);
            lineIndex++;
        }
        pw.close();
        ImageIO.write(image, "png", new File("src/main/resources/gen/map-"+s+".png"));
        System.out.println("Codes size "+codes.size());
        ArrayList<String> strings = new ArrayList<>(codes);
        Collections.sort(strings);
        for (String code : strings) {
            System.out.println(code);
        }

    }

    /**
     * prints the contents of buff2 on buff1 with the given opaque value.
     */
    private void addImage(BufferedImage buff1, BufferedImage buff2,
                          float opaque, int x, int y) {
        Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();
    }

    private void decompressStuff() throws IOException {
        String[] addresses = new String[]{

                "3393D",
                "65d4c",
                "61344",
                "6340e",
                "a4678",
                "a50fe",
                "a5a42",
                "a6614",
                "96c98",
                "48000",
                "3b15a"
                
                /*"D4BE4",
                "16D58F",
                "16EA3F",*/
                //"585AA"

                /*"16CD62",
                "16CE67",
                "16CF6C",
                "16D071",
                "16D176",
                "16D27B",
                "16D380",
                "16D485"*/

                /*"BF746",
                "D4DE2",
                "100FC4",
                "101755"*/

                /*"AD772"*/

                //"175C9E"
                //"140AD5"
                /*"BBA39",
                "BDC95",
                "B2528"*/
                /*"BBA39",
                "1D0000",
                "D4DE2",
                "1712E2",
                "BEA18"*/
        };
        for (String s:addresses) {
            int start = Integer.parseInt(s, 16)+3;
            Decompressor decompressor = new Decompressor(data, start);
            decompressor.decompressData();
            byte[] decompressedBytes = decompressor.getDecompressedBytes();
            System.out.println("From "+Utils.toHexString(start-3,6)+" to "+Utils.toHexString(decompressor.getEnd(),6));
            System.out.println("Header expected size "+decompressor.getHeader().getDecompressedLength());
            System.out.println("Decompressed bytes : "+Utils.bytesToHex(decompressedBytes));
            /*if (decompressor.getHeader().getDecompressedLength()==53764)
            {
                byte[] mapData = Arrays.copyOfRange(decompressedBytes, 4, decompressedBytes.length);
                PrintWriter pw = new PrintWriter(new File("src/main/resources/gen/map.txt"));
                String[] split = Utils.bytesToHex(mapData).split("(?<=\\G.{960})");
                for (String s1 : split) {
                    pw.write(s1+"\n");
                    System.out.println(s1);
                }
                pw.close();
            }
            if (decompressor.getHeader().getDecompressedLength()==6592)
            {
                String[] split = Utils.bytesToHex(decompressedBytes).split("(?<=\\G.{618})");
                for (String s1 : split) {
                    System.out.println(s1);
                }

            }
            if (decompressor.getHeader().getDecompressedLength()==4608)
            {
                int offset = Integer.parseInt("110",16);
                String[] split = Utils.bytesToHex(decompressedBytes).split("(?<=\\G.{48})");
                for (String s1 : split) {
                    System.out.println(Integer.toHexString(offset)+" - "+s1);
                    offset+=16;
                }

            }
            try (FileOutputStream fos = new FileOutputStream(
                    "src/main/resources/gen/"+s+".data"
            )) {
                fos.write(decompressedBytes);
            }*/
        }
    }
    
}
