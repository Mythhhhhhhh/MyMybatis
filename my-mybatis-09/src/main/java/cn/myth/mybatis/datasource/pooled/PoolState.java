package cn.myth.mybatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

/**
 * 池状态
 */
public class PoolState {

    protected PooledDataSource dataSource;

    // 空闲链接
    protected final List<PooledConnection> idleConnections = new ArrayList<>();
    // 活跃链接
    protected final List<PooledConnection> activeConnections = new ArrayList<>();

    // 从连接池中获取连接的次数
    protected long requestCount = 0;
    // 请求连接总耗时（单位：毫秒）
    protected long accumulatedRequestTime = 0;
    // 连接执行时间总耗时
    protected long accumulatedCheckoutTime = 0;
    // 执行时间超时的连接数
    protected long claimedOverdueConnectionCount = 0;// 当连接长时间未归还给连接池时，会被认为该连接超时，该字段记录了超时的连接个数
    // 超时时间累加值
    protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
    // 等待时间累加值
    protected long accumulatedWaitTime = 0;// 当连接池全部连接已经被占用之后，新的请求会阻塞等待，该字段就记录了累计阻塞等待总时间
    // 等待时间
    protected long hadToWaitCount = 0;// 要等待的次数，记录了阻塞等待总次数
    // 无效连接数
    protected long badConnectionCount = 0;


    public PoolState(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized long getRequestCount() {
        return requestCount;
    }

    public synchronized long getAverageRequestTime() {
        return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }

    public synchronized long getAverageWaitTime() {
        return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }

    public synchronized long getHadToWaitCount() {
        return hadToWaitCount;
    }

    public synchronized long getBadConnectionCount() {
        return badConnectionCount;
    }

    public synchronized long getClaimedOverdueConnectionCount() {
        return claimedOverdueConnectionCount;
    }

    public synchronized long getAverageOverdueCheckoutTime() {
        return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }

    public synchronized long getAverageCheckoutTime() {
        return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    }

    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }
}
