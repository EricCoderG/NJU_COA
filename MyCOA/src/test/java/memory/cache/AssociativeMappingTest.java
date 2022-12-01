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

public class AssociativeMappingTest {

    private final Memory memory = Memory.getMemory();
    private final Cache cache = Cache.getCache();

    @BeforeClass
    public static void init() {
        Cache.getCache().setSETS(1);
        Cache.getCache().setSetSize(Cache.CACHE_SIZE_B / Cache.LINE_SIZE_B);
    }

    @Before
    public void clearCache() {
        cache.clear();
    }

    @Test
    public void test01() {
        // 无替换发生
        Cache.getCache().setReplacementStrategy(new FIFOReplacement());
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        Arrays.fill(input1, (byte) 'a');
        Arrays.fill(input2, (byte) 'b');
        Arrays.fill(input3, (byte) 'c');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000000000000000000001000000";
        String pAddr3 = "00000000000000000000000111000000";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        assertTrue(cache.checkStatus(new int[]{0, 1}, new boolean[]{true, true}, new char[][]{"00000000000000000000000000".toCharArray(), "00000000000000000000000001".toCharArray()}));

        // cache第1行invalid，未命中，应重新加载
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);

        // cache第7行invalid，未命中，应重新加载
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);

        assertTrue(cache.checkStatus(new int[]{1, 7}, new boolean[]{true, true}, new char[][]{"00000000000000000000000001".toCharArray(), "00000000000000000000000111".toCharArray()}));
    }

    @Test
    public void test02() {
        // FIFO替换策略
        Cache.getCache().setReplacementStrategy(new FIFOReplacement());
        byte[] input1 = new byte[32 * 1024];
        byte[] input2 = new byte[64];
        byte[] input3 = new byte[64];
        Arrays.fill(input1, (byte) 'd');
        Arrays.fill(input2, (byte) 'e');
        Arrays.fill(input3, (byte) 'f');
        String pAddr1 = "00000000000000000000000000000000";
        String pAddr2 = "00000000000010100000000001000000";
        String pAddr3 = "00000000000001110000000001000001";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()});

        // cache中第0行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000010100000000001".toCharArray()});

        // cache中的第1行和第2行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{1, 2}, new boolean[]{true, true}, new char[][]{"00000000000001110000000001".toCharArray(), "00000000000001110000000010".toCharArray()}));
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
        String pAddr2 = "00000000000010100000000101000000";
        String pAddr3 = "00000000000001110000000001000000";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()});

        // 将除了第1行以外的所有行均访问一遍
        byte[] dataHit = new byte[64];
        Arrays.fill(dataHit, (byte) 'g');
        for (int i = 0; i < 512; i++) {
            if (i == 1) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(6, 32) + "000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }

        // cache中第1行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{1}, new boolean[]{true}, new char[][]{"00000000000010100000000101".toCharArray()});

        // cache中的第1行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{1}, new boolean[]{true}, new char[][]{"00000000000001110000000001".toCharArray()}));
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
        String pAddr2 = "00000000000010100000000101000000";
        String pAddr3 = "00000000000001110000000001000001";

        // 将cache填满
        memory.write(pAddr1, input1.length, input1);
        byte[] dataRead = cache.read(pAddr1, input1.length);
        assertArrayEquals(input1, dataRead);
        cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"000000000000000000000000000000".toCharArray()});

        // 将除了第2行以外的所有行均访问一遍
        byte[] dataHit = new byte[64];
        Arrays.fill(dataHit, (byte) 'j');
        for (int i = 0; i < 512; i++) {
            if (i == 2) continue;
            String pAddrHit = Transformer.intToBinary("" + i).substring(6, 32) + "000000";
            dataRead = cache.read(pAddrHit, dataHit.length);
            assertArrayEquals(dataHit, dataRead);
        }

        // cache中第2行应该被替换
        memory.write(pAddr2, input2.length, input2);
        dataRead = cache.read(pAddr2, input2.length);
        assertArrayEquals(input2, dataRead);
        cache.checkStatus(new int[]{2}, new boolean[]{true}, new char[][]{"00000000000010100000000101".toCharArray()});

        // cache中的第0行和第1行应该被替换
        memory.write(pAddr3, input3.length, input3);
        dataRead = cache.read(pAddr3, input3.length);
        assertArrayEquals(input3, dataRead);
        assertTrue(cache.checkStatus(new int[]{0, 1}, new boolean[]{true, true}, new char[][]{"00000000000001110000000001".toCharArray(), "00000000000001110000000010".toCharArray()}));
    }

}
