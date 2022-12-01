package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, byte[] input) {
        int replaceLineId = 0;
        Cache cache = Cache.getCache();
        int count = Integer.MAX_VALUE;

        for (int i = start; i <= end; i++) {
            if (!cache.isValid(i)) {
                replaceLineId = i;
                break;
            }
            if (cache.getVisited(i) < count) {
                replaceLineId = i;
                count = cache.getVisited(i);
            }
        }

        if (Cache.isWriteBack) {
            cache.WriteBack(replaceLineId);
        }

        cache.update(replaceLineId, addrTag, input);
        cache.setVisitedOne(replaceLineId);
        return replaceLineId;
    }

}
