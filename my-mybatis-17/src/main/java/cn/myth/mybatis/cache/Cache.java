package cn.myth.mybatis.cache;

/**
 * SPI(Service Provider Interface) for cache providers. 缓存接口
 * 缓存接口主要提供了数据的存放、获取、删除、情况，以及数量大小的获取。这样的实现方式和我们通常做业务开发时，定义的数据存放都是相识的。
 */
public interface Cache {

    /**
     * 获取ID，每个缓存都有唯一的ID标识
     */
    String getId();

    /**
     * 存入值
     */
    void putObject(Object key, Object value);

    /**
     * 获取值
     */
    Object getObject(Object key);

    /**
     * 删除值
     */
    Object removeObject(Object key);

    /**
     * 清空
     */
    void clear();

    /**
     * 获取缓存大小
     */
    int getSize();
}
