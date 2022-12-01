package memory;

import memory.cache.Cache;
import util.Transformer;

/**
 * 内存抽象类
 */

public class Memory {

    private static final int MEM_SIZE_B = 16 * 1024 * 1024;      // 16 MB

    private static final byte[] memory = new byte[MEM_SIZE_B];

    private static final Memory memoryInstance = new Memory();

    private Memory() {
    }

    public static Memory getMemory() {
        return memoryInstance;
    }

    public byte[] read(String pAddr, int len) {
        byte[] data = new byte[len];
        for (int ptr = 0; ptr < len; ptr++) {
            data[ptr] = memory[Integer.parseInt(Transformer.binaryToInt(pAddr)) + ptr];
        }
        return data;
    }

    public void write(String pAddr, int len, byte[] data) {
        // 通知Cache缓存失效
        Cache.getCache().invalid(pAddr, len);
        // 更新数据
        for (int ptr = 0; ptr < len; ptr++) {
            memory[Integer.parseInt(Transformer.binaryToInt(pAddr)) + ptr] = data[ptr];
        }
    }
}




















































