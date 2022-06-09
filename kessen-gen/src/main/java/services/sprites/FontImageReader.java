package services.sprites;

import enums.*;
import lz.entities.Header;
import services.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FontImageReader {

    BufferedImage image;

    byte[] imageData;

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public FontImageReader() {

    }

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }

    public void generateSpriteLatinCharacters() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/vram-latin.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteText(),
                2
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/130000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressCopyFile(uncomp, Header.LATIN_SPRITES_HEADER, outputFile);
        //compressedSpriteManager.decompressFile(outputFile, "src/main/resources/data/decomp-1B8000.data");
    }

    public void generateSpriteBattleCards() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/battle-cards.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteBattleCards(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/128000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressCopyFile(uncomp, Header.BATTLE_CARDS_SPRITES_HEADER, outputFile);
    }

    public void generateSpriteClasses() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/classes.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteClasses(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/12E000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressFile(uncomp, Header.CLASSES_SPRITES_HEADER, outputFile);
    }

    public void generateSpriteTownSigns() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/town-signs.png",
                "src/main/resources/data/output/FE05E.data",
                new PaletteTownSigns(),
                4
        );
    }

    public void generateSpriteSilence() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/silence.png",
                "src/main/resources/data/output/90C2E.data",
                new PaletteSilence(),
                4
        );
    }

    public void generateSpriteFreeTown() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/free-town.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteFreeTown(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/129500.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressFile(uncomp, Header.FREE_TOWN_SPRITES_HEADER, outputFile);
    }

    public void generateSpriteTitleKessen() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/title-kessen.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteTitle(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/12C000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressFile(uncomp, Header.TITLE_KESSEN_SPRITE_HEADER, outputFile);
    }

    public void generateSpriteTitleDokapon() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/title-dokapon.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteTitle(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/12D000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressFile(uncomp, Header.TITLE_DOKAPON_SPRITE_HEADER, outputFile);
    }

    public void generateSpriteScoreScreen() throws IOException {
        generateSpriteDataFromImage(
                "src/main/resources/images/score-screen.png",
                "src/main/resources/data/sprite-uncompressed.data",
                new PaletteScoreScreen(),
                4
        );
        String uncomp = "src/main/resources/data/sprite-uncompressed.data";
        String outputFile = "src/main/resources/data/output/12B000.data";
        CompressedSpriteManager compressedSpriteManager = new CompressedSpriteManager(null);
        compressedSpriteManager.compressFile(uncomp, Header.SCORE_SCREEN_SPRITE_HEADER, outputFile);
    }

    private static String generateSpriteDataFromImage(String image, String output, Palette palette, int bpp) throws IOException {
        System.out.println("Generating Sprite Data from image "+image);
        FontImageReader fontImageReader = new FontImageReader();
        String s = "";
        if (bpp==2) s = fontImageReader.loadFontImage2bpp(image, palette);
        else s = fontImageReader.loadFontImage4bpp(image, palette);
        byte[] bytes = fontImageReader.getBytes();

        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(bytes);
            fos.close();
            //There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }

        return s;
    }

    public String loadFontImage2bpp(String file, Palette palette) {
        StringBuffer sb = new StringBuffer();
        try {
            image = ImageIO.read(new File(file));
        } catch (IOException e) {

        }
        boolean stop = false;
        int tileX = 0, tileY = 0, x = 0, y = 0;
        while (tileY++<image.getHeight()/8) {
            tileX=0;
            while (tileX++<image.getWidth()/8) {
                y=0;
                while (y++<8) {
                    x=0;
                    int encodedLine = 0;
                    while (x++<8) {
                        int rgb = image.getRGB(((tileX - 1) * 8 + (x - 1)), ((tileY - 1) * 8 + (y - 1)));
                        if (rgb==0) {
                            stop = true;
                            break;
                        }
                        String color = Utils.getColorAsHex(rgb).toLowerCase();
                        FontColor fontColor = palette.getFontColor(color);
                        int mask = fontColor.getMask();
                        mask = mask >> (x-1);
                        encodedLine = encodedLine | mask;
                    }
                    if (stop) break;
                    int leftByte = encodedLine >> 8;
                    int rightByte = encodedLine & 0x00FF;
                    outputStream.write(leftByte);
                    outputStream.write(rightByte);
                    String hex = Utils.toHexString(leftByte, 2)+" "+Utils.toHexString(rightByte,2);
                    sb.append(hex.replaceAll(" ",""));
                    System.out.print(hex+" ");
                }
                if (stop) break;
                System.out.println();
            }
            if (stop) break;
        }
        return sb.toString();
    }

    public String loadFontImage4bpp(String file, Palette palette) {
        StringBuffer sb = new StringBuffer();
        byte[] output = new byte[0];
        int indexOutput = 0;
        try {
            image = ImageIO.read(new File(file));
            output = new byte[image.getHeight()*image.getWidth()/2];
        } catch (IOException e) {

        }
        boolean stop = false;
        int tileX = 0, tileY = 0, x = 0, y = 0;
        while (tileY++<image.getHeight()/8) {
            tileX=0;
            while (tileX++<image.getWidth()/8) {
                y=0;
                while (y++<8) {
                    x=0;
                    long encodedLine = 0;
                    while (x++<8) {
                        int rgb = image.getRGB(((tileX - 1) * 8 + (x - 1)), ((tileY - 1) * 8 + (y - 1)));
                        String color = Utils.getColorAsHex(rgb).toLowerCase();
                        if (rgb==0) {
                            stop = true;
                            break;
                        }
                        FontColor fontColor = palette.getFontColor(color);
                        long mask = fontColor.getLongMask();
                        mask = mask >> (x-1);
                        encodedLine = encodedLine | mask;
                    }
                    if (stop) break;
                    //int leftByte = encodedLine >> 8;
                    //int rightByte = encodedLine & 0x00FF;
                    long byte1 = encodedLine >> 24;
                    long byte2 = (encodedLine >> 16) & 0x00FF;
                    long byte3 = (encodedLine >> 8) & 0x00FF;
                    long byte4 = (encodedLine) & 0x00FF;

                    output[indexOutput] = (byte) ((byte1) & 0xFF);
                    output[indexOutput+1] = (byte) ((byte2) & 0xFF);
                    output[indexOutput+16] = (byte) ((byte3) & 0xFF);
                    output[indexOutput+17] = (byte) ((byte4) & 0xFF);
                    indexOutput += 2;
                }
                indexOutput += 16;
                if (stop) break;
            }
            if (stop) break;
        }
        int k = 0;
        for (byte b:output) {
            if (k++%16==0) System.out.println();
            String s = Utils.toHexString(b) + " ";
            sb.append(s);
            outputStream.write(b);
            System.out.print(s);
        }
        return sb.toString();
    }


}
