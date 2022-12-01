package memory.cache;

import memory.Memory;
import memory.cache.Cache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import util.Transformer;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

// 直接映射无替换策略
public class DirectMappingTest {

    private final Memory memory = Memory.getMemory();
    private final Cache cache = Cache.getCache();

    @BeforeClass
    public static void init() {
        Cache.getCache().setSETS(Cache.CACHE_SIZE_B / Cache.LINE_SIZE_B);
        Cache.getCache().setSetSize(1);
    }

    @Before
    public void clearCache() {
        cache.clear();
    }

    @Test
    public void test01() {
        byte[] data = {0b00000001, 0b00000010, 0b000000011, 0b00000100};
        String pAddr = "00000000000000000000000000000001";
        memory.write(pAddr, data.length, data);
        // 判断是否能够正确读出数据
        assertArrayEquals(data, cache.read(pAddr, data.length));
        // 判断Cache状态是否符合预期
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));
    }

    @Test
    public void test02() {
        byte[] data = {0b00000001, 0b00000010, 0b000000011, 0b00000100};
        String pAddr = "00000000000000001000000000000001";
        memory.write(pAddr, data.length, data);
        // 判断是否能够正确读出数据
        assertArrayEquals(data, cache.read(pAddr, data.length));
        // 判断Cache状态是否符合预期
        assertTrue(cache.checkStatus(new int[]{0}, new boolean[]{true}, new char[][]{"00000000000000000000000001".toCharArray()}));
    }

    @Test
    public void test03() {
        byte[] data = {0b00000001, 0b00000010, 0b000000011, 0b00000100};
        String pAddr = "00000000000000000000000001000001";
        memory.write(pAddr, data.length, data);
        // 判断是否能够正确读出数据
        assertArrayEquals(data, cache.read(pAddr, data.length));
        // 判断Cache状态是否符合预期
        assertTrue(cache.checkStatus(new int[]{Integer.parseInt(Transformer.binaryToInt("000000001"))}, new boolean[]{true}, new char[][]{"00000000000000000000000000".toCharArray()}));
    }

    @Test
    public void test04() {
        byte[] data = {0b00000001, 0b00000010, 0b000000011, 0b00000100};
        String pAddr = "00000000000000001000000001000001";
        memory.write(pAddr, data.length, data);
        // 判断是否能够正确读出数据
        assertArrayEquals(data, cache.read(pAddr, data.length));
        // 判断Cache状态是否符合预期
        assertTrue(cache.checkStatus(new int[]{Integer.parseInt(Transformer.binaryToInt("000000001"))}, new boolean[]{true}, new char[][]{"00000000000000000000000001".toCharArray()}));
    }

}
