package entities;

import characters.JapaneseChar;
import enums.CharType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static services.Utils.toHexString;

public class Dictionnary {

    Map<CharType, Map<Byte, String>> japaneseDictionnaries = new HashMap<>();

    public Dictionnary(List<JapaneseChar> chars) {
        japaneseDictionnaries.put(CharType.MODE_F0, new HashMap<>());
        japaneseDictionnaries.put(CharType.MODE_F1, new HashMap<>());
        japaneseDictionnaries.put(CharType.MODE_FB, new HashMap<>());
        for (JapaneseChar c:chars) {
            byte b = (byte) Integer.parseInt(c.getCode(), 16);
            Map<Byte, String> map = japaneseDictionnaries.get(c.getType());
            map.put(b, c.getValue());
        }
    }

    public String getJapanese(CharType mode, byte value) {
        return japaneseDictionnaries.get(mode).get(value);
    }

    public String getJapaneseCharFromJapanese(String s){
        for (Map.Entry<Byte, String> entry : japaneseDictionnaries.get(CharType.MODE_F0).entrySet()) {
            if (entry.getValue().equals(s)) return toHexString(entry.getKey());
        }
        for (Map.Entry<Byte, String> entry : japaneseDictionnaries.get(CharType.MODE_F1).entrySet()) {
            if (entry.getValue().equals(s)) return toHexString(entry.getKey());
        }
        for (Map.Entry<Byte, String> entry : japaneseDictionnaries.get(CharType.MODE_FB).entrySet()) {
            if (entry.getValue().equals(s)) return toHexString(entry.getKey());
        }
        return "";
    }
}
