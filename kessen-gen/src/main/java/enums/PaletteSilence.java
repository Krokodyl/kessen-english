package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteSilence extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000000", MAP_COLOR_01);
        put("420000", MAP_COLOR_02);
        put("633100", MAP_COLOR_03);
        put("735200", MAP_COLOR_04);
        put("a58400", MAP_COLOR_05);
        put("d6b500", MAP_COLOR_06);
        put("ffff42", MAP_COLOR_07);
        put("ffffc6", MAP_COLOR_08);
        put("1c27ac", MAP_COLOR_09);
        put("420000", MAP_COLOR_10);
        put("841010", MAP_COLOR_11);
        put("b53129", MAP_COLOR_12);
        put("d67373", MAP_COLOR_13);
        put("f79c9c", MAP_COLOR_14);
        put("f7cece", MAP_COLOR_15);
        put("ffffff", MAP_COLOR_16);
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
