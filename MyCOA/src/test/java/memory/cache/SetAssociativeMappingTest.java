package memory.cache;

import memory.Memory;
import memory.cache.Cache;
import memory.cache.cacheReplacementStrategy.FIFOReplacement;
import memory.cache.cacheReplacementStrategy.LFUReplacement;
import memory.cache.cacheReplacementStrategy.LRUReplacement;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.Transformer;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SetAssociativeMappingTest {

    private final Memory memory = Memory.getMemory();
    private final Cache cache = Cache.getCache();

    @BeforeClass
    public static void init() {
        Cache.getCache().setSETS(128);
        Cache.getCache().setSetSize(4);
    }

    @Before
    public void clearCache() {
        cache.clear();
    }

    @Test
    public void test01() {
        // 无替换发生
        cache.setReplacementStrategy(new FIFOReplacement());
        String pAddr1 = "00000000000000000000000001000000";
        String pAddr2 = "00000000000000000000000010000000";
        String pAddr3 = "00000000000001001000110110000000";

        byte[] input1 = new byte[64];
        Arrays.fill(input1, (byte) 'a');
        memory.write(pAddr1, input1.length, input1);
        assertArrayEquals(input1, cache.read(pAddr1, input1.length));
        assertTrue(cache.checkStatus(new int[]{1 * 4}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));

        byte[] input2 = new byte[64];
        Arrays.fill(input2, (byte) 'b');
        memory.write(pAddr2, input2.length, input2);
        assertArrayEquals(input2, cache.read(pAddr2, input2.length));
        assertTrue(cache.checkStatus(new int[]{2 * 4}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));

        byte[] input3 = {0b01110111, 0b01111110, 0b00010110, 0b01111110, 0b00110101, 0b00010100, 0b01010111, 0b00011101, 0b01111100, 0b01000000};
        memory.write(pAddr3, input3.length, input3);
        assertArrayEquals(input3, cache.read(pAddr3, input3.length));
        assertTrue(cache.checkStatus(new int[]{54 * 4}, new boolean[]{true}, new char[][]{"00000000000000000000100100".toCharArray()}));
    }

    @Test
    public void test02() {
        // FIFO替换策略
        Cache.getCache().setReplacementStrategy(new FIFOReplacement());
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        byte[] input4 = new byte[64];
        Arrays.fill(input1, (byte) 'c');
        Arrays.fill(input2, (byte) 'd');
        Arrays.fill(input3, (byte) 'e');
        Arrays.fill(input4, (byte) 'f');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000000000000000";
        String pAddr3 = "00000000111000000000000000000000";
        String pAddr4 = "00000000011100000000000001000001";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()});

        // cache中第0行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000010100000000".toCharArray()});

        // cache中第1行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        cache.checkStatus(new int[]{1}, new boolean[]{true}, new char[][]{"00000000000000011100000000".toCharArray()});

        // cache中的第4行和第8行应该被替换
        memory.write(pAddr4, input4.length, input4);
        dataRead = cache.read(pAddr4, input4.length);
        assertArrayEquals(input4, dataRead);
        assertTrue(cache.checkStatus(new int[]{4, 8}, new boolean[]{true, true}, new char[][]{"00000000000000001110000000".toCharArray(), "00000000000000001110000000".toCharArray()}));
    }

    @Test
    public void test03() {
        // LFU替换策略
        Cache.getCache().setReplacementStrategy(new LFUReplacement());
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        Arrays.fill(input1, (byte) 'g');
        Arrays.fill(input2, (byte) 'h');
        Arrays.fill(input3, (byte) 'i');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000000000000000";
        String pAddr3 = "00000000011100000000000001000000";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()});

        // 访问一遍cache的第0 2 3 5 6 7行
        byte[] dataHit = new byte[64];
        Arrays.fill(dataHit, (byte) 'g');
        for (int i = 0; i < 4; i++) {
            if (i == 1) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(13, 32) + "0000000000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }
        for (int i = 0; i < 4; i++) {
            if (i == 0) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(13, 32) + "0000001000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }

        // cache中第1行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{1}, new boolean[]{true}, new char[][]{"00000000000000010100000000".toCharArray()});

        // cache中的第4行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{4}, new boolean[]{true}, new char[][]{"00000000000000001110000000".toCharArray()}));
    }

    @Test
    public void test04() {
        // LRU替换策略
        Cache.getCache().setReplacementStrategy(new LRUReplacement());
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        Arrays.fill(input1, (byte) 'j');
        Arrays.fill(input2, (byte) 'k');
        Arrays.fill(input3, (byte) 'l');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000101000000000000000000000";
        String pAddr3 = "00000000011100000000000000000001";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()});

        // 访问一遍cache的第0 2 3 5 6 7行
        byte[] dataHit = new byte[64];
        Arrays.fill(dataHit, (byte) 'j');
        for (int i = 0; i < 4; i++) {
            if (i == 1) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(13, 32) + "0000000000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }
        for (int i = 0; i < 4; i++) {
            if (i == 0) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(13, 32) + "0000001000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }

        // cache中第1行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{1}, new boolean[]{true}, new char[][]{"00000000000000010100000000".toCharArray()});

        // cache中的第0行和第4行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{0, 4}, new boolean[]{true, true}, new char[][]{"00000000000000001110000000".toCharArray(), "00000000000000001110000000".toCharArray()}));
    }
}
