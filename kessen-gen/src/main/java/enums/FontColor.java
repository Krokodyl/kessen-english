package enums;

public enum FontColor {

    BLACK(Integer.parseInt("0000000000000000",2)),
    WHITE(Integer.parseInt("1000000010000000",2)),
    DARK_GREY(Integer.parseInt("1000000000000000",2)),
    LIGHT_GREY(Integer.parseInt("0000000010000000",2));

    int mask;

    FontColor(int m) {
        mask = m;
    }

    public int getMask() {
        return mask;
    }

    public static FontColor getFontColor(int i) {
        if (i==-16777216) return BLACK;
        else if (i==-1) return WHITE;
        else if (i==-5592406) return LIGHT_GREY;
        else return DARK_GREY;
    }
}
