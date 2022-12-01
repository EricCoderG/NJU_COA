package memory.cache;

import memory.Memory;
import memory.cache.cacheReplacementStrategy.ReplacementStrategy;
import util.Transformer;

import java.util.Arrays;

/**
 * 高速缓存抽象类
 */
public class Cache {

    public static final boolean isAvailable = true; // 默认启用Cache

    public static final int CACHE_SIZE_B = 32 * 1024; // 32 KB 总大小

    public static final int LINE_SIZE_B = 64; // 64 B 行大小

    private final CacheLine[] cache = new CacheLine[CACHE_SIZE_B / LINE_SIZE_B];

    private int SETS;   // 组数

    private int setSize;    // 每组行数

    private static int setsBinaryLen; //组数二进制表示的长度

    private static int tagBinaryLen;

    private static final int tagLen = 26;

    // 单例模式
    private static final Cache cacheInstance = new Cache();

    private Cache() {
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new CacheLine();
        }
    }

    public static Cache getCache() {
        return cacheInstance;
    }

    private ReplacementStrategy replacementStrategy;    // 替换策略

    public static boolean isWriteBack;   // 写策略

    /**
     * 读取[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @param len   待读数据的字节数
     * @return 读取出的数据，以char数组的形式返回
     */
    public byte[] read(String pAddr, int len) {
        byte[] data = new byte[len];
        int addr = Integer.parseInt(Transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(Transformer.intToBinary(String.valueOf(addr)));
            byte[] cache_data = cache[rowNO].getData();
            int i = 0;
            while (i < nextSegLen) {
                data[index] = cache_data[addr % LINE_SIZE_B + i];
                index++;
                i++;
            }
            addr += nextSegLen;
        }
        return data;
    }

    /**
     * 向cache中写入[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @param len   待写数据的字节数
     * @param data  待写数据
     */
    public void write(String pAddr, int len, byte[] data) {
        int addr = Integer.parseInt(Transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(Transformer.intToBinary(String.valueOf(addr)));
            byte[] cache_data = cache[rowNO].getData();
            int i = 0;
            while (i < nextSegLen) {
                cache_data[addr % LINE_SIZE_B + i] = data[index];
                index++;
                i++;
            }

            // TODO
            if (isWriteBack) {
                cache[rowNO].dirty = true;
            } else {
                Memory.getMemory().write(pAddr, len, data);
            }

            addr += nextSegLen;
        }
    }


    /**
     * 查询{@link Cache#cache}表以确认包含pAddr的数据块是否在cache内
     * 如果目标数据块不在Cache内，则将其从内存加载到Cache
     *
     * @param pAddr 数据起始点(32位物理地址 = 26位块号 + 6位块内地址)
     * @return 数据块在Cache中的对应行号
     */
    private int fetch(String pAddr) {
        int blockId = getBlockNO(pAddr);
        int lineInCache = map(blockId);
        if (lineInCache == -1) {
            if (tagBinaryLen == tagLen) { //全关联映射
                lineInCache = 0;
            } else {
                lineInCache = Integer.parseInt(pAddr.substring(tagBinaryLen, tagLen), 2) * setSize;
            }

            if (replacementStrategy != null) {
                lineInCache = replacementStrategy.replace(lineInCache, lineInCache + setSize - 1, pAddr.substring(0, tagBinaryLen).toCharArray(), Memory.getMemory().read(pAddr.substring(0, tagLen) + "000000", LINE_SIZE_B));
            } else {
                //直接映射
                update(lineInCache, pAddr.substring(0, tagBinaryLen).toCharArray(), Memory.getMemory().read(pAddr.substring(0, tagLen) + "000000", LINE_SIZE_B));
            }
        } else {
            replacementStrategy.hit(lineInCache);
        }
        return lineInCache;
    }


    /**
     * 根据目标数据内存地址前26位的int表示，进行映射
     *
     * @param blockNO 数据在内存中的块号
     * @return 返回cache中所对应的行，-1表示未命中
     */
    private int map(int blockNO) {
        int setId = blockNO % SETS;
        int tag = blockNO >> setsBinaryLen;

        int startIndex = setId * setSize;
        for (int i = startIndex; i < startIndex + setSize; i++) {
            if (!cache[i].validBit) {
                continue;
            }
            if (Integer.parseInt(new String(cache[i].tag).substring(tagLen - tagBinaryLen, tagLen), 2) == tag) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 更新cache
     *
     * @param rowNO 需要更新的cache行号
     * @param tag   待更新数据的Tag
     * @param input 待更新的数据
     */
    public void update(int rowNO, char[] tag, byte[] input) {
        cache[rowNO].validBit = true;
        for (int i = 0; i < tagLen - tag.length; i++) {
            cache[rowNO].tag[i] = '0';
        }
        for (int i = tagLen - tag.length; i < tagLen; i++) {
            cache[rowNO].tag[i] = tag[i - (tagLen - tag.length)];
        }
        cache[rowNO].data = input;
    }


    /**
     * 从32位物理地址(26位块号 + 6位块内地址)获取目标数据在内存中对应的块号
     *
     * @param pAddr 32位物理地址
     * @return 数据在内存中的块号
     */
    private int getBlockNO(String pAddr) {
        return Integer.parseInt(Transformer.binaryToInt("0" + pAddr.substring(0, 26)));
    }


    /**
     * 该方法会被用于测试，请勿修改
     * 使用策略模式，设置cache的替换策略
     *
     * @param replacementStrategy 替换策略
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param SETS 组数
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
        setsBinaryLen = 0;
        while (SETS > 1) {
            setsBinaryLen++;
            SETS >>= 1;
        }
        tagBinaryLen = tagLen - setsBinaryLen;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param setSize 每组行数
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    /**
     * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
     * 该方法仅在memory类中使用，请勿修改
     *
     * @param pAddr 发生变化的数据段的起始地址
     * @param len   数据段长度
     */
    public void invalid(String pAddr, int len) {
        int from = getBlockNO(pAddr);
        int to = getBlockNO(Transformer.intToBinary(String.valueOf(Integer.parseInt(Transformer.binaryToInt("0" + pAddr)) + len - 1)));

        for (int blockNO = from; blockNO <= to; blockNO++) {
            int rowNO = map(blockNO);
            if (rowNO != -1) {
                cache[rowNO].validBit = false;
            }
        }
    }

    /**
     * 清除Cache全部缓存
     * 该方法会被用于测试，请勿修改
     */
    public void clear() {
        for (CacheLine line : cache) {
            if (line != null) {
                line.validBit = false;
            }
        }
    }

    /**
     * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param tags        tag
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
        if (lineNOs.length != validations.length || validations.length != tags.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            CacheLine line = cache[lineNOs[i]];
            if (line.validBit != validations[i]) {
                return false;
            }
            if (!Arrays.equals(line.getTag(), tags[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * Cache行，每行长度为(1+26+{@link Cache#LINE_SIZE_B})
     */
    private static class CacheLine {

        // 有效位，标记该条数据是否有效
        boolean validBit = false;

        // 脏位，标记该条数据是否被修改
        boolean dirty = false;

        // 用于LFU算法，记录该条cache使用次数
        int visited = 0;

        // 用于LRU和FIFO算法，记录该条数据时间戳
        Long timeStamp = 0L;

        // 标记，占位长度为26位，有效长度取决于映射策略：
        // 直接映射: 17 位
        // 全关联映射: 26 位
        // (2^n)-路组关联映射: 26-(9-n) 位
        // 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(17位)+行号(9位)+块内地址(6位)，
        // 那么对于值为0b1111的tag应该表示为00000000000000000000001111，其中低12位为有效长度
        char[] tag = new char[26];

        // 数据
        byte[] data = new byte[LINE_SIZE_B];

        byte[] getData() {
            return this.data;
        }

        char[] getTag() {
            return this.tag;
        }

    }

    // 获取有效位
    public boolean isValid(int rowNO) {
        return cache[rowNO].validBit;
    }

    // 获取脏位
    public boolean isDirty(int rowNO) {
        return cache[rowNO].dirty;
    }

    // LFU算法增加访问次数
    public void addVisited(int rowNO) {
        cache[rowNO].visited++;
    }

    //设置访问次数为1
    public void setVisitedOne(int rowNO) {
        cache[rowNO].visited = 1;
    }

    // 获取访问次数
    public int getVisited(int rowNO) {
        return cache[rowNO].visited;
    }

    // 用于LRU算法，重置时间戳
    public void setTimeStamp(int rowNO) {
        cache[rowNO].timeStamp = System.currentTimeMillis();
    }

    // 获取时间戳
    public long getTimeStamp(int rowNO) {
        return cache[rowNO].timeStamp;
    }

    // 获取该行数据
    public byte[] getData(int rowNO) {
        return cache[rowNO].data;
    }

    public String calAddr(int rowNO) {
        String setIdStr = Integer.toBinaryString(rowNO / setSize);
        int len = setIdStr.length();
        String tag = new String(cache[rowNO].tag).substring(tagLen - tagBinaryLen, tagLen);
        for (int i = 0; i < setsBinaryLen - len; i++) {
            setIdStr = "0" + setIdStr;
        }
        return tag + setIdStr + "000000";
    }

    public void WriteBack(int replaceLineId) {
        String pAddr = calAddr(replaceLineId);
        if (Cache.isWriteBack) {
            if (isDirty(replaceLineId) && isValid(replaceLineId)) {
                byte[] originalInput = getData(replaceLineId);
                Memory.getMemory().write(pAddr, LINE_SIZE_B, originalInput);
            }
        }
    }
}
