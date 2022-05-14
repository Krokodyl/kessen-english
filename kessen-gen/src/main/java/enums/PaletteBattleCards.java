package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteBattleCards extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000000", MAP_COLOR_01);
        put("00085a", MAP_COLOR_02);
        put("0039a5", MAP_COLOR_03);
        put("0073f7", MAP_COLOR_04);
        put("638cf7", MAP_COLOR_05);
        put("292929", MAP_COLOR_06);
        put("7b7b7b", MAP_COLOR_07);
        put("a5a5ad", MAP_COLOR_08);
        put("f7f7f7", MAP_COLOR_09);
        put("7b7b00", MAP_COLOR_10);
        put("a5a500", MAP_COLOR_11);
        put("cece00", MAP_COLOR_12);
        put("730000", MAP_COLOR_13);
        put("843931", MAP_COLOR_14);
        put("000000", MAP_COLOR_15);
        put("000039", MAP_COLOR_16);
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
