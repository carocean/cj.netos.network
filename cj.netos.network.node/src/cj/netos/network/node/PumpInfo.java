package cj.netos.network.node;

import cj.studio.ecm.EcmException;

import java.util.Map;

public class PumpInfo {

    private int upstreamWorkThreadCount;
    private long upstreamQueueFileLength;
    private long upstreamQueueWaitTime;
    private int downstreamWorkThreadCount;
    private long downstreamQueueFileLength;
    private long downstreamQueueWaitTime;


    public void parse(Map<String, Object> node) {
        Map<String, Object> pump = (Map<String, Object>) node.get("pump");
        if (pump == null) {
            throw new EcmException(String.format("缺少pump配置，在文件：node.yaml"));
        }
        _parseUpstream((Map<String, Object>) pump.get("upstream"));
        _parseDownstream((Map<String, Object>) pump.get("downstream"));
    }

    private void _parseDownstream(Map<String, Object> downstream) {
        downstreamWorkThreadCount = downstream.get("workThreadCount") == null ? 8 : (int) downstream.get("workThreadCount");
        Map<String, Object> queue = (Map<String, Object>) downstream.get("queue");
        downstreamQueueFileLength = queue.get("fileLength") == null ? 1024 * 1024 * 1024L : Long.valueOf(queue.get("fileLength") + "");
        downstreamQueueWaitTime = queue.get("waitTime") == null ? 10000L : Long.valueOf(queue.get("waitTime") + "");
    }

    private void _parseUpstream(Map<String, Object> upstream) {
        upstreamWorkThreadCount = upstream.get("workThreadCount") == null ? 4 : (int) upstream.get("workThreadCount");
        Map<String, Object> queue = (Map<String, Object>) upstream.get("queue");
        upstreamQueueFileLength = queue.get("fileLength") == null ? 1024 * 1024 * 1024L : Long.valueOf(queue.get("fileLength") + "");
        upstreamQueueWaitTime = queue.get("waitTime") == null ? 10000L : Long.valueOf(queue.get("waitTime") + "");
    }

    public int upstreamWorkThreadCount() {
        return upstreamWorkThreadCount;
    }

    public int downstreamWorkThreadCount() {
        return downstreamWorkThreadCount;
    }

    public long upstreamQueueFileLength() {
        return upstreamQueueFileLength;
    }

    public long upstreamQueueWaitTime() {
        return upstreamQueueWaitTime;
    }

    public long downstreamQueueFileLength() {
        return downstreamQueueFileLength;
    }

    public long downstreamQueueWaitTime() {
        return downstreamQueueWaitTime;
    }
}
