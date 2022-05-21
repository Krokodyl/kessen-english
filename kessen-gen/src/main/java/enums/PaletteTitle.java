package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteTitle extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000073", MAP_COLOR_01);
        put("422910", MAP_COLOR_02);
        put("633100", MAP_COLOR_03);
        put("c66b21", MAP_COLOR_04);
        put("e78421", MAP_COLOR_05);
        put("ffa529", MAP_COLOR_06);
        put("841808", MAP_COLOR_07);
        put("bd1808", MAP_COLOR_08);
        put("210000", MAP_COLOR_09);
        put("636363", MAP_COLOR_10);
        put("94949c", MAP_COLOR_11);
        put("efefef", MAP_COLOR_12);
        put("ffffff", MAP_COLOR_13);
        put("2994ff", MAP_COLOR_14);
        put("d6ad00", MAP_COLOR_15);
        put("737300", MAP_COLOR_16);
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
