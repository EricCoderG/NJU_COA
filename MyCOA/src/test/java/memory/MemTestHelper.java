package memory;

import java.util.Arrays;

public class MemTestHelper {

    /**
     * 此方法容易导致生成过大数组，谨慎使用，后期建议删除
     */
    public byte[] fillData(char dataUnit, int len) {
        byte[] data = new byte[len];
        Arrays.fill(data, (byte) dataUnit);
        return data;
    }

}
