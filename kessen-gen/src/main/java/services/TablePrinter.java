package services;

import characters.JapaneseChar;
import entities.Dictionnary;
import entities.PointerRange;
import entities.PointerTable;
import enums.CharType;

import java.util.Arrays;
import java.util.List;

import static services.Constants.*;
import static services.DataReader.readUntilEndOfLine;
import static services.Utils.bytesToHex;
import static services.Utils.toHexString;

public class TablePrinter {

    public void generateTranslationFile(PointerTable table, byte[] data, Dictionnary japanese) {
        List<PointerRange> ranges = table.getRanges();
        for (PointerRange range : ranges) {
            int start = range.getStart();
            int end = range.getEnd();
            int shift = range.getShift();
            for (int offset=start;offset<=end;offset=offset+2) {
                int byte1 = data[offset] & 0xFF;
                int byte2 = data[offset+1] & 0xFF;
                int value = byte2 * 256 + byte1;
                byte[] bytes = readUntilEndOfLine(data, value + shift);
                String jpn = getJapaneseFromBytes(bytes, japanese);
                String eng = "";
                System.out.println(TRANSLATION_KEY_OFFSET+"="+toHexString(offset, 6));
                System.out.println(TRANSLATION_KEY_VALUE+"="+toHexString(value, 4));
                System.out.println(TRANSLATION_KEY_OFFSETDATA+"="+toHexString(value + shift, 6));
                System.out.println(TRANSLATION_KEY_DATA+"="+bytesToHex(bytes));
                System.out.println(TRANSLATION_KEY_JPN +"="+jpn);
                System.out.println(TRANSLATION_KEY_ENG+"="+eng);
                System.out.println();
            }
        }
    }

    public void generateMenuTable(byte[] data, Dictionnary japanese) {
        int i = Integer.parseInt("2028D",16);
        int count = 0;
        while (i <= Integer.parseInt("209DF",16)) {
            byte cursor = data[i];
            if (cursor == 2 && data[i+5]==0) {
                int a = (data[i+6] & 0xFF);
                int b = (data[i+7] & 0xFF);
                int offsetData = Integer.parseInt("18000",16) + (b * 256 + a);
                byte[] bytes = readUntilEndOfLine(data, offsetData);
                count++;
                if (bytes[0]==MODE_F0_BYTE || bytes[0]==MODE_F1_BYTE) {

                System.out.println(TRANSLATION_KEY_OFFSET+"="+toHexString(i,5));
                System.out.println(TRANSLATION_KEY_VALUE+"="+toHexString(b)+toHexString(a));
                System.out.println(TRANSLATION_KEY_OFFSETDATA+"="+toHexString(offsetData,5));
                System.out.println(TRANSLATION_KEY_MENUDATA+"="
                        +toHexString(data[i])+" "
                        +toHexString(data[i+1])+" "
                        +toHexString(data[i+2])+" "
                        +toHexString(data[i+3])+" "
                        +toHexString(data[i+4])+" "
                        +toHexString(data[i+5])+" "
                        );

                String jpn = getJapaneseFromBytes(bytes, japanese);
                String eng = "";

                System.out.println(TRANSLATION_KEY_DATA+"="+bytesToHex(bytes));
                System.out.println(TRANSLATION_KEY_JPN +"="+jpn);
                System.out.println(TRANSLATION_KEY_ENG+"=");
                System.out.println();


                    //System.out.println(toHexString(i,5)+"  "+toHexString(a)+" "+toHexString(b)+"  "+toHexString(offsetData,5));
                    //System.out.println(Arrays.toString(strings));
                }
            }
            i++;
            /*int a = (data[i] & 0xFF);
            int b = (data[i + 1] & 0xFF);
            int offsetData = Integer.parseInt("18000",16) + (b * 256 + a);*/
            //String[] strings = DataReader.readPointerData(offsetData, data);

        }
        System.out.println(count);
    }

    public String getJapaneseFromBytes(byte[] data, Dictionnary japanese) {
        String res = "";
        CharType currentMode = CharType.MODE_F0;
        CharType previousMode = CharType.MODE_F0;
        byte[] fdBytes = null;
        for (byte b:data) {
            if (b==MODE_F0_BYTE) {
                previousMode = currentMode;
                currentMode = CharType.MODE_F0;
            }
            else if (b==MODE_F1_BYTE) {
                previousMode = currentMode;
                currentMode = CharType.MODE_F1;
            }
            else if (b==MODE_FB_BYTE) {
                previousMode = currentMode;
                currentMode = CharType.MODE_FB;
            }
            else if (b==MODE_FD_BYTE) {
                previousMode = currentMode;
                currentMode = CharType.MODE_FD;
            } else {
                if (currentMode == CharType.MODE_FD) {
                    if (fdBytes==null) {
                        fdBytes = new byte[2];
                        fdBytes[0] = b;
                    } else {
                        fdBytes[1] = b;
                        res+= "{"+toHexString(MODE_FD_BYTE)+" "+bytesToHex(fdBytes).trim()+"}";
                        currentMode = previousMode;
                    }
                }
                else {
                    String s = japanese.getJapanese(currentMode, b);
                    res += s;
                }
                if (currentMode==CharType.MODE_FB) {
                    currentMode = previousMode;
                }
            }

        }
        return res;
    }
}
