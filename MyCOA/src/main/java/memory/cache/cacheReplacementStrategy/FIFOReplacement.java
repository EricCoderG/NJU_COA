package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        //只有在添加进cache的时候需要重置timestamp
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        int replaceLineId = 0;
        Cache cache = Cache.getCache();
        long timeStamp = Long.MAX_VALUE;

        for (int i = start; i <= end; i++) {
            if (!cache.isValid(i)) {
                replaceLineId = i;
                break;
            }
            if (cache.getTimeStamp(i) < timeStamp) {
                replaceLineId = i;
                timeStamp = cache.getTimeStamp(i);
            }
        }

        if (Cache.isWriteBack) {
            cache.WriteBack(replaceLineId);
        }

        cache.update(replaceLineId, addrTag, input);
        cache.setTimeStamp(replaceLineId);
        return replaceLineId;
    }

}
