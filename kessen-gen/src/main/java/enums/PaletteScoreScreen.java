package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteScoreScreen extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000000", MAP_COLOR_01);
        //put("000000", MAP_COLOR_02);
        //put("000000", MAP_COLOR_03);
        put("846300", MAP_COLOR_04);
        put("bd9408", MAP_COLOR_05);
        put("dec652", MAP_COLOR_06);
        put("ffff94", MAP_COLOR_07);
        put("6b0021", MAP_COLOR_08);
        put("b50094", MAP_COLOR_09);
        put("e742e7", MAP_COLOR_10);
        put("ff73ff", MAP_COLOR_11);
        //put("000000", MAP_COLOR_12);
        put("6394a5", MAP_COLOR_13);
        put("212121", MAP_COLOR_14);
        put("a5a5a5", MAP_COLOR_15);
        put("e7e7e7", MAP_COLOR_16);
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
