package memory;

import cpu.mmu.MMU;
import memory.cache.Cache;
import memory.cache.cacheReplacementStrategy.FIFOReplacement;
import memory.disk.Disk;
import memory.tlb.TLB;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class CacheTest {

    private final MMU mmu = MMU.getMMU();

    private final Memory memory = Memory.getMemory();

    private final Cache cache = Cache.getCache();

    private final Disk disk = Disk.getDisk();

    private final MemTestHelper helper = new MemTestHelper();

    @Before
    public void init() {
        Memory.PAGE = true;
        Memory.SEGMENT = true;
        Cache.isAvailable = true;
        TLB.isAvailable = false;
        MMU.getMMU().clear();
        Memory.timer = false;
        Cache.getCache().setSETS(128);
        Cache.getCache().setSetSize(4);
        Cache.getCache().setReplacementStrategy(new FIFOReplacement());
    }

    // 第0个虚页放到第0个物理页框，然后加载到cache的第0、4、8、12行，tag均为全0
    @Test
    public void test1() {
        int len = Memory.PAGE_SIZE_B;
        byte[] expect = disk.read("00000000000000000000000000000000", len);
        byte[] actual = mmu.read("000000000000000000000000000000000000000000000000", len);
        assertArrayEquals(expect, actual);
        assertTrue(memory.isValidPage(0));
        assertArrayEquals("00000000000000000000".toCharArray(), memory.getFrameOfPage(0));
        assertTrue(cache.checkStatus(new int[]{0, 4, 8, 12}, new boolean[]{true, true, true, true}, new char[][]{"00000000000000000000000000".toCharArray(), "00000000000000000000000000".toCharArray(), "00000000000000000000000000".toCharArray(), "00000000000000000000000000".toCharArray()}));
    }

}
