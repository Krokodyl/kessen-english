package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteFreeTown extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000000", MAP_COLOR_01);
        put("940000", MAP_COLOR_02);
        put("bd6b42", MAP_COLOR_03);
        put("4a4a4a", MAP_COLOR_04);
        put("b5b5b5", MAP_COLOR_05);
        put("b58c00", MAP_COLOR_06);
        put("ffd600", MAP_COLOR_07);
        put("ffff7b", MAP_COLOR_08);
        put("4a4a4a", MAP_COLOR_09);
        put("ce0000", MAP_COLOR_10);
        put("737373", MAP_COLOR_11);
        put("188400", MAP_COLOR_12);
        put("9c9c9c", MAP_COLOR_13);
        put("bdbdbd", MAP_COLOR_14);
        put("dedede", MAP_COLOR_15);
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
