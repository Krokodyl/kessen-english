package compression;

import services.Utils;

public class RepeatByte extends CompressedByte {

    byte position;
    byte size;

    /**
     * size
     * ----
     * 01101101 10101100
     *     ---- --------
     *     position
     */

    public RepeatByte(byte size, byte pos)
    {
        position = pos;
        this.size = size;
    }

    public RepeatByte(int size, int position) {
        this.position = (byte)(position);
        byte rightPart = (byte)(position >> 8);
        byte leftPart = (byte)(size << 4);
        this.size = (byte) (leftPart | rightPart);
    }

    public int getSize() {
        int i = size & 0xFF;
        return i >> 4;
    }

    public int getPosition() {
        return getExactPosition();
    }

    public int getExactPosition() {
        int exactPosition = (size & 0xFF) << 8;
        //System.out.println(Integer.toBinaryString(exactPosition & 0xFFF)+" "+Integer.toBinaryString(position & 0xFF));
        exactPosition = (exactPosition & 0xFFF) | (position & 0xFF);
        //System.out.println(Integer.toBinaryString(exactPosition));
        return exactPosition;
    }

    public String toString() {
        return "REPEAT : position "+getPosition()+"("+ Utils.toBinaryString(position)+") size "+getSize()+" ("+Utils.toBinaryString(size)+")";
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = size;
        bytes[1] = position;
        return bytes;
    }
}
