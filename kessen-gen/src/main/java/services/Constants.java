package services;

public class Constants {

    public static String END_OF_LINE_CHARACTER_HEXA = "FF";
    public static byte END_OF_LINE_CHARACTER_BYTE = (byte) Integer.parseInt("FF",16);
    public static byte MODE_F0_BYTE = (byte) Integer.parseInt("F0",16);
    public static byte MODE_F1_BYTE = (byte) Integer.parseInt("F1",16);
    public static byte MODE_FB_BYTE = (byte) Integer.parseInt("FB",16);
    public static byte MODE_FD_BYTE = (byte) Integer.parseInt("FD",16);

    public static String NEW_LINE_CHARACTER_HEXA = "FE";

    public static int LENGTH_ARMOR_NAMES = 7;
    public static int LENGTH_WEAPON_NAMES = 7;
    public static int LENGTH_SHIELD_NAMES = 7;
    public static int LENGTH_CIY_NAMES = 4;
    public static int LENGTH_DIALOG_LINE = 30;

    public static int MENU_RIGHT_EDGE = 30;
    public static int MENU_BOTTOM_EDGE = 25;

    public static int OFFSET_FIRST_CHAR_00 = Integer.parseInt("d8000",16);
    public static int OFFSET_FIRST_CHAR_01 = Integer.parseInt("da000",16);
    public static int OFFSET_FIRST_CHAR_02 = Integer.parseInt("dc000",16);
    public static int OFFSET_END_CHAR_02 = Integer.parseInt("e0000",16);


    public static String TRANSLATION_KEY_VALUE_SEPARATOR = "=";
    public static String TRANSLATION_KEY_JPN = "JPN";
    public static String TRANSLATION_KEY_ENG = "ENG";
    public static String TRANSLATION_KEY_OFFSETDATA = "OFFSETDATA";
    public static String TRANSLATION_KEY_DATA = "DATA";
    public static String TRANSLATION_KEY_OFFSET = "OFFSET";
    public static String TRANSLATION_KEY_MENUDATA = "MENUDATA";
    public static String TRANSLATION_KEY_VALUE = "VALUE";

}
