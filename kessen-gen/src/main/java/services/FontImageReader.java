package services;

import enums.FontColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FontImageReader {

    BufferedImage image;

    byte[] imageData;

    public FontImageReader() {

    }

    public String loadFontImage(String file) {
        StringBuffer sb = new StringBuffer();
        try {
            image = ImageIO.read(new File(file));
        } catch (IOException e) {

        }
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
                        FontColor fontColor = FontColor.getFontColor(rgb);
                        int mask = fontColor.getMask();
                        mask = mask >> (x-1);
                        encodedLine = encodedLine | mask;
                    }
                    int leftByte = encodedLine >> 8;
                    int rightByte = encodedLine & 0x00FF;
                    String hex = Utils.toHexString(leftByte, 2)+" "+Utils.toHexString(rightByte,2);
                    sb.append(hex.replaceAll(" ",""));
                    System.out.print(hex+" ");
                }
                System.out.println();
            }
        }
        return sb.toString();
    }


}
