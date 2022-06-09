package entities;

import java.util.Arrays;

public class MonsterSkillSet {
    
    int magicValue;
    int pointerOffset;
    int count;
    byte[] values;

    public MonsterSkillSet(int magicValue, int pointerOffset, int count, byte[] values) {
        this.magicValue = magicValue;
        this.pointerOffset = pointerOffset;
        this.count = count;
        this.values = values;
    }

    public int getMagicValue() {
        return magicValue;
    }

    public void setMagicValue(int magicValue) {
        this.magicValue = magicValue;
    }

    public int getPointerOffset() {
        return pointerOffset;
    }

    public void setPointerOffset(int pointerOffset) {
        this.pointerOffset = pointerOffset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public byte[] getValues() {
        return values;
    }

    public void setValues(byte[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "MonsterSkill{" +
                "magicValue=" + magicValue +
                ", pointerOffset=" + pointerOffset +
                ", count=" + count +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
