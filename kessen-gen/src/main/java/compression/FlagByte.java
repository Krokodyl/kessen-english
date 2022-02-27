package compression;

import services.Kessen;
import services.Utils;

import javax.rmi.CORBA.Util;

public class FlagByte extends CompressedByte {

    byte value;

    public FlagByte(byte value) {
        this.value = value;
        //System.out.println("FLAG BEFORE "+Utils.toHexString(this.value));
        if (Kessen.REVERSE_FLAG_BITS) {
            this.value = (byte) (Utils.reverse(this.value) & 0xFF);
        }
        if (Kessen.FLIP_FLAG_BITS) {
            this.value = (byte) (Utils.complement(this.value) & 0xFF);
        }
        //System.out.println("FLAG AFTER "+Utils.toHexString(this.value));
    }

    /**
     * index between 0 and 7
     */
    public boolean isNewData(int index) {
        String binaryString = Utils.toBinaryString(value);
        return binaryString.charAt(8-index-1)=='1';
    }

    @Override
    public String toString() {
        return "FLAG : "+Utils.toHexString(value)+" "+Utils.toBinaryString(value);
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[1];
        bytes[0] = value;
        return bytes;
    }
}
