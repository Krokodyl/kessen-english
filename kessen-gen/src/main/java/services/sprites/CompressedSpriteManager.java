package services.sprites;


import entities.Config;
import lz.compression.Compressor;
import lz.compression.CopyCompressor;
import lz.decompression.Decompressor;
import lz.entities.Header;
import services.JsonLoader;
import services.Kessen;
import services.Utils;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
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

    private void decompressStuff() throws IOException {
        String[] addresses = new String[]{
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

                "F8000"

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
            }
        }
    }
    
}
