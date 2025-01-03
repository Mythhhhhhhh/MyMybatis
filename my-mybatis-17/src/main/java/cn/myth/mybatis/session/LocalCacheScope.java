package cn.myth.mybatis.session;

/**
 * 本地缓存机制：
 * SESSION 默认值，缓存一个会话中执行的所有查询
 * STATEMENT 不支持使用一级缓存，本地会话仅用在语句执行上，对相同SqlSession的不同调用将不做数据共享
 */
public enum LocalCacheScope {
    SESSION,
    STATEMENT
}
