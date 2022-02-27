package services;

import compression.DataByte;
import compression.FlagByte;
import compression.RepeatByte;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Decompressor {

    byte[] input;

    byte[] decompOutput;
    int decompIndex = 16;


    public Decompressor(byte[] data, int start, int end) {
        input = new byte[end-start+1];
        decompOutput = new byte[2000*16];
        Arrays.fill(decompOutput, (byte) 0);
        /*while (decompIndex++<32) {
            if (decompIndex>16) decompOutput[decompIndex] = (byte) Integer.parseInt("ff",16);
        }*/
        System.out.println(data.length);
        System.out.println(input.length);
        System.arraycopy(data, start, input, 0, end-start);
    }

    public void parseInput() {
        int index = 0;

        while (index < input.length) {
            FlagByte flagByte = new FlagByte(input[index++]);
            System.out.println(flagByte);
            int cursor = 0;
            while (cursor<8) {
                if (flagByte.isNewData(cursor)) {
                    if (index==10241) {
                        System.out.println();
                        /*try {
                            printOutput();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }*/
                    }
                    DataByte dataByte = new DataByte(input[index++]);
                    decompOutput[decompIndex++] = dataByte.getValue();
                    System.out.println(dataByte);
                } else {
                    RepeatByte repeatByte = new RepeatByte(input[index++], input[index++]);
                    //System.out.println("size "+repeatByte.getSize());
                    System.out.println(repeatByte);
                    //System.out.println("Exact Pos : "+repeatByte.getExactPosition());
                    decompressRepeatByte(repeatByte);
                }
                cursor++;
            }
        }
    }

    public void decomp() {
        try {
            parseInput();
            //printOutput();
        } catch (Exception e) {
            /*try {
                printOutput();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }*/
        }
    }

    public void printSingleLineOutput() {
        int offset = -16;
        for (int i=0;i<decompOutput.length;i++) {
            if (i%16==0) {
                System.out.println();
                System.out.print(Utils.toHexString(offset,4)+"   ");
                offset+=16;
            }
            System.out.print(Utils.toHexString(decompOutput[i] & 0xFF,2)+" ");
            if (i>=128) return;
        }
    }

    private void printOutput() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("D:/git/kessen-english/font/output.txt", "UTF-8");

        int offset = -16;
        for (int i=0;i<decompOutput.length;i++) {
            if (i%16==0) {
                System.out.println();
                writer.println();
                System.out.print(Utils.toHexString(offset,4)+"   ");
                offset+=16;
            }
            writer.print(Utils.toHexString(decompOutput[i] & 0xFF,2));
            System.out.print(Utils.toHexString(decompOutput[i] & 0xFF,2)+" ");
        }
        writer.close();
    }

    public void decompressRepeatByte(RepeatByte repeatByte) {
        byte value = 0;
        int size = repeatByte.getSize()+2;
        int position = repeatByte.getPosition();
        //if (position==255) position = position & 0x0F;
        int windowStart = decompIndex-1-position;
        //System.out.println("windowStart"+windowStart);
        int windowIndex = windowStart;
        if (windowIndex<0) windowIndex=0;

        byte[] written = new byte[size];
        int indexWritten = 0;

        while (size-->0) {
            value = decompOutput[windowIndex++];
            decompOutput[decompIndex++] = value;
            written[indexWritten++] = value;
        }

        System.out.println("Written : "+Utils.bytesToHex(written));

        //Arrays.fill(decompOutput, decompIndex, decompIndex+size, value);
        //System.arraycopy(decompOutput, decompIndex-repeatByte.getPosition(), decompOutput, decompIndex, repeatByte.getSize());
        //decompIndex += size;
    }

    public void test() {
        for (byte b:input) {
            FlagByte flagByte = new FlagByte(b);
            for (int i=0;i<8;i++)
                System.out.println(flagByte.isNewData(i));
        }
    }
}
