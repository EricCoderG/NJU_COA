package memory.disk;

import java.util.Arrays;

public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        int len = 0;
        for (int x : request) {
            len += Math.abs(x - start);
            start = x;
        }
        return (double) len / request.length;
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        int len = 0;
        int index = 0;
        int min;
        boolean[] vis = new boolean[request.length];

        for (int k = 0; k < request.length; k++) {
            min = Integer.MAX_VALUE;
            for (int i = 0; i < request.length; i++) {
                if (!vis[i] && Math.abs(start - request[i]) < min) {
                    index = i;
                    min = Math.abs(start - request[i]);
                }
            }
            vis[index] = true;
            len += min;
            start = request[index];
        }
        return (double) len / request.length;
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        int len = 0;
        Arrays.sort(request);
        int minIdx = request[0];
        int maxIdx = request[request.length - 1];
        if (direction) {
            if (start <= minIdx) { //需要取等号
                len += maxIdx - start;
            } else {
                len += Disk.TRACK_NUM - start - 1;
                len += Disk.TRACK_NUM - minIdx - 1;
            }
        } else {
            if (start >= maxIdx) { //需要取等号
                len += start - minIdx;
            } else {
                len += start;
                len += maxIdx;
            }
        }
        return (double) len / request.length;
    }

}
