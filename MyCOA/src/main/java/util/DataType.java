package util;

import java.util.Collections;

public class DataType {

    private final byte[] data = new byte[4];

    public DataType(String dataStr) {
        // 目前是大端实现，高位字节存放在低地址
        int length = dataStr.length();
        if (length == 8 || length == 16 || length == 32) {
            dataStr = String.join("", Collections.nCopies(32 - length, "0")) + dataStr;
            for (int i = 0; i < 32; i++) {
                char temp = dataStr.charAt(i);
                if (temp == '0' || temp == '1') {
                    data[i / 8] |= ((dataStr.charAt(i) - '0') << (7 - i % 8));
                } else {
                    throw new NumberFormatException("Illegal dataStr: " + dataStr);
                }
            }
        } else {
            throw new NumberFormatException("Illegal dataStr: " + dataStr);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            stringBuilder.append(Transformer.intToBinary(String.valueOf(data[i])).substring(24));
        }
        return stringBuilder.toString();
    }

}
