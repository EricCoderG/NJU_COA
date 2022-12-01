package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().setTimeStamp(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        int replaceLineId = 0;
        Cache cache = Cache.getCache();
        long longestTimeSpan = 0;
        long timeNow = System.currentTimeMillis();

        for (int i = start; i <= end; i++) {
            if (!cache.isValid(i)) {
                replaceLineId = i;
                break;
            }
            if (timeNow - cache.getTimeStamp(i) > longestTimeSpan) {
                replaceLineId = i;
                longestTimeSpan = timeNow - cache.getTimeStamp(i);
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





























