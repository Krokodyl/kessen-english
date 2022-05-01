package enums;

import java.util.HashMap;
import java.util.Map;

import static enums.FontColor.*;

public class PaletteText extends Palette {

    Map<String, FontColor> mapGameColors = new HashMap<String, FontColor>(){{
        put("000073", MAP_2BPP_COLOR_01);
        put("f7f794", MAP_2BPP_COLOR_02);
        put("a58400", MAP_2BPP_COLOR_03);
        put("000000", MAP_2BPP_COLOR_04);
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
