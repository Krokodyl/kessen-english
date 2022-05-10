package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteTownSigns extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000073", MAP_COLOR_01);
        put("522108", MAP_COLOR_02);
        put("733910", MAP_COLOR_03);
        put("9c5a39", MAP_COLOR_04);
        put("bd7b5a", MAP_COLOR_05);
        put("de9c7b", MAP_COLOR_06);
        put("4a3918", MAP_COLOR_07);
        put("94c663", MAP_COLOR_08);
        put("636342", MAP_COLOR_09);
        put("a5a552", MAP_COLOR_10);
        put("085221", MAP_COLOR_11);
        put("bdf794", MAP_COLOR_12);
        put("311008", MAP_COLOR_13);
        put("000000", MAP_COLOR_14);
        put("000000", MAP_COLOR_15);
        put("d6ceb5", MAP_COLOR_16);
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
