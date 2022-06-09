package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteClasses extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000073", MAP_COLOR_01);
        put("f7ce94", MAP_COLOR_02);
        put("d6a573", MAP_COLOR_03);
        put("ad844a", MAP_COLOR_04);
        put("845a21", MAP_COLOR_05);
        put("5a1008", MAP_COLOR_06);
        put("9c0000", MAP_COLOR_07);
        put("dede31", MAP_COLOR_08);
        put("5abd21", MAP_COLOR_09);
        put("004200", MAP_COLOR_10);
        put("d6efff", MAP_COLOR_11);
        put("100818", MAP_COLOR_12);
        put("18186b", MAP_COLOR_13);
        put("3142b5", MAP_COLOR_14);
        put("526bff", MAP_COLOR_15);
        put("7b94ff", MAP_COLOR_16);
    }};

    public FontColor getFontColor(int i) {
        if (i==-16777216) return BLACK;
        else if (i==-1) return WHITE;
        else if (i==-16777126) return DARK_BLUE;
        else return DARK_GREY;
    }

    @Override
    public FontColor getFontColor(String hexa) {
        return mapGameColors.get(hexa);
    }

}
