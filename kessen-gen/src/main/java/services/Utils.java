package services;

import entities.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Constants.*;

public class Utils {

    /**
     * return a binary string padded on 8 chars with 0.
     */
    public static String toBinaryString(byte value) {
        String binaryString = Integer.toBinaryString(value & 0xFF);
        binaryString = String.format("%08d", Integer.parseInt(binaryString));
        return binaryString;
    }

    public static String toHexString(byte value) {
        return toHexString(((int)value) & 0xFF, 2);
    }

    public static String toHexString(int value) {
        return toHexString(value & 0xFF, 2);
    }

    public static String toHexString(int value, int padding) {
        return String.format("%0"+padding+"x",value).toUpperCase();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    public static byte reverse(byte b) {
        String s = Utils.toBinaryString(b);
        StringBuilder reverse = new StringBuilder(s).reverse();
        int i = Integer.parseInt(reverse.toString(), 2);
        return (byte) (i & 0xFF);
    }

    public static byte complement(byte b) {
        String s = Utils.toBinaryString(b);
        s = s.replaceAll("0","X");
        s = s.replaceAll("1","0");
        s = s.replaceAll("X", "1");
        int i = Integer.parseInt(s, 2);
        return (byte) (i & 0xFF);
    }

    public static void main(String[] args) {
        Config config = JsonLoader.loadConfig();
        try {
            byte[] data = Files.readAllBytes(new File(config.getRomInput()).toPath());




        } catch (IOException ex) {
            Logger.getLogger(Kessen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static int findArray(int[] largeArray, int[] subArray) {

        /* If any of the arrays is empty then not found */
        if (largeArray.length == 0 || subArray.length == 0) {
            return -1;
        }

        /* If subarray is larger than large array then not found */
        if (subArray.length > largeArray.length) {
            return -1;
        }

        for (int i = 0; i < largeArray.length; i++) {
            /* Check if the next element of large array is the same as the first element of subarray */
            if (largeArray[i] == subArray[0]) {

                boolean subArrayFound = true;
                for (int j = 0; j < subArray.length; j++) {
                    /* If outside of large array or elements not equal then leave the loop */
                    if (largeArray.length <= i+j || subArray[j] != largeArray[i+j]) {
                        subArrayFound = false;
                        break;
                    }
                }

                /* Sub array found - return its index */
                if (subArrayFound) {
                    return i;
                }

            }
        }

        /* Return default value */
        return -1;
    }

    public static String padLeft(String s,char c,int length) {
        while (s.length()<length) {
            s=c+s;
        }
        return s;
    }
}
