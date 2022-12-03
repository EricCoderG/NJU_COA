package memory;

import cpu.mmu.MMU;
import memory.cache.Cache;
import memory.tlb.TLB;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * 实模式读取数据，逻辑地址(48-bits)直接转化为物理地址(32-bits)
 */
public class RealTest {

    private static MMU mmu;

    private static MemTestHelper helper;

    @BeforeClass
    public static void init() {
        mmu = MMU.getMMU();
        Memory.PAGE = false;
        Memory.SEGMENT = false;
        helper = new MemTestHelper();
        MMU.getMMU().clear();
        Cache.isAvailable = false;
        TLB.isAvailable = false;
        Memory.timer = false;
    }

    @Test
    public void test1() {
        int len = 128;
        byte[] expect = helper.fillData('0', len);
        byte[] actual = mmu.read("000000000000000000000000000000000000000000000000", len);
        assertArrayEquals(expect, actual);
    }

	@Test
	public void test2() {
		int len = 128;
        byte[] expect = helper.fillData('1', len);
        byte[] actual = mmu.read("000000000100000000000000000000000000000000000000", len);
		assertArrayEquals(expect, actual);
	}

    @Test
    public void test3() {
        int len = 128;
        byte[] expect = helper.fillData('2', len);
        byte[] actual = mmu.read("000000000000000000000000000000000000100000000000", len);
        assertArrayEquals(expect, actual);
    }

    @Test
    public void test4() {
        int len = 128;
        byte[] expect = helper.fillData('Q', len);
        byte[] actual = mmu.read("000010000000000000000000000000000000010000000000", len);
        assertArrayEquals(expect, actual);
    }

}
